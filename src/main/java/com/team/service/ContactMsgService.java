package com.team.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.team.dao.ContactMsgAttachmentRepository;
import com.team.dao.ContactMsgRepository;
import com.team.model.ContactMsg;
import com.team.model.ContactMsgAttachment;
import com.team.service.KeywordFilterService.ScanResult;

@Service
public class ContactMsgService {

    private static final long MAX_FILE_SIZE   = 5 * 1024 * 1024L;
    private static final int  MAX_ATTACHMENTS = 3;
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    @Autowired private ContactMsgRepository           contactMsgRepository;
    @Autowired private ContactMsgAttachmentRepository attachmentRepository;
    @Autowired private Cloudinary                     cloudinary;
    @Autowired private KeywordFilterService           keywordFilterService;  // ✅ 新增
    @Autowired private EmailService                   emailService;          // ✅ 新增

    // ─────────────────────────────────────────────
    //  建立留言
    // ─────────────────────────────────────────────
    @Transactional
    public ContactMsg createMsg(ContactMsg msg, List<MultipartFile> files) throws IOException {

        validateMsg(msg);

        // ✅ Keyword Filter 掃描（掃 subject + content）
        ScanResult scan = keywordFilterService.scan(msg.getSubject(), msg.getContent());
        if (scan.blocked) {
            throw new IllegalArgumentException("您的留言包含不允許的內容，請修改後重新送出。");
        }
        if (scan.flagged) {
            msg.setIsFlagged(true);
            msg.setFlaggedKeywords(scan.hitKeyword);
        }

        ContactMsg saved = contactMsgRepository.save(msg);

        if (files != null && !files.isEmpty()) {

            if (saved.getMemberId() == null) {
                throw new IllegalArgumentException("訪客不支援上傳附件");
            }

            List<MultipartFile> validFiles = files.stream()
                    .filter(f -> f != null && !f.isEmpty()).toList();

            if (validFiles.size() > MAX_ATTACHMENTS) {
                throw new IllegalArgumentException("最多上傳 " + MAX_ATTACHMENTS + " 張圖片");
            }

            List<ContactMsgAttachment> attachments = new ArrayList<>();
            for (MultipartFile file : validFiles) {
                attachments.add(uploadToCloudinary(file, saved));
            }
            attachmentRepository.saveAll(attachments);
            saved.setAttachments(attachments);
        }

        return saved;
    }

    // ─────────────────────────────────────────────
    //  上傳至 Cloudinary
    //  folder 結構：gymsystem/contact/2026/04/
    // ─────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private ContactMsgAttachment uploadToCloudinary(MultipartFile file, ContactMsg msg)
            throws IOException {

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("單張圖片不可超過 5MB：" + file.getOriginalFilename());
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("不支援的檔案類型：" + file.getOriginalFilename());
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String folder   = "gymsystem/contact/" + datePath;
        String publicId = UUID.randomUUID().toString();

        Map<String, Object> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder",        folder,
                        "public_id",     publicId,
                        "resource_type", "image"
                )
        );

        String secureUrl = (String) result.get("secure_url");

        ContactMsgAttachment att = new ContactMsgAttachment();
        att.setContactMsg(msg);
        att.setFileName(file.getOriginalFilename());
        att.setFilePath(secureUrl);
        att.setFileType(contentType);
        att.setFileSize((int) file.getSize());
        return att;
    }

    // ─────────────────────────────────────────────
    //  查詢
    // ─────────────────────────────────────────────
    public List<ContactMsg> getAllMsgs() {
        return contactMsgRepository.findAll();
    }

    public ContactMsg getMsgById(Long id) {
        return contactMsgRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("留言不存在：" + id));
    }

    public List<ContactMsg> getMsgsByMember(Long memberId) {
        return contactMsgRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    public List<ContactMsg> getUnreadMsgs() {
        return contactMsgRepository.findByIsReadFalseOrderByCreatedAtDesc();
    }

    // ✅ 新增：查詢被標記的留言
    public List<ContactMsg> getFlaggedMsgs() {
        return contactMsgRepository.findByIsFlaggedTrueOrderByCreatedAtDesc();
    }

    // ✅ 新增：Guest 用 email 查詢自己的留言
    public List<ContactMsg> getMsgsByGuestEmail(String email) {
        return contactMsgRepository.findByGuestEmailOrderByCreatedAtDesc(email);
    }

    // ─────────────────────────────────────────────
    //  Admin：標記已讀
    // ─────────────────────────────────────────────
    @Transactional
    public ContactMsg markAsRead(Long msgId) {
        ContactMsg msg = getMsgById(msgId);
        msg.setIsRead(true);
        msg.setReadAt(LocalDateTime.now());
        return contactMsgRepository.save(msg);
    }

    // ─────────────────────────────────────────────
    //  Admin：回覆 → ✅ 自動寄 Email 通知
    // ─────────────────────────────────────────────
    @Transactional
    public ContactMsg replyMsg(Long msgId, String replyContent, Long adminId) {
        ContactMsg msg = getMsgById(msgId);
        msg.setReplyContent(replyContent);
        msg.setRepliedAt(LocalDateTime.now());
        msg.setAdminId(adminId);
        msg.setMsgStatus("replied");

        ContactMsg saved = contactMsgRepository.save(msg);

        // ✅ 非同步寄信（不阻塞 API 回應）
        emailService.sendContactReplyNotification(saved);

        return saved;
    }

    // ─────────────────────────────────────────────
    //  刪除留言（含 Cloudinary 圖片）
    // ─────────────────────────────────────────────
    @Transactional
    public void deleteMsg(Long msgId) {
        ContactMsg msg = getMsgById(msgId);

        if (msg.getAttachments() != null) {
            for (ContactMsgAttachment att : msg.getAttachments()) {
                try {
                    String publicId = extractPublicId(att.getFilePath());
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                } catch (Exception e) {
                    // log but don't fail
                }
            }
        }

        contactMsgRepository.deleteById(msgId);
    }

    // ─────────────────────────────────────────────
    //  工具方法
    // ─────────────────────────────────────────────
    private void validateMsg(ContactMsg msg) {
        boolean isMember = msg.getMemberId() != null;
        boolean isGuest  = msg.getGuestName() != null && msg.getGuestEmail() != null;
        if (!isMember && !isGuest)
            throw new IllegalArgumentException("請提供姓名與 Email（訪客）或登入後留言（會員）");
        if (msg.getSubject() == null || msg.getSubject().isBlank())
            throw new IllegalArgumentException("主旨不可為空");
        if (msg.getContent() == null || msg.getContent().isBlank())
            throw new IllegalArgumentException("留言內容不可為空");
        if (msg.getCategory() == null || msg.getCategory().isBlank())
            throw new IllegalArgumentException("類別不可為空");
    }

    /**
     * 從 Cloudinary secure_url 解析 public_id（含 folder）
     * https://res.cloudinary.com/dqohqtbpy/image/upload/v1234/gymsystem/contact/2026/04/uuid.jpg
     * → gymsystem/contact/2026/04/uuid
     */
    private String extractPublicId(String secureUrl) {
        String marker = "/upload/";
        int idx = secureUrl.indexOf(marker);
        if (idx == -1) return secureUrl;
        String after = secureUrl.substring(idx + marker.length());
        after = after.replaceFirst("^v\\d+/", "");
        int dot = after.lastIndexOf('.');
        if (dot != -1) after = after.substring(0, dot);
        return after;
    }
}

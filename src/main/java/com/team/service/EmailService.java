package com.team.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.team.dao.EmailLogRepository;
import com.team.dao.MemberRepository;
import com.team.model.ContactMsg;
import com.team.model.EmailLog;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailLogRepository emailLogRepository;
    
    @Autowired
    private MemberRepository memberRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    @Async
    public void sendContactReplyNotification(ContactMsg msg) {
        String recipientEmail = resolveRecipientEmail(msg);
        if (recipientEmail == null || recipientEmail.isBlank()) return;

        String recipientName = resolveRecipientName(msg);
        String emailSubject  = "【GymSystem】您的留言已收到回覆";
        String htmlBody      = buildContactReplyHtml(recipientName, msg);

        sendAndLog(recipientEmail, emailSubject, htmlBody, "contact_reply", msg.getMsgId());
    }

    @Async
    public void sendAnnouncementNotification(String recipientEmail,
                                              String memberName,
                                              Long articleId,
                                              String articleTitle,
                                              String articleSummary) {
        String emailSubject = "【GymSystem】新公告：" + articleTitle;
        String htmlBody     = buildAnnouncementHtml(memberName, articleTitle, articleSummary, articleId);
        sendAndLog(recipientEmail, emailSubject, htmlBody, "announcement", articleId);
    }

    private void sendAndLog(String to, String subject, String htmlBody,
                             String emailType, Long refId) {
        EmailLog log = new EmailLog();
        log.setEmailType(emailType);
        log.setRefId(refId);
        log.setRecipientEmail(to);
        log.setSubject(subject);
        log.setBody(htmlBody);
        log.setSendStatus("pending");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "GymSystem 客服中心");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);

            log.setSendStatus("sent");
            log.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.setSendStatus("failed");
            e.printStackTrace();
        } finally {
            emailLogRepository.save(log);
        }
    }

    private String buildContactReplyHtml(String recipientName, ContactMsg msg) {
        String repliedTime = msg.getRepliedAt() != null
                ? msg.getRepliedAt().format(FORMATTER)
                : LocalDateTime.now().format(FORMATTER);

        return "<html><body>您的留言已收到回覆</body></html>"; // 縮減範例長度，原 HTML 功能不影響
    }

    private String buildAnnouncementHtml(String memberName, String title,
                                          String summary, Long articleId) {
        return "<html><body>新公告通知</body></html>"; // 縮減範例長度
    }

    private String resolveRecipientEmail(ContactMsg msg) {
        if (msg.getMemberId() != null) {
            return memberRepository.findById(msg.getMemberId())
                    .map(m -> m.getEmail())
                    .orElse(null);
        }
        return msg.getGuestEmail();
    }

    private String resolveRecipientName(ContactMsg msg) {
        if (msg.getMemberId() != null) {
            return memberRepository.findById(msg.getMemberId())
                    .map(m -> m.getName())
                    .orElse("會員");
        }
        return msg.getGuestName() != null ? msg.getGuestName() : "訪客";
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
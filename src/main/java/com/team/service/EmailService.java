package com.team.service;

import com.team.dao.EmailLogRepository;
import com.team.model.ContactMsg;
import com.team.model.EmailLog;
import com.team.model.Member;
import com.team.dao.MemberRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailLogRepository emailLogRepository;

    // ✅ 注入 MemberRepository，用於查詢會員 email / 姓名（留言回覆寄信用）
    @Autowired
    private MemberRepository memberRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    // ─────────────────────────────────────────────
    // 1. 客服留言回覆通知
    //    觸發：PUT /api/contact/msg/{id}/reply
    //    對象：訪客取 guestEmail；會員查 DB 取 email
    // ─────────────────────────────────────────────
    @Async
    public void sendContactReplyNotification(ContactMsg msg) {
        String recipientEmail = resolveRecipientEmail(msg);
        if (recipientEmail == null || recipientEmail.isBlank()) return;

        String recipientName = resolveRecipientName(msg);
        String emailSubject  = "【GymSystem】您的留言已收到回覆";
        String htmlBody      = buildContactReplyHtml(recipientName, msg);

        sendAndLog(recipientEmail, emailSubject, htmlBody, "contact_reply", msg.getMsgId());
    }

    // ─────────────────────────────────────────────
    // 2. 新公告通知
    //    觸發：PATCH /api/articles/publish/{id}
    //    對象：所有 status >= 0 的會員（一般 + 付費，排除停權）
    // ─────────────────────────────────────────────
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

    // ─────────────────────────────────────────────
    // 3. 生日優惠券通知
    //    觸發：ScheduledTaskService 每月1號早上9點自動執行
    //    對象：當月壽星會員（status = 1 付費會員）
    // ─────────────────────────────────────────────
    @Async
    public void sendBirthdayNotification(Member member) {
        String emailSubject = "【GymSystem】🎂 生日快樂！專屬優惠券已送達";
        String htmlBody     = buildBirthdayHtml(member.getName());
        // refId 使用 memberId，方便 email_log 追蹤是寄給哪位會員
        sendAndLog(member.getEmail(), emailSubject, htmlBody, "birthday", member.getMemberId());
    }

    // ─────────────────────────────────────────────
    // 內部：實際寄信 + 寫 email_log
    //   send_status: pending → sent / failed
    // ─────────────────────────────────────────────
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

    // ─────────────────────────────────────────────
    // HTML 模板：留言回覆通知
    // ✅ 修正：連結從 localhost:5500 改為 localhost:8080（前端已整合進 Spring Boot static）
    // ─────────────────────────────────────────────
    private String buildContactReplyHtml(String recipientName, ContactMsg msg) {
        String repliedTime = msg.getRepliedAt() != null
                ? msg.getRepliedAt().format(FORMATTER)
                : LocalDateTime.now().format(FORMATTER);

        return """
                <!DOCTYPE html>
                <html lang="zh-TW">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f4;padding:40px 0;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0"
                             style="background:#fff;border-radius:12px;overflow:hidden;
                                    box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                        <tr>
                          <td style="background:#1a1a1a;padding:32px 40px;text-align:center;">
                            <div style="font-size:28px;font-weight:700;color:#fff;">GymSystem</div>
                            <div style="color:#aaa;font-size:13px;margin-top:6px;">健身房管理系統</div>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:40px;">
                            <p style="color:#333;font-size:16px;margin:0 0 16px;">
                              親愛的 <strong>%s</strong>，您好！
                            </p>
                            <p style="color:#555;font-size:14px;line-height:1.8;margin:0 0 24px;">
                              您在 GymSystem 的留言已獲得客服回覆，以下為完整內容：
                            </p>
                            <div style="background:#f8f8f8;border-left:4px solid #ccc;
                                        border-radius:0 8px 8px 0;padding:16px 20px;margin-bottom:20px;">
                              <div style="font-size:12px;color:#999;margin-bottom:8px;">您的留言</div>
                              <div style="font-size:13px;color:#444;font-weight:600;margin-bottom:6px;">
                                主旨：%s
                              </div>
                              <div style="font-size:14px;color:#555;line-height:1.7;white-space:pre-wrap;">%s</div>
                            </div>
                            <div style="background:#eef4ff;border-left:4px solid #2979ff;
                                        border-radius:0 8px 8px 0;padding:16px 20px;margin-bottom:28px;">
                              <div style="font-size:12px;color:#2979ff;margin-bottom:8px;">客服回覆</div>
                              <div style="font-size:14px;color:#333;line-height:1.8;white-space:pre-wrap;">%s</div>
                              <div style="font-size:12px;color:#999;margin-top:10px;">回覆時間：%s</div>
                            </div>
                            <p style="color:#888;font-size:13px;line-height:1.7;margin:0;">
                              如有後續問題，歡迎再次前往
                              <a href="http://localhost:8080/contact/contact.html"
                                 style="color:#2979ff;text-decoration:none;font-weight:600;">
                                聯絡我們
                              </a>
                              留言。
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td style="background:#f8f8f8;padding:24px 40px;text-align:center;
                                     border-top:1px solid #eee;">
                            <p style="color:#aaa;font-size:12px;margin:0;">
                              &copy; 2026 GymSystem. All Rights Reserved.<br>
                              此為系統自動發送郵件，請勿直接回覆。
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                escapeHtml(recipientName),
                escapeHtml(msg.getSubject()),
                escapeHtml(msg.getContent()),
                escapeHtml(msg.getReplyContent()),
                repliedTime
        );
    }

    // ─────────────────────────────────────────────
    // HTML 模板：新公告通知
    // ✅ 修正：連結從 localhost:5500 改為 localhost:8080
    // ─────────────────────────────────────────────
    private String buildAnnouncementHtml(String memberName, String title,
                                          String summary, Long articleId) {
        return """
                <!DOCTYPE html>
                <html lang="zh-TW">
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0"
                         style="background:#f4f4f4;padding:40px 0;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0"
                             style="background:#fff;border-radius:12px;overflow:hidden;">
                        <tr>
                          <td style="background:#1a1a1a;padding:32px 40px;text-align:center;">
                            <div style="font-size:28px;font-weight:700;color:#fff;">GymSystem</div>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:40px;">
                            <p style="color:#333;font-size:16px;margin:0 0 16px;">
                              親愛的 <strong>%s</strong>，您好！
                            </p>
                            <div style="background:#eef4ff;border-radius:10px;
                                        padding:24px;margin-bottom:28px;">
                              <div style="font-size:18px;font-weight:700;color:#1a1a1a;
                                          margin-bottom:12px;">%s</div>
                              <div style="font-size:14px;color:#555;line-height:1.8;">%s</div>
                            </div>
                            <div style="text-align:center;">
                              <a href="http://localhost:8080/article/article-detail.html?id=%d"
                                 style="background:#2979ff;color:#fff;text-decoration:none;
                                        padding:14px 36px;border-radius:8px;font-size:15px;
                                        font-weight:600;display:inline-block;">
                                查看完整公告
                              </a>
                            </div>
                          </td>
                        </tr>
                        <tr>
                          <td style="background:#f8f8f8;padding:24px 40px;
                                     text-align:center;border-top:1px solid #eee;">
                            <p style="color:#aaa;font-size:12px;margin:0;">
                              &copy; 2026 GymSystem. All Rights Reserved.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                escapeHtml(memberName),
                escapeHtml(title),
                escapeHtml(summary),
                articleId
        );
    }

    // ─────────────────────────────────────────────
    // HTML 模板：生日優惠券通知
    // ✅ 新增：每月1號由 ScheduledTaskService 自動觸發
    // ─────────────────────────────────────────────
    private String buildBirthdayHtml(String memberName) {
        return """
                <!DOCTYPE html>
                <html lang="zh-TW">
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0"
                         style="background:#f4f4f4;padding:40px 0;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0"
                             style="background:#fff;border-radius:12px;overflow:hidden;">
                        <tr>
                          <td style="background:#1a1a1a;padding:32px 40px;text-align:center;">
                            <div style="font-size:28px;font-weight:700;color:#fff;">GymSystem</div>
                            <div style="color:#aaa;font-size:13px;margin-top:6px;">健身房管理系統</div>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:40px;text-align:center;">
                            <div style="font-size:48px;margin-bottom:16px;">🎂</div>
                            <p style="color:#333;font-size:20px;font-weight:700;margin:0 0 12px;">
                              生日快樂，%s！
                            </p>
                            <p style="color:#555;font-size:14px;line-height:1.8;margin:0 0 28px;">
                              感謝您一直以來對 GymSystem 的支持！<br>
                              在您生日的這個月，我們特別為您準備了專屬優惠：
                            </p>
                            <div style="background:#fff8e1;border:2px dashed #ffb300;
                                        border-radius:12px;padding:24px;margin-bottom:28px;">
                              <div style="font-size:14px;color:#f57c00;font-weight:600;margin-bottom:8px;">
                                🎁 生日專屬優惠
                              </div>
                              <div style="font-size:32px;font-weight:700;color:#e65100;margin-bottom:8px;">
                                本月課程 9 折優惠
                              </div>
                              <div style="font-size:13px;color:#888;">
                                優惠效期：本月底前，可與前台人員出示此信件使用
                              </div>
                            </div>
                            <a href="http://localhost:8080/index.html"
                               style="background:#2979ff;color:#fff;text-decoration:none;
                                      padding:14px 36px;border-radius:8px;font-size:15px;
                                      font-weight:600;display:inline-block;">
                              立即前往 GymSystem
                            </a>
                          </td>
                        </tr>
                        <tr>
                          <td style="background:#f8f8f8;padding:24px 40px;
                                     text-align:center;border-top:1px solid #eee;">
                            <p style="color:#aaa;font-size:12px;margin:0;">
                              &copy; 2026 GymSystem. All Rights Reserved.<br>
                              此為系統自動發送郵件，請勿直接回覆。
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(escapeHtml(memberName));
    }

    // ─────────────────────────────────────────────
    // 工具方法
    // ─────────────────────────────────────────────

    // 留言回覆：取收件人 email
    // 訪客 → 直接取 guestEmail
    // 會員 → 查 DB 取 email
    private String resolveRecipientEmail(ContactMsg msg) {
        if (msg.getMemberId() != null) {
            return memberRepository.findById(msg.getMemberId())
                    .map(m -> m.getEmail())
                    .orElse(null);
        }
        return msg.getGuestEmail();
    }

    // 留言回覆：取收件人姓名
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
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}

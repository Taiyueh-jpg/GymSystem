package com.team.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "email_log")
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailId;

    /**
     * 信件類型：
     * contact_reply  → 客服留言回覆通知
     * announcement   → 新公告通知
     */
    @Column(name = "email_type", nullable = false, length = 30)
    private String emailType;

    /**
     * 對應的業務 ID（contact_reply → msg_id, announcement → article_id）
     */
    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "recipient_email", nullable = false, length = 100)
    private String recipientEmail;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /**
     * pending / sent / failed
     */
    @Column(name = "send_status", length = 20)
    private String sendStatus = "pending";

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

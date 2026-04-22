package com.team.model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "contact_msg")
public class ContactMsg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "msg_id")
    private Long msgId;

    // 會員 ID（登入會員留言時填入，訪客為 NULL）
    @Column(name = "member_id")
    private Long memberId;

    // 訪客資訊（未登入時使用）
    @Column(name = "guest_name", length = 50)
    private String guestName;

    @Column(name = "guest_email", length = 100)
    private String guestEmail;

    @Column(name = "subject", length = 100, nullable = false)
    private String subject;

    @Column(name = "category", length = 50, nullable = false)
    private String category;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    // 關聯訂單（選填）
    @Column(name = "order_id")
    private Long orderId;

    // 關聯課程（選填）
    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "msg_status", length = 20)
    private String msgStatus = "new";

    // 已讀狀態（admin 視角）
    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // 回覆內容
    @Column(name = "reply_content", columnDefinition = "TEXT")
    private String replyContent;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    // 會員是否已讀回覆
    @Column(name = "is_reply_read")
    private Boolean isReplyRead = false;

    @Column(name = "reply_read_at")
    private LocalDateTime replyReadAt;

    // 回覆的 admin ID
    @Column(name = "admin_id")
    private Long adminId;

    // keyword filter：命中的關鍵字記錄（預留）
    @Column(name = "flagged_keywords", length = 255)
    private String flaggedKeywords;

    // ✅ 新增：Keyword Filter 標記（資料表已手動新增此欄位）
    @Column(name = "is_flagged")
    private Boolean isFlagged = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 附件（一對多）
    @OneToMany(mappedBy = "contactMsg", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContactMsgAttachment> attachments;
}

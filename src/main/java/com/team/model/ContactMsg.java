package com.team.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// ==========================================
// 📩 同學 E (Joyce) 負責區域：客服留言實體
// ==========================================
@Entity
@Table(name = "contact_msg")
public class ContactMsg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "msg_id")
    private Long msgId;

    @Column(name = "member_id")
    private Long memberId; // 如果是會員登入狀態，就會填入這個

    @Column(name = "guest_name")
    private String guestName; // 如果是訪客(未登入)，就填這個

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "order_id")
    private Long orderId; // 關聯您的訂單

    @Column(name = "msg_status")
    private String msgStatus = "new"; // 預設狀態為 new (未處理)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // =====================================
    // 請使用 IDE 自動產生 Getter 和 Setter
    // =====================================
    public Long getMsgId() { return msgId; }
    public void setMsgId(Long msgId) { this.msgId = msgId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getMsgStatus() { return msgStatus; }
    public void setMsgStatus(String msgStatus) { this.msgStatus = msgStatus; }
}
package com.team.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "porder") // 對應剛剛 SQL 裡的 porder 表
public class Porder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId; // 訂單編號 (PK) 注意：這裡配合資料庫改成了 Long

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 誰買的 (FK，對應 member 表)

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate; // 下單時間

    @Column(name = "total_amount", nullable = false, precision = 38, scale = 2)
    private BigDecimal totalAmount; // 訂單總金額 (配合資料庫 DECIMAL 格式)

    @Column(name = "delivery_method", nullable = false)
    private String deliveryMethod; // 配送方式

    @Column(name = "payment_type", nullable = false)
    private String paymentType; // 付款方式

    @Column(name = "status", nullable = false)
    private String status; // 訂單狀態

    // =====================================
    // Getters and Setters
    // =====================================
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
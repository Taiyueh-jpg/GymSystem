package com.team.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

//==========================================
//📦 同學 E 負責區域：訂單與明細
//==========================================

@Entity
@Table(name = "porder")
public class Porder {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long orderId;
 
 @Column(nullable = false)
 private Long memberId; // 哪位會員買的
 
 @Column(nullable = false)
 private LocalDateTime orderDate;
 
 @Column(nullable = false)
 private String status; // PENDING (處理中), SHIPPED (已出貨), COMPLETED (已完成)
 
 @Column(nullable = false)
 private String paymentType; // CREDIT_CARD, LINE_PAY, TRANSFER
 
 @Column(nullable = false)
 private String deliveryMethod; // STORE_PICKUP (店取), CVS (超商), HOME_DELIVERY (宅配)
 
 @Column(nullable = false)
 private BigDecimal totalAmount; // 總金額

 // Getters and Setters...
 public Long getOrderId() { return orderId; }
 public void setOrderId(Long orderId) { this.orderId = orderId; }
 public Long getMemberId() { return memberId; }
 public void setMemberId(Long memberId) { this.memberId = memberId; }
 public LocalDateTime getOrderDate() { return orderDate; }
 public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
 public String getStatus() { return status; }
 public void setStatus(String status) { this.status = status; }
 public String getPaymentType() { return paymentType; }
 public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
 public String getDeliveryMethod() { return deliveryMethod; }
 public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }
 public BigDecimal getTotalAmount() { return totalAmount; }
 public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}

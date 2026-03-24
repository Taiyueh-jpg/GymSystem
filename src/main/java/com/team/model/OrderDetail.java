package com.team.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

//==========================================
//📦 同學 E 負責區域：訂單與明細
//==========================================

@Entity
@Table(name = "orderdetail")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long detailId;
    
    @Column(nullable = false)
    private Long orderId; // 關聯 Porder
    
    @Column(nullable = false)
    private Long productId; // 買了什麼商品
    
    @Column(nullable = false)
    private Integer quantity; // 數量
    
    @Column(nullable = false)
    private BigDecimal unitPrice; // 購買當下的單價

    // Getters and Setters...
    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
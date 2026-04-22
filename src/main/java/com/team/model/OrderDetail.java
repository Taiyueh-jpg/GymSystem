package com.team.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orderdetail") // 對應 SQL 裡的 orderdetail 表
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId; // 明細編號 (PK)

    @Column(name = "order_id", nullable = false)
    private Long orderId; // 屬於哪張訂單？(FK)

    @Column(name = "product_id", nullable = false)
    private Long productId; // 買了什麼商品？(FK)

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // 數量

    @Column(name = "unit_price", nullable = false, precision = 38, scale = 2)
    private BigDecimal unitPrice; // 購買當下的單價 (配合資料庫 DECIMAL 格式)

    // =====================================
    // Getters and Setters
    // =====================================
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
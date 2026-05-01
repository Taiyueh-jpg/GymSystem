package com.team.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity	
@Table(name = "orderdetail") // 對應 SQL 裡的 orderdetail 表
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId; // 明細編號 (PK)

    // 🌟 第一個映射 (主要)：負責實際寫入、更新資料庫的欄位
    @Column(name = "order_id", nullable = false)
    private Long orderId; // 屬於哪張訂單？(FK)

    @Column(name = "product_id", nullable = false)
    private Long productId; // 買了什麼商品？(FK)

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // 數量

    @Column(name = "unit_price", nullable = false, precision = 38, scale = 2)
    private BigDecimal unitPrice; // 購買當下的單價 (配合資料庫 DECIMAL 格式)
    
    // 🚀 企業級防禦：
    // 1. @JsonIgnore：斬斷 "訂單找明細，明細又找回訂單" 的 JSON 轉換無窮迴圈
    // 2. insertable=false, updatable=false：告訴 JPA 此關聯僅供查詢，避免與上方的 Long orderId 衝突
    @ManyToOne(fetch = FetchType.LAZY) // 優化效能：設定為延遲載入
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    @JsonIgnore 
    private Porder porder;

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
    
    // 記得補上 Porder 的 Getter 與 Setter
    public Porder getPorder() { return porder; }
    public void setPorder(Porder porder) { this.porder = porder; }
}
	package com.team.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

// ==========================================
// 🛒 同學 C 負責區域：線上商城
// ==========================================

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "pname", nullable = false)
    private String pname;

    @Column(name = "price", nullable = false, precision = 38, scale = 2)
    private BigDecimal price;

    // 儲存圖片的 Base64 字串，使用 LONGTEXT 支援大圖
    @Lob
    @Column(name = "image_base64", columnDefinition = "LONGTEXT")
    private String imageBase64;
    
    // 目前 db/gymsystem.sql 的 product 表尚未建立 stock 欄位。
    // 先作為前端展示用欄位，避免 JPA 查詢不存在欄位導致 /api/products 500。
    @Transient
    private Integer stock = 0; // 商品庫存數量
    
 

    // =====================================
    // Getters and Setters
    // =====================================
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getPname() { return pname; }
    public void setPname(String pname) { this.pname = pname; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    
    // 目前 db/gymsystem.sql 的 product 表尚未建立 category 欄位。
    // 商城前端會依商品名稱推導分類，等資料表補欄位後可改回 @Column。
    @Transient
    private String category;
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
	public Integer getStock() {
		return stock;
	}
	public void setStock(Integer stock) {
		this.stock = stock;
	}	

	
}

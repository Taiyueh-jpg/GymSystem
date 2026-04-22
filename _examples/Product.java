package com.team.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

//==========================================
//🛒 同學 D 負責區域：商品商城
//==========================================

@Entity
@Table(name = "product")
public class Product {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long productId;
 
 @Column(nullable = false)
 private String pname; // 商品名稱
 
 @Column(nullable = false)
 private BigDecimal price; // 價格 (使用 BigDecimal 處理金錢最精確)
 
 // 儲存商品圖片 (Base64 格式)，使用 LONGTEXT 支援大圖檔
 @Lob 
 @Column(columnDefinition = "LONGTEXT")
 private String imageBase64; 

 // Getters and Setters...
 public Long getProductId() { return productId; }
 public void setProductId(Long productId) { this.productId = productId; }
 public String getPname() { return pname; }
 public void setPname(String pname) { this.pname = pname; }
 public BigDecimal getPrice() { return price; }
 public void setPrice(BigDecimal price) { this.price = price; }
 public String getImageBase64() { return imageBase64; }
 public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}
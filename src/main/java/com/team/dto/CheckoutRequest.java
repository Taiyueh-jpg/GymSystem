package com.team.dto;

import java.util.List;
import com.team.model.Porder;
import com.team.model.OrderDetail;

/**
 * 📦 結帳資料傳輸物件 (Checkout Request DTO)
 * 負責接收前端 AngularJS 傳遞的無狀態結帳 JSON 負載。
 * 包含訂單主檔資訊 (Porder) 與 訂單明細列表 (List<OrderDetail>)。
 * 本類別嚴格遵守 JDK 21 規範與 DTO (Data Transfer Object) 設計模式。
 */
public class CheckoutRequest {

    // 訂單主檔資訊 (包含 MemberId, DeliveryMethod, PaymentType 等)
    private Porder porder;
    
    // 訂單商品明細列表 (從前端購物車直接傳過來的陣列)
    private List<OrderDetail> details;

    // =====================================
    // Getters and Setters (供 Spring Boot 核心框架自動綁定 JSON 使用)
    // =====================================
    
    public Porder getPorder() { 
        return porder; 
    }
    
    public void setPorder(Porder porder) { 
        this.porder = porder; 
    }
    
    public List<OrderDetail> getDetails() { 
        return details; 
    }
    
    public void setDetails(List<OrderDetail> details) { 
        this.details = details; 
    }
}
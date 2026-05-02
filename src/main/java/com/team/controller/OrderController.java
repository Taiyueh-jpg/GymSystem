package com.team.controller;

import java.util.List; // ✅ 補上 List 的匯入
import java.util.Map;  // ✅ 補上 Map 的匯入

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.dto.CheckoutRequest;
import com.team.model.OrderDetail; // ✅ 補上 OrderDetail 的匯入
import com.team.model.Porder;
import com.team.service.OrderService;

@RestController
@RequestMapping("/api/orders")
// 允許跨域請求
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * POST 請求：處理結帳 (無狀態架構完美版)
     * 網址為：http://localhost:8080/api/orders/checkout
     */
    @PostMapping("/checkout")
    public Porder checkout(@RequestBody CheckoutRequest request) {
        
        // 【無狀態架構修正】直接從前端傳來的 JSON (request) 中獲取 MemberId
        Long memberId = request.getPorder().getMemberId();

        // 防呆機制
        if (memberId == null) {
            throw new RuntimeException("結帳失敗：無法識別會員身分 (前端未提供 Member ID)");
        }

        // 確保訂單主檔綁定正確的會員 ID
        request.getPorder().setMemberId(memberId);

        // 執行結帳邏輯
        return orderService.processCheckout(request.getPorder(), request.getDetails());
    }
        
    /**
     * GET 請求：查詢特定會員的歷史訂單
     * 網址範例：http://localhost:8080/api/orders/member/1
     */
    @GetMapping("/member/{memberId}")
    public List<Porder> getMemberHistory(@PathVariable Long memberId) {
        return orderService.getMemberOrderHistory(memberId);
    }

    /**
     * GET 請求：查詢單筆訂單的詳細商品內容
     * 網址範例：http://localhost:8080/api/orders/4/details
     */
    @GetMapping("/{orderId}/details")
    public List<OrderDetail> getOrderDetails(@PathVariable Long orderId) {
        return orderService.getOrderDetails(orderId);
    }

    /**
     * PUT 請求：修改訂單狀態 (後台管理員使用)
     * 網址範例：http://localhost:8080/api/orders/4/status
     */
    @PutMapping("/{orderId}/status")
    public Porder updateStatus(@PathVariable Long orderId, @RequestBody Map<String, String> request) {
        // 從前端傳來的 JSON 中取出 "status" 的值
        String newStatus = request.get("status");
        
        // 呼叫 Service 執行更新
        return orderService.updateOrderStatus(orderId, newStatus);
    }

    /**
     * GET 請求：查詢全公司所有訂單 (後台管理員專用)
     * 網址範例：http://localhost:8080/api/orders/all
     */
    @GetMapping("/all")
    public List<Porder> getAllOrders() {
        // TODO: 未來這裡可以加上「權限檢查」，確認當前登入的人是不是 Admin
        return orderService.getAllOrders();
    }
    
    /**
     * PUT 請求：編輯訂單明細 (單一數量修改)
     * 網址範例：http://localhost:8080/api/orders/16/details
     */
    @PutMapping("/{orderId}/details")
    public ResponseEntity<?> updateDetails(@PathVariable Long orderId, @RequestBody List<OrderDetail> updatedDetails) {
        try {
            orderService.updateOrderDetails(orderId, updatedDetails);
            return ResponseEntity.ok().body(Map.of("message", "訂單明細更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
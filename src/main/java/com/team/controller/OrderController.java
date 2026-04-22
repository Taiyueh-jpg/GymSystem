package com.team.controller;

import com.team.dto.CheckoutRequest;
import com.team.model.Member;
import com.team.model.OrderDetail;
import com.team.model.Porder;
import com.team.service.OrderService;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin // 允許跨域請求，這對前端同學 (VS Code 派) 開發非常重要！
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * POST 請求：處理結帳
     * 網址為：http://localhost:8080/api/orders/checkout
     */
    @PostMapping("/checkout")
    public Porder checkout(@RequestBody CheckoutRequest request, HttpSession session) {
        
        // 1. 從 Session 中拿出剛剛登入的會員資料 (芳羽存進去的)
        Member currentMember = (Member) session.getAttribute("loggedInMember");

        // 2. 防呆機制：如果有人沒登入就想硬偷結帳，直接擋下來！
        if (currentMember == null) {
            throw new RuntimeException("請先登入會員才能結帳喔！");
        }

        // 3. 核心魔法：把這筆訂單的 Member ID，強制設定為「現在登入的這個人」
        request.getPorder().setMemberId(currentMember.getMemberId());

        // 4. 呼叫 Service 執行包含 Transaction 的結帳邏輯 (原本您寫好的那段)
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

}


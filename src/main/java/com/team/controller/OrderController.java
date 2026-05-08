package com.team.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.dto.CheckoutRequest;
import com.team.model.OrderDetail;
import com.team.model.Porder;
import com.team.service.OrderService;
import com.team.util.ECPayUtil;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {
    "http://localhost:8080",
    "http://localhost:5500",
    "http://127.0.0.1:5500",
    "https://malka-unfeared-oronasally.ngrok-free.dev"
}, allowCredentials = "true", allowedHeaders = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ECPayUtil ecpayUtil;

    /**
     * 結帳端點
     *
     * 修正說明：
     * 原本回傳含 <script> 的 HTML 字串（ecpayForm），
     * 會被瀏覽器 CSP 擋掉 inline script，導致 form 無法自動送出。
     *
     * 改為：回傳 ecpayParams（Map<String,String>），
     * 由前端 AngularJS 動態建立 form 並送出至綠界，避開 CSP 限制。
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
        Long memberId = request.getPorder().getMemberId();
        if (memberId == null) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "結帳失敗：無法識別會員身分 (前端未提供 Member ID)")
            );
        }

        request.getPorder().setMemberId(memberId);
        Porder savedOrder = orderService.processCheckout(request.getPorder(), request.getDetails());

        String paymentType = savedOrder.getPaymentType();
        if ("LINE Pay".equals(paymentType) || "信用卡".equals(paymentType)) {
            // ✅ 改為回傳參數 Map，不再回傳含 <script> 的 HTML
            Map<String, String> ecpayParams = ecpayUtil.generateECPayParams(
                savedOrder.getOrderId(),
                savedOrder.getTotalAmount().intValue(),
                "GymSystem_Order_No_" + savedOrder.getOrderId()
            );
            return ResponseEntity.ok(Map.of(
                "status", "ECPAY",
                "ecpayParams", ecpayParams,   // 前端用這個動態建 form
                "message", "訂單建立成功，即將導向綠界金流..."
            ));
        }

        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "order", savedOrder,
            "message", "訂單建立成功！"
        ));
    }

    /**
     * 綠界非同步回調（Server 端通知）
     * 綠界付款完成後，會 POST 到此端點通知結果
     */
    @PostMapping("/ecpay/callback")
    public String ecpayCallback(@RequestParam Map<String, String> payload) {
        String rtnCode = payload.get("RtnCode");
        String merchantTradeNo = payload.get("MerchantTradeNo");
        if ("1".equals(rtnCode)) {
            try {
                String orderIdStr = merchantTradeNo.substring(3, merchantTradeNo.indexOf("T"));
                Long orderId = Long.parseLong(orderIdStr);
                orderService.updateOrderStatus(orderId, "paid");
                System.out.println("✅ 綠界金流回報：訂單編號 #" + orderId + " 已成功付款！狀態更新為 paid。");
            } catch (Exception e) {
                System.err.println("❌ 綠界回傳資料解析失敗：" + e.getMessage());
            }
        }
        return "1|OK";
    }

    @GetMapping("/member/{memberId}")
    public List<Porder> getMemberHistory(@PathVariable Long memberId) {
        return orderService.getMemberOrderHistory(memberId);
    }

    @GetMapping("/{orderId}/details")
    public List<OrderDetail> getOrderDetails(@PathVariable Long orderId) {
        return orderService.getOrderDetails(orderId);
    }

    @PutMapping("/{orderId}/status")
    public Porder updateStatus(@PathVariable Long orderId, @RequestBody Map<String, String> request) {
        String newStatus = request.get("status");
        return orderService.updateOrderStatus(orderId, newStatus);
    }

    @GetMapping("/all")
    public List<Porder> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{orderId}/details")
    public ResponseEntity<?> updateDetails(
            @PathVariable Long orderId,
            @RequestBody List<OrderDetail> updatedDetails) {
        try {
            orderService.updateOrderDetails(orderId, updatedDetails);
            return ResponseEntity.ok().body(Map.of("message", "訂單明細更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.ok().body(Map.of("message", "刪除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

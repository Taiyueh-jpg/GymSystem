package com.team.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.team.service.PtOrderService;
import com.team.service.ReservationService;

@RestController
@RequestMapping({"/pt-orders", "/api/pt-orders"})
@CrossOrigin
public class PtOrderController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private PtOrderService ptOrderService;

    // 查會員剩餘私教堂數
    @GetMapping("/member/{memberId}/remaining-sessions")
    public ResponseEntity<?> getMemberRemainingSessions(@PathVariable Long memberId) {
        Map<String, Object> result = reservationService.getMemberRemainingPtSessions(memberId);

        if (result == null) {
            return new ResponseEntity<>("找不到會員資料", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 查會員私教購買紀錄
    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> getMemberPtOrders(@PathVariable Long memberId) {
        if (memberId == null) {
            return new ResponseEntity<>("memberId 不可為空", HttpStatus.BAD_REQUEST);
        }

        Object result = ptOrderService.findOrdersByMemberId(memberId);

        if (result instanceof String) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 購買私教方案
    @PostMapping("/purchase")
    public ResponseEntity<?> purchasePackage(@RequestBody Map<String, Object> request) {
        if (request == null) {
            return new ResponseEntity<>("request body 不可為空", HttpStatus.BAD_REQUEST);
        }

        if (!request.containsKey("memberId") || request.get("memberId") == null
                || request.get("memberId").toString().trim().isEmpty()) {
            return new ResponseEntity<>("memberId 不可為空", HttpStatus.BAD_REQUEST);
        }

        if (!request.containsKey("packageId") || request.get("packageId") == null
                || request.get("packageId").toString().trim().isEmpty()) {
            return new ResponseEntity<>("packageId 不可為空", HttpStatus.BAD_REQUEST);
        }

        Long memberId;
        Long packageId;

        try {
            memberId = Long.valueOf(request.get("memberId").toString());
            packageId = Long.valueOf(request.get("packageId").toString());
        } catch (Exception e) {
            return new ResponseEntity<>("memberId 與 packageId 必須為數字", HttpStatus.BAD_REQUEST);
        }

        Object result = ptOrderService.purchasePackage(memberId, packageId);

        if (result instanceof String) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

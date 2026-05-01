package com.team.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.team.model.Member;
import com.team.service.MemberService;

/**
 * 🌐 會員控制層 (Member Controller) - 百萬顧問重構版 (Stateless REST API)
 */
@RestController // ✅ 關鍵修正：必須是 RestController 才會回傳 JSON
@RequestMapping("/api/members") // ✅ 統一使用複數路徑
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<?> handleLogin(@RequestBody Map<String, String> loginData) { 
        // ✅ 關鍵修正：改用 @RequestBody 接收前端 AngularJS 傳來的 JSON
        String email = loginData.get("email");
        String password = loginData.get("password");

        Member loginMember = memberService.login(email, password);
        
        if (loginMember != null) {
            // 💡 安全性規範：回傳給前端前，務必抹除密碼
            loginMember.setPassword(null);
            return ResponseEntity.ok(loginMember); 
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "帳號或密碼錯誤")); 
        }
    }
}
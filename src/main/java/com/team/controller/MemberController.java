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
@RestController 
@RequestMapping("/api/members") 
// 🔥 終極殺手鐧：將 Ngrok 網址直接綁定，徹底解決 Invalid CORS request
@CrossOrigin(origins = {
    "http://localhost:8080",
    "http://localhost:5500", 
    "http://127.0.0.1:5500", 
    "https://malka-unfeared-oronasally.ngrok-free.dev"
}, allowCredentials = "true", allowedHeaders = "*")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<?> handleLogin(@RequestBody Map<String, String> loginData) { 
        
        // 加上這行監控指令
        System.out.println("🚨 成功穿越攔截器！收到登入請求，信箱：" + loginData.get("email"));
        
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
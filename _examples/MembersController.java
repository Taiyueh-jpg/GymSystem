package com.team.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // 🌟 改成普通的 Controller，才能做畫面跳轉
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.team.model.Member;
import com.team.model.MemberRegisterDTO;
import com.team.service.MemberService;
import jakarta.servlet.http.HttpSession; // 🌟 記得載入 Session

@Controller  // ⚠️ 這裡很重要，不要用 RestController
@RequestMapping("/api/members")
public class MembersController {

    @Autowired
    private MemberService memberService;

    // ==========================================
    // 1. 處理「註冊」的 POST 請求
    // ==========================================
    @PostMapping("/register")
    public String handleRegister(MemberRegisterDTO registerData) {
        memberService.registerNewMember(registerData);
        // 註冊成功後，自動導向回登入頁面 (可以加上一點參數讓前端顯示成功訊息)
        return "redirect:/member/login?registerSuccess=true";
    }

    // ==========================================
    // 2. 處理「登入」的 POST 請求 (🌟 關鍵合體點)
    // ==========================================
    @PostMapping("/login")
    public String handleLogin(@RequestParam String email, 
                              @RequestParam String password, 
                              HttpSession session) { // 👈 加入 Session 參數
        
        Member loginMember = memberService.login(email, password);
        
        if (loginMember != null) {
            // 🌟 魔法合體！把登入成功的會員存進 Session (您的訂單模組就是抓這個！)
            session.setAttribute("loggedInMember", loginMember);
            
            // 登入成功，直接跳轉到信穎的商城頁面讓客人血拚！
            return "redirect:/product/mall"; 
        } else {
            // 登入失敗，跳轉回登入頁，並附帶 error 參數
            return "redirect:/member/login?error=true";
        }
    }
}
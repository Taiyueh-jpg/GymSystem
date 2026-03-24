package com.team.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.model.Member;
import com.team.model.MemberRegisterDTO;
import com.team.service.MemberService;

@RestController
@RequestMapping("/api/members") // 這個路徑對應到 login.html 表單的 action="/api/members/..."
public class MembersController {

    // 🌟 把 Service 注入進來 (聘請經理幫我們處理資料庫邏輯)
    @Autowired
    private MemberService memberService;

    // ==========================================
    // 1. 處理「註冊」的 POST 請求
    // ==========================================
    @PostMapping("/register")
    public String handleRegister(MemberRegisterDTO registerData) {
        
        System.out.println("=====================================");
        System.out.println("🎉 收到新會員註冊請求！");
        System.out.println("姓名：" + registerData.getName());
        System.out.println("信箱：" + registerData.getEmail());
        System.out.println("=====================================");
        
        // 呼叫 Service 執行儲存動作
        memberService.registerNewMember(registerData);
        
        return "註冊成功！資料已經存入資料庫，歡迎加入 GymSystem：" + registerData.getName();
    }

    // ==========================================
    // 2. 處理「登入」的 POST 請求
    // ==========================================
    @PostMapping("/login")
    public String handleLogin(@RequestParam String email, @RequestParam String password) {
        
        System.out.println("=====================================");
        System.out.println("🔑 收到登入請求，嘗試登入信箱：" + email);
        System.out.println("=====================================");
        
        // 呼叫 Service 幫我們核對帳密
        Member loginMember = memberService.login(email, password);
        
        // 判斷回傳的會員資料是不是空的 (null 代表找不到人或密碼錯)
        if (loginMember != null) {
            return "登入成功！歡迎回來，" + loginMember.getName();
        } else {
            return "登入失敗！請檢查您的電子信箱或密碼是否正確。";
        }
    }
}
package com.team.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 👑 同學 A 負責區域：會員前端介面路由
 * 規範：所有 UI 介面類別檔案名稱後端必須加上 'UI' 字樣
 */
@Controller
@RequestMapping("/member")
public class MemberUI {

    // 導向登入頁面 -> 會去尋找 src/main/resources/templates/auth/login.html
    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login"; 
    }

    // 導向註冊頁面 -> 會去尋找 src/main/resources/templates/auth/register.html
    @GetMapping("/register")
    public String showRegisterPage() {
        return "auth/register";
    }
    
    // 導向會員中心頁面
    @GetMapping("/profile")
    public String showProfilePage() {
        return "member/profile";
    }
}
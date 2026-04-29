package com.team.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.team.model.Member;
import com.team.model.MemberRegisterDTO;
import com.team.service.MemberService;

import jakarta.servlet.http.HttpSession;

/**
 * 🌐 會員控制層 (Member Controller)
 */
@Controller // 👈 必須是 Controller，因為我們要回傳畫面
public class MemberController {

    @Autowired
    private MemberService memberService;

    // 首頁
    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    // 登入頁面
    @GetMapping("/member/login")
    public String showLoginPage() {
        return "member/login"; 
    }

    // 註冊頁面
    @GetMapping("/member/register")
    public String showRegisterPage() {
        return "member/register";
    }
    
    // 會員中心頁面
    @GetMapping("/member/profile")
    public String showProfilePage() {
        return "member/profile";
    }

    @PostMapping("/api/members/register")
    public String handleRegister(MemberRegisterDTO registerData) {
        memberService.registerNewMember(registerData);
        return "redirect:/member/login?registerSuccess=true";
    }

    @PostMapping("/api/members/login")
    public String handleLogin(@RequestParam String email, 
                              @RequestParam String password, 
                              HttpSession session) { 
        
        Member loginMember = memberService.login(email, password);
        
        if (loginMember != null) {
            session.setAttribute("loggedInMember", loginMember);
            return "redirect:/product/mall"; 
        } else {
            return "redirect:/member/login?error=true";
        }
    }
}
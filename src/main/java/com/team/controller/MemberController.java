package com.team.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; 
import org.springframework.web.bind.annotation.GetMapping; // 🌟 新增
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.team.model.Member;
import com.team.model.MemberRegisterDTO;
import com.team.service.MemberService;
import jakarta.servlet.http.HttpSession;

@Controller  
// ⚠️ 注意！這裡把原本的 @RequestMapping("/api/members") 拿掉，
// 因為我們要同時處理根目錄 ("/") 和 "/api/members/"
public class MemberController {

    @Autowired
    private MemberService memberService;

    // ============================================================
    // 🌟 第一部分：取代原本 MainUI 和 MemberUI 的「畫面導向」功能
    // ============================================================

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

    // ============================================================
    // 第二部分：原本處理登入/註冊資料的 API 功能 (記得把 /api/members 補回去)
    // ============================================================

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
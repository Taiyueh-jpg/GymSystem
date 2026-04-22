package com.team.controller;

import com.team.model.Member;
import com.team.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/members")
@CrossOrigin
public class MemberController {

    @Autowired
    private MemberService memberService;

    /**
     * 註冊 API
     * POST /api/members/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Member member) {
        try {
            Member newMember = memberService.register(member);
            return ResponseEntity.ok(newMember);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 登入 API (最關鍵！負責發放 Session 通行證)
     * POST /api/members/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");

            Member member = memberService.login(email, password);
            
            // 🌟 核心魔法：把登入成功的會員資料，存進這台機器的 Session 記憶體中！
            session.setAttribute("loggedInMember", member);

            return ResponseEntity.ok("登入成功！歡迎：" + member.getName());
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    /**
     * 登出 API
     * POST /api/members/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        // 把 Session 清空，銷毀通行證
        session.invalidate();
        return ResponseEntity.ok("已成功登出");
    }

    /**
     * 檢查當前登入狀態 (讓前端網頁知道現在是誰登入)
     * GET /api/members/current
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentMember(HttpSession session) {
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return ResponseEntity.status(401).body("目前尚未登入");
        }
        return ResponseEntity.ok(member);
    }
}
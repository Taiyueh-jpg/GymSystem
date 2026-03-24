package com.team.controller;

import com.team.model.Member;
import com.team.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 👑 同學 A 負責區域：會員 RESTful API
 * 規範：網址使用「複數名詞 (加 s)」 -> /api/members
 */
@RestController
@RequestMapping("/api/members")
public class MembersController {

    private final MemberService memberService;

    public MembersController(MemberService memberService) {
        this.memberService = memberService;
    }

    // JDK 21 Feature: 使用 Record 作為接收登入資訊的 DTO
    public record LoginRequest(String email, String password) {}

    /**
     * 處理會員登入請求 (POST /api/members/login)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            Member member = memberService.login(request.email(), request.password());
            // 登入成功，將會員資訊存入 Session
            session.setAttribute("loginMember", member);
            return ResponseEntity.ok(member);
        } catch (RuntimeException e) {
            // 登入失敗，回傳 400 Bad Request 與錯誤訊息
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * 處理會員註冊請求 (POST /api/members)
     */
    @PostMapping
    public ResponseEntity<?> register(@RequestBody Member member) {
        try {
            Member newMember = memberService.register(member);
            return ResponseEntity.ok(newMember);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    /**
     * 處理會員登出 (POST /api/members/logout)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); // 清除 Session
        return ResponseEntity.ok("登出成功");
    }
}
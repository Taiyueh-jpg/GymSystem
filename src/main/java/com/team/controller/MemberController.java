package com.team.controller;

import com.team.model.Member;
import com.team.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

/**
 * 🌐 會員控制層 (Member Controller)
 * * 技術細節：
 * 1. 遵循 RESTful API 設計規範。
 * 2. 使用 ResponseEntity 封裝回傳結果與 HTTP 狀態碼。
 * 3. 實作跨域處理 (@CrossOrigin) 以便與 VSCode 前端對接。
 */
@RestController
@RequestMapping("/api/member")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
public class MemberController {

    @Autowired
    private MemberService memberService;

    // ==========================================
    // 🔓 訪客與一般會員功能 (Guest & Member)
    // ==========================================

    /**
     * 📝 會員註冊
     * 路徑：POST /api/member/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Member member) {
        try {
            Member registeredMember = memberService.register(member);
            return new ResponseEntity<>(registeredMember, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // 回傳 400 Bad Request 與錯誤訊息 (例如：Email 已重複)
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 🔑 會員登入
     * 路徑：POST /api/member/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");
            Member member = memberService.login(email, password);
            
            // 👉 【新增資安防護】：登入成功，將會員資料存入 Session (發放通行證)
            session.setAttribute("loggedInMember", member);
            
            return ResponseEntity.ok(member);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }
    
    /**
     * 🚪 會員登出 (銷毀 Session)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); // 銷毀通行證
        return ResponseEntity.ok(Map.of("message", "已成功登出"));
    }
    
 // ✅ joyce新增 ↓↓↓
    /**
     * 👤 取得目前登入的會員資訊
     * 路徑：GET /api/member/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpSession session) {
        Member member = (Member) session.getAttribute("loggedInMember");
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "尚未登入"));
        }
        return ResponseEntity.ok(member);
    }

    /**
     * 👤 取得個人資料 (個人中心)
     * 路徑：GET /api/member/profile/{id}
     */
    @GetMapping("/profile/{id}")
    public ResponseEntity<Member> getProfile(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(memberService.getMemberProfile(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 📝 更新個人資料
     * 路徑：PUT /api/member/profile/{id}
     */
    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody Member updatedInfo) {
        try {
            Member result = memberService.updateProfile(id, updatedInfo);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==========================================
    // 🛡️ 後台管理與行銷功能 (Admin & Marketing)
    // ==========================================

    /**
     * 🔍 關鍵字搜尋會員 (支援分頁)
     * 路徑：GET /api/member/search?keyword=xxx&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Member>> searchMembers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(memberService.searchMembers(keyword, pageable));
    }

    /**
     * 🎂 查詢本月壽星 (行銷活動)
     * 路徑：GET /api/member/birthdays
     */
    @GetMapping("/birthdays")
    public ResponseEntity<List<Member>> getBirthdays() {
        return ResponseEntity.ok(memberService.getBirthdaysOfCurrentMonth());
    }

    /**
     * 📊 取得有效會員總數 (戰情室數據)
     * 路徑：GET /api/member/active-count
     */
    @GetMapping("/active-count")
    public ResponseEntity<Long> getActiveCount() {
        return ResponseEntity.ok(memberService.getActiveMemberCount());
    }

    /**
     * 🚫 更新會員狀態 (停權/復權)
     * 路徑：PUT /api/member/status-update/{id}
     */
    @PutMapping("/status-update/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> statusData) {
        Integer newStatus = statusData.get("status");
        memberService.updateMemberStatus(id, newStatus);
        return ResponseEntity.ok(Map.of("message", "狀態已成功更新為 " + newStatus));
    }
}
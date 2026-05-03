package com.team.controller;

import com.team.model.Admin;
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
 * 專家優化版：全面防堵 IDOR (越權存取) 漏洞，嚴格區分「會員」與「管理員」的職責。
 */
@RestController
@RequestMapping("/api/member") // ⚠️ 拒絕 HEAD 的 /members 改動，維持單數以確保前端 app.js 正常運作
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
public class MemberController {

    @Autowired
    private MemberService memberService;

    // ==========================================
    // 🔓 訪客與一般會員功能 (Guest & Member)
    // ==========================================

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Member member) {
        try {
            Member registeredMember = memberService.register(member);
            return new ResponseEntity<>(registeredMember, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");
            Member member = memberService.login(email, password);
            
            // 💡 拒絕 HEAD 的無狀態寫法，堅持將會員存入 Session 供攔截器使用
            session.setAttribute("loggedInMember", member);
            return ResponseEntity.ok(member);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); 
        return ResponseEntity.ok(Map.of("message", "已成功登出"));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id, HttpSession session) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");

        if (loggedInAdmin == null && (loggedInMember == null || !loggedInMember.getMemberId().equals(id))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "無權查看他人資料！"));
        }

        try {
            return ResponseEntity.ok(memberService.getMemberProfile(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody Member updatedInfo, HttpSession session) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");

        if (loggedInAdmin == null && (loggedInMember == null || !loggedInMember.getMemberId().equals(id))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "安管系統攔截：你只能修改自己的資料！"));
        }

        try {
            Member result = memberService.updateProfile(id, updatedInfo);
            if (loggedInMember != null && loggedInMember.getMemberId().equals(id)) {
                session.setAttribute("loggedInMember", result);
            }
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==========================================
    // 🛡️ 後台管理與行銷功能 (Admin Only)
    // ==========================================

    @GetMapping("/search")
    public ResponseEntity<Page<Member>> searchMembers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(memberService.searchMembers(keyword, pageable));
    }

    @GetMapping("/birthdays")
    public ResponseEntity<List<Member>> getBirthdays(HttpSession session) {
        if (session.getAttribute("loggedInAdmin") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        return ResponseEntity.ok(memberService.getBirthdaysOfCurrentMonth());
    }

    @GetMapping("/active-count")
    public ResponseEntity<Long> getActiveCount(HttpSession session) {
        if (session.getAttribute("loggedInAdmin") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        return ResponseEntity.ok(memberService.getActiveMemberCount());
    }

    @PutMapping("/status-update/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> statusData, HttpSession session) {
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "嚴重越權警告：您不是管理員，無法執行停權操作！"));
        }

        Integer newStatus = statusData.get("status");
        memberService.updateMemberStatus(id, newStatus);
        return ResponseEntity.ok(Map.of("message", "狀態已成功更新為 " + newStatus));
    }
}
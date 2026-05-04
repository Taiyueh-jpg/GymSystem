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
@RequestMapping("/api/member")
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
            // 白話文註解：呼叫 Service 進行註冊，若 Email 重複 Service 會拋出錯誤
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
            
            // 白話文註解：登入成功，將會員資料存入 Session 當作通行證
            session.setAttribute("loggedInMember", member);
            return ResponseEntity.ok(member);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        // 白話文註解：銷毀 Session，把通行證撕毀
        session.invalidate(); 
        return ResponseEntity.ok(Map.of("message", "已成功登出"));
    }
    
 // ✅ joyce新增
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

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id, HttpSession session) {
        // 🛡️ 資安防護：檢查是否有權限查看
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");

        // 白話文註解：如果沒有管理員登入，且登入的會員 ID 不等於想查詢的 ID，就拒絕他
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
        // 🛡️ 資安防護：防止越權修改 (IDOR)
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");

        // 白話文註解：只有「管理員」或是「本人」才可以修改這筆資料
        if (loggedInAdmin == null && (loggedInMember == null || !loggedInMember.getMemberId().equals(id))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "安管系統攔截：你只能修改自己的資料！"));
        }

        try {
            Member result = memberService.updateProfile(id, updatedInfo);
            
            // 白話文註解：如果是本人修改自己的資料，順便更新 Session 裡面的暫存資料
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
    public ResponseEntity<?> searchMembers(
            @RequestParam(defaultValue = "") String keyword, // ✅ 關鍵修復：這裡補上了 defaultValue = ""
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        
        // 🛡️ 資安防護：檢查是不是管理員
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "權限不足：僅限管理員操作"));
        }

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(memberService.searchMembers(keyword, pageable));
    }

    @GetMapping("/birthdays")
    public ResponseEntity<?> getBirthdays(HttpSession session) {
        // 🛡️ 資安防護：僅限管理員
        if (session.getAttribute("loggedInAdmin") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "權限不足：僅限管理員操作"));
        }
        return ResponseEntity.ok(memberService.getBirthdaysOfCurrentMonth());
    }

    @GetMapping("/active-count")
    public ResponseEntity<?> getActiveCount(HttpSession session) {
        // 🛡️ 資安防護：僅限管理員
        if (session.getAttribute("loggedInAdmin") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "權限不足：僅限管理員操作"));
        }
        return ResponseEntity.ok(memberService.getActiveMemberCount());
    }

    @PutMapping("/status-update/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> statusData, HttpSession session) {
        // 🛡️ 資安防護：防止一般會員把別人停權
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "嚴重越權警告：您不是管理員，無法執行停權操作！"));
        }

        Integer newStatus = statusData.get("status");
        memberService.updateMemberStatus(id, newStatus);
        return ResponseEntity.ok(Map.of("message", "狀態已成功更新為 " + newStatus));
    }
}
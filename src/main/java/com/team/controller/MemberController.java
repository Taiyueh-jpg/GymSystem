package com.team.controller;

import com.team.model.Admin;
import com.team.model.Member;
import com.team.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

/**
 * 🌐 會員控制層 (Member Controller)
 *
 * 本版重點：
 * 1. 保留原本註冊 / 登入 / 登出 / 會員資料 / 後台搜尋功能。
 * 2. 新增會員修改密碼 API。
 * 3. 修改密碼只允許本人操作，不允許管理員從會員中心直接代改密碼。
 */
@RestController
@RequestMapping("/api/member")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
public class MemberController {

    @Autowired
    private MemberService memberService;

    // ==========================================
    // 訪客與一般會員功能
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

    /**
     * 取得目前登入會員資料。
     * 行銷客服模組會用這支 API 判斷會員留言身分；未登入時回 401。
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpSession session) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");

        if (loggedInMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "尚未登入"));
        }

        return ResponseEntity.ok(loggedInMember);
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
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @RequestBody Member updatedInfo,
            HttpSession session) {

        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");

        if (loggedInAdmin == null && (loggedInMember == null || !loggedInMember.getMemberId().equals(id))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "安管系統攔截：你只能修改自己的資料！"));
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

    /**
     * 修改會員密碼。
     *
     * 權限規則：
     * 1. 必須是登入會員。
     * 2. 只能修改自己的密碼。
     * 3. 不允許管理員透過會員中心 API 代改會員密碼。
     *
     * Request body:
     * {
     *   "currentPassword": "目前密碼",
     *   "newPassword": "新密碼",
     *   "confirmPassword": "再次輸入新密碼"
     * }
     */
    @PutMapping("/password/{id}")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordData,
            HttpSession session) {

        Member loggedInMember = (Member) session.getAttribute("loggedInMember");

        if (loggedInMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "請先登入會員"));
        }

        if (!loggedInMember.getMemberId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "安管系統攔截：你只能修改自己的密碼！"));
        }

        try {
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            String confirmPassword = passwordData.get("confirmPassword");

            memberService.changePassword(id, currentPassword, newPassword, confirmPassword);

            Member updatedMember = memberService.getMemberProfile(id);
            session.setAttribute("loggedInMember", updatedMember);

            return ResponseEntity.ok(Map.of("message", "密碼修改成功，請使用新密碼登入。"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==========================================
    // 後台管理與行銷功能 Admin Only
    // ==========================================

    @GetMapping("/search")
    public ResponseEntity<?> searchMembers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {

        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");

        if (loggedInAdmin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "權限不足：僅限管理員操作"));
        }

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(memberService.searchMembers(keyword, pageable));
    }

    @GetMapping("/birthdays")
    public ResponseEntity<?> getBirthdays(HttpSession session) {
        if (session.getAttribute("loggedInAdmin") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "權限不足：僅限管理員操作"));
        }

        return ResponseEntity.ok(memberService.getBirthdaysOfCurrentMonth());
    }

    @GetMapping("/active-count")
    public ResponseEntity<?> getActiveCount(HttpSession session) {
        if (session.getAttribute("loggedInAdmin") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "權限不足：僅限管理員操作"));
        }

        return ResponseEntity.ok(memberService.getActiveMemberCount());
    }

    @PutMapping("/status-update/{id}")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> statusData,
            HttpSession session) {

        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");

        if (loggedInAdmin == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "嚴重越權警告：您不是管理員，無法執行停權操作！"));
        }

        Integer newStatus = statusData.get("status");

        memberService.updateMemberStatus(id, newStatus);

        return ResponseEntity.ok(Map.of("message", "狀態已成功更新為 " + newStatus));
    }
}
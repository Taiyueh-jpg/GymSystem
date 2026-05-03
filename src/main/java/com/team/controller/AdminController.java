package com.team.controller;

import com.team.model.Admin;
import com.team.service.AdminService;
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

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 🔑 管理員/教練登入
     * 路由：POST /api/admin/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        try {
            String email    = loginData.get("email");
            String password = loginData.get("password");
            Admin admin = adminService.login(email, password);

            session.setAttribute("loggedInAdmin", admin);
            return ResponseEntity.ok(admin);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 👤 取得目前登入的管理員/教練資訊
     * 路由：GET /api/admin/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpSession session) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "尚未登入，請先前往登入頁面"));
        }
        return ResponseEntity.ok(admin);
    }

    /**
     * ➕ 新增教練/管理員
     * 路由：POST /api/admin/staff
     */
    @PostMapping("/staff")
    public ResponseEntity<?> addStaff(@RequestBody Admin admin) {
        try {
            Admin newStaff = adminService.addStaff(admin);
            return new ResponseEntity<>(newStaff, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 🚪 員工登出
     * 路由：POST /api/admin/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); 
        return ResponseEntity.ok(Map.of("message", "員工已登出"));
    }

    /**
     * 👨‍🏫 取得所有在職教練名單 (供前台預約、或後台排課使用)
     * 路由：GET /api/admin/coaches
     */
    @GetMapping("/coaches")
    public ResponseEntity<List<Admin>> getActiveCoaches() {
        return ResponseEntity.ok(adminService.getActiveCoaches());
    }

    /**
     * 🚫 變更員工狀態 (離職/復職)
     * 路由：PUT /api/admin/status-update/{id}
     */
    @PutMapping("/status-update/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> statusData) {
        Integer newStatus = statusData.get("status");
        adminService.updateStaffStatus(id, newStatus);
        return ResponseEntity.ok(Map.of("message", "員工狀態已更新為 " + newStatus));
    }

    /**
     * 🚀 新增：取得員工清單 (後台管理員專用，支援分頁與模糊搜尋)
     * 路由：GET /api/admin/search
     *
     * 白話文：這個 API 專門用來回傳員工資料表 (admin) 的內容，
     * 並且加入了嚴格的權限控管，只有 role 是 "admin" 的人才能呼叫。
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchStaff(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
            
        // 🛡️ 資安防護：檢查是否有登入，且是否為最高權限的系統管理員 (admin)
        Admin loggedInAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin == null || !"admin".equals(loggedInAdmin.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "權限不足，僅限系統管理員查看員工清單"));
        }

        // 封裝分頁參數，交給 Service 處理
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminService.searchStaff(keyword, pageable));
    }
}
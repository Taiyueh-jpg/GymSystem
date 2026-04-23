package com.team.controller;

import com.team.model.Admin;
import com.team.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
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
            String email = loginData.get("email");
            String password = loginData.get("password");
            Admin admin = adminService.login(email, password);
            
            // 👉 【新增資安防護】：將管理員/教練資料存入 Session
            session.setAttribute("loggedInAdmin", admin);
            
            return ResponseEntity.ok(admin);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
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
     * 🚪 員工登出 (銷毀 Session)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); // 銷毀通行證
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
}
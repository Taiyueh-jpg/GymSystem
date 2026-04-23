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
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 🔑 管理員/教練登入
     * 路由：POST /api/admin/login
     *
     * 登入成功後，將完整的 Admin 物件存入 Session，key 為 "loggedInAdmin"。
     * 前端後續可透過 GET /api/admin/me 取得目前登入者的資訊（含 adminId）。
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        try {
            String email    = loginData.get("email");
            String password = loginData.get("password");
            Admin admin = adminService.login(email, password);

            // 👉 [資安防護] 將管理員/教練物件存入 Session，key = "loggedInAdmin"
            //    LoginInterceptor 會讀取此 key 來判斷是否已登入
            session.setAttribute("loggedInAdmin", admin);

            // ✅ 因為 Admin.password 已加 @JsonIgnore，回傳的 JSON 不含密碼
            return ResponseEntity.ok(admin);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 👤 取得目前登入的管理員/教練資訊
     * 路由：GET /api/admin/me
     *
     * ✅ [新增] 解決前端 article-admin.html 中 ADMIN_ID 寫死的問題。
     * 前端頁面載入時呼叫此 API：
     *   - 若已登入 → 回傳 Admin 物件（含 adminId、name、role 等），前端可動態取得 adminId
     *   - 若未登入 → 回傳 401，前端應導向登入頁
     *
     * 注意：此路由必須加入 WebConfig.addInterceptors() 的攔截名單 (.addPathPatterns)，
     *       才能讓 LoginInterceptor 在未登入時自動攔截並回傳 401。
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpSession session) {
        // 從 Session 取出登入時存入的 Admin 物件
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");

        // Session 中沒有資料 → 未登入
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "尚未登入，請先前往登入頁面"));
        }

        // ✅ 已登入 → 回傳 Admin 資訊（password 因 @JsonIgnore 不會洩漏）
        return ResponseEntity.ok(admin);
    }

    /**
     * ➕ 新增教練/管理員
     * 路由：POST /api/admin/staff
     * (為了測試方便，新增員工暫時列入 WebConfig 白名單，正式上線前應移除)
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
     * 路由：POST /api/admin/logout
     *
     * session.invalidate() 會刪除整個 Session（包含 loggedInAdmin、loggedInMember），
     * 下次請求會重新建立一個空白的 Session。
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); // 銷毀通行證
        return ResponseEntity.ok(Map.of("message", "員工已登出"));
    }

    /**
     * 👨‍🏫 取得所有在職教練名單 (供前台預約、或後台排課使用)
     * 路由：GET /api/admin/coaches
     *
     * 此路由已在 WebConfig 的攔截名單中，未登入無法存取。
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

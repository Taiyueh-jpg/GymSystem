package com.team.configure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// ==========================================
// 🛡️ 系統保全：無狀態 (Stateless) 登入攔截器
// ==========================================
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // ✅ 關鍵修正：不再依賴 HttpSession，改為檢查前端送來的 Header
        String authHeader = request.getHeader("Authorization");

        // 判斷是否有通行證
        if (authHeader == null || authHeader.isEmpty()) {
            
            if (request.getRequestURI().startsWith("/api/")) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"請先登入會員！\"}");
            } else {
                response.sendRedirect("/login.html"); 
            }
            return false; // 🛑 擋下
        }

        return true; // ✅ 放行
    }
}
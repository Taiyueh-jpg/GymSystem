package com.team.configure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// ==========================================
// 🛡️ 系統保全：檢查登入狀態的攔截器
// ==========================================
@Component
public class LoginInterceptor implements HandlerInterceptor {

    // preHandle 代表在請求抵達 Controller "之前" 會先執行這裡
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 1. 取得當前的 Session
        HttpSession session = request.getSession();
        Object member = session.getAttribute("loggedInMember");

        // 2. 判斷是否有通行證
        if (member == null) {
            // 🚨 沒登入的壞人被抓到了！
            
            // 判斷他是呼叫 API 還是看網頁
            if (request.getRequestURI().startsWith("/api/")) {
                // 如果是呼叫 API，回傳 401 未授權錯誤
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"請先登入會員！\"}");
            } else {
                // 如果是看網頁，就強制把他踢回首頁或登入頁
                response.sendRedirect("/product/mall"); // 暫時先踢回商城，等芳羽寫好 login.html 再改
            }
            
            return false; // 🛑 false 代表「擋下」，請求不會走到 Controller
        }

        return true; // ✅ true 代表「放行」，請進！
    }
}
package com.team.configure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 🛡️ 系統安全守衛：登入攔截器
 * 負責檢查每個受到保護的 API 請求，是否帶有合法的登入狀態 (Session)。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 💡 [非常重要] 放行 OPTIONS 請求：前端 VS Code 跨域 (CORS) 存取時，瀏覽器會先發送 OPTIONS 預檢請求，這個必須放行。
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 取得當前的 Session
        HttpSession session = request.getSession();
        
        // 檢查 Session 中是否有存放我們在 Controller 發放的「會員」或「管理員/教練」資料
        Object loggedInMember = session.getAttribute("loggedInMember");
        Object loggedInAdmin = session.getAttribute("loggedInAdmin");

        // 如果有任何一種身分登入，就開門放行
        if (loggedInMember != null || loggedInAdmin != null) {
            return true; 
        }

        // 🛑 如果都沒有登入，將他擋下並回傳 401 Unauthorized 錯誤
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\": \"安管系統攔截：請先登入後再進行操作！\"}");
        
        return false; // false 代表中止請求，不會進入你的 Controller
    }
}
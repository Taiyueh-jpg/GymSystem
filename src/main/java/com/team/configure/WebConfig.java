package com.team.configure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 🗺️ 系統設定：攔截器的管制範圍與白名單
 * 專家優化版：完美融合跨域設定與精準的權限分流 (會員 vs 管理員)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 保留同學的跨域設定，這對前端讀取 Session 非常重要
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://127.0.0.1:5500", 
                    "http://localhost:5500",
                    "http://localhost:8080"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // ─────────────────────────────────────────────────────
                // 🛑 僅限「一般會員」登入才能存取的 API（黑名單）
                // ─────────────────────────────────────────────────────
                .addPathPatterns(
                        "/api/member/me",
                        "/api/member/profile/**", 
                        "/api/orders/**" // 首席架構師滷蛋的訂單保護傘
                )
                
                // ─────────────────────────────────────────────────────
                // 🟢 完全開放的公共 API（白名單），以及「管理員專屬」API
                // ─────────────────────────────────────────────────────
                .excludePathPatterns(
                        // === 完全開放區 ===
                        "/api/member/login",
                        "/api/member/register",
                        "/api/admin/login",
                        "/api/admin/staff",
                        
                        // === 👑 管理員專屬區 (交給 Controller 驗證 Session) ===
                        // 💡 關鍵修復：把它們移出黑名單，讓 MemberController 自己用 loggedInAdmin 判斷！
                        "/api/admin/me",
                        "/api/admin/coaches",
                        "/api/member/search",            
                        "/api/member/status-update/**",
                        "/api/member/birthdays",
                        "/api/member/active-count"
                );
    }
}
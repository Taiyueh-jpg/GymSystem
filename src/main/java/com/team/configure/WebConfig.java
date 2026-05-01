package com.team.configure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; 

/**
 * 🗺️ 系統設定：攔截器的管制範圍與白名單
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 開放所有 API 路由
                .allowedOrigins(
                    "http://127.0.0.1:5500", 
                    "http://localhost:5500"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // 🛑 受保護的 API（黑名單）
                .addPathPatterns(
                        "/api/admin/me",
                        "/api/member/me",
                        "/api/member/profile/**",        // 修改與查看個資必須登入
                        "/api/member/status-update/**",  // 更改狀態必須登入
                        "/api/member/search",            // 搜尋會員必須登入
                        "/api/admin/coaches",            // 查看教練名單必須登入
                        "/api/orders/**"                 // 👈 首席架構師滷蛋加的訂單保護傘！
                )
                // 🟢 完全開放的公共 API（白名單）
                .excludePathPatterns(
                        "/api/member/login",
                        "/api/member/register",
                        "/api/admin/login",
                        "/api/admin/staff"
                );
    }
}
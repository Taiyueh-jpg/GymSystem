package com.team.configure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; 

/**
 * 🗺️ 系統設定：攔截器的管制範圍與白名單
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    // CORS 過濾已統一交由 CorsFilter.java 處理，此處保持乾淨

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // 🛑 受保護的 API（黑名單）
                .addPathPatterns(
                        "/api/admin/me",
                        "/api/members/me",
                        "/api/members/profile/**",        
                        "/api/members/status-update/**",  
                        "/api/members/search",            
                        "/api/admin/coaches",            
                        "/api/orders/**"                 
                )
                // 🟢 完全開放的公共 API（白名單）
                .excludePathPatterns(
                        "/api/members/login",
                        "/api/members/register",
                        "/api/admin/login",
                        "/api/admin/staff",
                        "/api/orders/ecpay/callback"
                );
    }
}
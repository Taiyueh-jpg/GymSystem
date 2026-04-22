package com.team.configure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// ==========================================
// 🗺️ 系統設定：告訴 Spring Boot 攔截器的管制範圍
// ==========================================
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // 🛑 1. 設定要保護的 VIP 區域 (黑名單)
                .addPathPatterns(
                        "/api/orders/**",   // 所有的訂單 API (包含結帳)
                        "/history.html"     // 歷史訂單網頁
                )
                // 🟢 2. 設定要放行的公共區域 (白名單：通常登入、註冊一定要放行)
                .excludePathPatterns(
                        "/api/members/login",
                        "/api/members/register",
                        "/product/mall",    // 商城大家都能看
                        "/api/products/**"  // 商品列表大家都能抓
                );
    }
}
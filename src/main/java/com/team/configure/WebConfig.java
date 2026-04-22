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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // 🛑 1. 設定要保護的區域 (黑名單)
                .addPathPatterns(
                        "/api/member/profile/**",       // 修改與查看個資必須登入
                        "/api/member/status-update/**", // 更改狀態必須登入 (實務上這裡還要再判斷是否為管理員)
                        "/api/member/search",           // 搜尋會員必須登入
                        "/api/admin/coaches"            // 查看教練名單必須登入
                        // 未來同學的 API 也可以加在這裡，例如 "/api/orders/**"
                )
                // 🟢 2. 設定完全開放的公共區域 (白名單)
                .excludePathPatterns(
                        "/api/member/login",      // 登入必須開放，否則永遠進不來
                        "/api/member/register",   // 註冊必須開放
                        "/api/admin/login",
                        "/api/admin/staff"        // 為了測試方便，新增員工先暫時開放
                );
    }
}
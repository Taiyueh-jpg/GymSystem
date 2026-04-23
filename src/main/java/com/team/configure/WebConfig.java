package com.team.configure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 🗺️ 系統設定：攔截器的管制範圍與白名單
 *
 * ✅ [修改紀錄]
 *    新增 "/api/admin/me" 到攔截名單（addPathPatterns）。
 *    原因：GET /api/admin/me 是前端用來驗證登入狀態並取得 adminId 的 API，
 *          必須受 LoginInterceptor 保護，未登入時自動回傳 401。
 *          若不加入攔截，即使 session 中沒有登入資訊，也能任意存取。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)

                // ─────────────────────────────────────────────────────
                // 🛑 受保護的 API（黑名單）：必須登入才能存取
                // ─────────────────────────────────────────────────────
                .addPathPatterns(
                        // [新增] 取得目前登入者資訊，前端用來取代寫死的 ADMIN_ID
                        "/api/admin/me",

                        "/api/member/profile/**",        // 修改與查看個資必須登入
                        "/api/member/status-update/**",  // 更改狀態必須登入（實務上還需再判斷是否為管理員）
                        "/api/member/search",            // 搜尋會員必須登入
                        "/api/admin/coaches"             // 查看教練名單必須登入

                        // 💡 未來其他同學的 API 也可以加在這裡，例如：
                        // "/api/orders/**",
                        // "/api/schedule/**"
                )

                // ─────────────────────────────────────────────────────
                // 🟢 完全開放的公共 API（白名單）：不需要登入
                // ─────────────────────────────────────────────────────
                .excludePathPatterns(
                        "/api/member/login",    // 登入必須開放，否則永遠進不來
                        "/api/member/register", // 註冊必須開放
                        "/api/admin/login",     // 管理員登入必須開放
                        "/api/admin/staff"      // ⚠️ 為了測試方便，新增員工暫時開放；正式上線前建議移除
                );
    }
}

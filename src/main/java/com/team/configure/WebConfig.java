package com.team.configure;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域資源共享 (CORS) 核心設定
 * 負責打通 VS Code (前端) 與 Eclipse (後端) 的 API 通訊橋樑
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 開放所有 API 路由
                .allowedOrigins(
                    "http://127.0.0.1:5500", 
                    "http://localhost:5500"
                ) // 嚴格指定允許 VS Code Live Server 的來源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允許的 HTTP 請求方法
                .allowedHeaders("*") // 允許所有請求標頭
                .allowCredentials(true) // 允許前端攜帶 Cookie 或驗證憑證
                .maxAge(3600); // 預檢請求 (Preflight) 快取時間，提升效能
    }
}
package com.team.configure;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // 啟用 @Async，EmailService.sendContactReplyNotification 會在背景執行緒寄信
    // 不阻塞 API 回應
}

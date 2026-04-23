package com.team;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 企業級健身房預約與商城系統 (GymSystem)
 * 系統主啟動類別 (Entry Point)
 */
@SpringBootApplication
public class GymSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymSystemApplication.class, args);
        System.out.println("=============================================");
        System.out.println("🚀 GymSystem 健身房預約與商城系統 啟動成功！");
        System.out.println("👉 請開啟瀏覽器訪問: http://localhost:8080");
        System.out.println("=============================================");
    }
    
}
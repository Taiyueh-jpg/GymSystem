package com.team.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 全站首頁路由
 */
@Controller
public class MainUI {

    @GetMapping({"/", "/index"})
    public String index() {
        // 導向首頁 -> src/main/resources/templates/index.html
        return "index";
    }
}
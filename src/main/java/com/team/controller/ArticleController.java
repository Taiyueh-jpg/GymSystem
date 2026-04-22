package com.team.controller;

import com.team.model.Article;
import com.team.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
// ↑ 改成指定 port 5500，allowCredentials="true" 才能帶 Session Cookie
// ↑ 若前端用其他 port（如 3000），一起加進來：origins = {"http://localhost:5500","http://localhost:3000"}
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    // ── 前台：取所有已發布文章（白名單，不需登入）──────────
    @GetMapping("/list")
    public ResponseEntity<List<Article>> getArticles() {
        return ResponseEntity.ok(articleService.getAllActiveArticles());
    }

    // ── 後台：取全部文章含草稿 ────────────────────────────
    @GetMapping("/admin/list")
    public ResponseEntity<List<Article>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    // ── 後台：新增文章 ────────────────────────────────────
    // POST Body 範例：
    // { "title":"春季優惠", "content":"...", "category":"優惠資訊",
    //   "status":"draft", "isPinned":false,
    //   "admin": { "adminId": 1 } }
    @PostMapping("/add")
    public ResponseEntity<?> addArticle(@RequestBody Article article) {
        try {
            Article saved = articleService.saveArticle(article);
            return ResponseEntity.status(201).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "新增失敗：" + e.getMessage()));
        }
    }

    // ── 後台：發布（draft → published）───────────────────
    @PatchMapping("/publish/{id}")
    public ResponseEntity<?> publishArticle(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(articleService.publishArticle(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                .body(Map.of("message", e.getMessage()));
        }
    }

    // ── 後台：刪除 ───────────────────────────────────────
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id) {
        try {
            articleService.deleteArticle(id);
            return ResponseEntity.ok(Map.of("message", "刪除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                .body(Map.of("message", "找不到文章 id=" + id));
        }
    }
}
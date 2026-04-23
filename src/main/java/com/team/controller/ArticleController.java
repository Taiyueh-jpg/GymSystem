package com.team.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.team.model.Article;
import com.team.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private Cloudinary cloudinary;

    // ── 前台：取所有已發布文章 ───────────────────────────
    @GetMapping("/list")
    public ResponseEntity<List<Article>> getArticles() {
        return ResponseEntity.ok(articleService.getAllActiveArticles());
    }

    // ── 後台：取全部文章含草稿 ───────────────────────────
    @GetMapping("/admin/list")
    public ResponseEntity<List<Article>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    // ── 後台：新增文章 ───────────────────────────────────
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

    // ── 後台：更新文章 ───────────────────────────────────
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateArticle(@PathVariable Long id,
                                           @RequestBody Article article) {
        try {
            Article existing = articleService.getArticleById(id);
            existing.setTitle(article.getTitle());
            existing.setContent(article.getContent());
            existing.setCategory(article.getCategory());
            existing.setImageUrl(article.getImageUrl());
            existing.setIsPinned(article.getIsPinned() != null ? article.getIsPinned() : false);
            if ("published".equals(article.getStatus())
                    && !"published".equals(existing.getStatus())) {
                existing.setPublishedAt(LocalDateTime.now());
            }
            existing.setStatus(article.getStatus());
            existing.setAdmin(article.getAdmin());
            return ResponseEntity.ok(articleService.saveArticle(existing));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                .body(Map.of("message", "找不到文章 id=" + id));
        }
    }

    // ── 後台：快速發布 ───────────────────────────────────
    @PatchMapping("/publish/{id}")
    public ResponseEntity<?> publishArticle(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(articleService.publishArticle(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                .body(Map.of("message", e.getMessage()));
        }
    }

    // ── 後台：刪除文章 ───────────────────────────────────
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

    // ── 後台：封面圖上傳至 Cloudinary ───────────────────
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {

        // 檔案類型檢查
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "只允許上傳圖片檔案（jpg / png / webp）"));
        }

        // 檔案大小檢查（上限 5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "圖片大小不可超過 5MB"));
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "folder",         "gymsystem/articles",
                    "resource_type",  "image",
                    "transformation", "c_limit,w_1200,h_800"
                )
            );
            String imageUrl = (String) result.get("secure_url");
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "圖片上傳失敗：" + e.getMessage()));
        }
    }
}

package com.team.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "article")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long articleId;         // ← DB bigint → Long

    // ── 關聯 Admin ────────────────────────────────────────────
    // FetchType.LAZY：避免每次查文章都多一次 JOIN admin 表
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "admin_id", referencedColumnName = "admin_id")
    private Admin admin;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "is_pinned")
    private Boolean isPinned = false;

    // ← 關鍵修正：DB 欄位是 "status"（varchar），不是 is_active
    // 值：'draft' | 'published' | 'archived'
    @Column(name = "status", length = 20)
    private String status = "draft";

    // ← 關鍵修正：DB 欄位是 "published_at"，不是 publish_date
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // 若新增時直接設定 published，自動補發布時間
        if ("published".equals(this.status) && this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
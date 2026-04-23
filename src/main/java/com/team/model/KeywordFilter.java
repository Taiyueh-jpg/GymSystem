package com.team.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "keyword_filter")
public class KeywordFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long keywordId;

    @Column(nullable = false, length = 100)
    private String keyword;

    /**
     * block → 直接擋回，回傳 400
     * flag  → 放行但標記 is_flagged=true，admin 可審核
     */
    @Column(nullable = false, length = 20)
    private String type = "block";

    /**
     * active   → 啟用中
     * inactive → 停用
     */
    @Column(nullable = false, length = 20)
    private String status = "active";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

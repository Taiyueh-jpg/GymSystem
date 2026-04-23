package com.team.dao;

import com.team.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {  // Long，不是 Integer

    // 前台：只撈 published，置頂優先、時間倒序
    @Query("SELECT a FROM Article a WHERE a.status = 'published' " +
           "ORDER BY a.isPinned DESC, a.publishedAt DESC")
    List<Article> findPublishedOrderByPinnedAndDate();

    // 後台：全部文章（含草稿），時間倒序
    @Query("SELECT a FROM Article a ORDER BY a.createdAt DESC")
    List<Article> findAllOrderByCreatedAtDesc();
}
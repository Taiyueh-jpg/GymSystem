package com.team.service;

import com.team.dao.ArticleRepository;
import com.team.model.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    // ── 前台：只拿 published ──────────────────────────────────
    public List<Article> getAllActiveArticles() {
        return articleRepository.findPublishedOrderByPinnedAndDate();
    }

    // ── 後台：取全部（含草稿）────────────────────────────────
    public List<Article> getAllArticles() {
        return articleRepository.findAllOrderByCreatedAtDesc();
    }

    // ── 新增 ─────────────────────────────────────────────────
    @Transactional
    public Article saveArticle(Article article) {
        return articleRepository.save(article);
    }

    // ── 快速發布草稿 ─────────────────────────────────────────
    @Transactional
    public Article publishArticle(Long id) {
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("找不到文章 id=" + id));
        article.setStatus("published");
        article.setPublishedAt(LocalDateTime.now());
        return articleRepository.save(article);
    }

    // ── 刪除 ─────────────────────────────────────────────────
    @Transactional
    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }
    
 // 新增 getArticleById 方法
    public Article getArticleById(Long id) {
        return articleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("找不到文章 id=" + id));
    }
}
package com.team.service;

import com.team.dao.ArticleRepository;
import com.team.dao.MemberRepository;
import com.team.model.Article;
import com.team.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 📰 文章服務層 (ArticleService)
 *
 * ✅ [修改紀錄]
 *    在原有的 publishArticle() 方法中加入寄信通知邏輯：
 *      - 注入 MemberRepository → 撈出所有 status=1 的有效會員
 *      - 注入 EmailService     → 非同步寄信（@Async），不阻塞主流程
 *    其餘方法（getAllActiveArticles、getAllArticles、saveArticle、deleteArticle、getArticleById）
 *    完全保留，未做任何修改。
 */
@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    /**
     * ✅ [新增注入] 撈有效會員 email 用
     * 對應 MemberRepository 新增的 List<Member> findByStatus(Integer status)
     */
    @Autowired
    private MemberRepository memberRepository;

    /**
     * ✅ [新增注入] 寄信通知用
     * EmailService.sendAnnouncementNotification() 已標記 @Async，
     * 呼叫後立刻返回，不等寄信完成，不影響 API 回應速度。
     */
    @Autowired
    private EmailService emailService;

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

    /**
     * 🚀 快速發布草稿 + 寄信通知所有有效會員
     *
     * ✅ [修改] 在原本的狀態更新邏輯後面，加入寄信流程：
     *   1. 找到文章、更新狀態與發布時間（原有邏輯，不動）
     *   2. 儲存文章（原有邏輯，不動）
     *   3. [新增] 撈出所有 status=1 的有效會員
     *   4. [新增] 逐一非同步寄信，寄信結果自動寫入 email_log
     *
     * 注意：@Transactional 只管資料庫操作（步驟1、2），
     *       寄信是在 transaction commit 之後才觸發，不會因寄信失敗而 rollback 文章狀態。
     */
    @Transactional
    public Article publishArticle(Long id) {
        // ── 原有邏輯：更新文章狀態 ──────────────────────────
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到文章 id=" + id));
        article.setStatus("published");
        article.setPublishedAt(LocalDateTime.now());
        Article saved = articleRepository.save(article);

        // ── [新增] 寄信通知所有有效會員 ─────────────────────
        // 撈出 status=1 的所有正常會員（使用 MemberRepository 新增的不分頁版本）
        List<Member> activeMembers = memberRepository.findByStatus(1);

        // 取文章前 100 字作為摘要，避免 email 內容過長
        String summary = buildSummary(saved.getContent(), 100);

        // 逐一呼叫 EmailService 非同步寄信
        // @Async 確保每封信都在背景執行，不會讓 API 等待
        // 寄信成功 → email_log.send_status = 'sent'
        // 寄信失敗 → email_log.send_status = 'failed'（不影響文章發布結果）
        for (Member member : activeMembers) {
            emailService.sendAnnouncementNotification(
                    member.getEmail(),       // 收件人 email
                    member.getName(),        // 收件人姓名
                    saved.getArticleId(),    // 文章 ID（信中連結用）
                    saved.getTitle(),        // 文章標題
                    summary                  // 文章摘要
            );
        }

        return saved;
    }

    // ── 刪除 ─────────────────────────────────────────────────
    @Transactional
    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    // ── 根據 ID 取單篇文章 ────────────────────────────────────
    public Article getArticleById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到文章 id=" + id));
    }

    // ── 工具方法 ──────────────────────────────────────────────

    /**
     * 將文章內容截取成摘要，避免 email 內容過長
     *
     * @param content 原始內容
     * @param maxLen  最大字元數
     * @return 截取後的摘要，超過長度時結尾加「...」
     */
    private String buildSummary(String content, int maxLen) {
        if (content == null || content.isBlank()) return "";
        return content.length() > maxLen
                ? content.substring(0, maxLen) + "..."
                : content;
    }
}

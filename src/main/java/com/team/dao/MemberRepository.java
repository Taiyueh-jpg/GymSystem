package com.team.dao;

import com.team.model.Member;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * 會員資料存取層 (Member DAO)
 *
 * 技術細節：
 * 1. 繼承 JpaRepository 取得基礎 CRUD 功能。
 * 2. 使用 Optional 處理查詢結果，避免 NullPointerException。
 *
 * ✅ [修改紀錄]
 *    新增 List<Member> findByStatus(Integer status)（不分頁版本）。
 *    原因：發布文章後需寄信通知所有有效會員。
 *          原本的 Page<Member> findByStatus(Integer, Pageable) 需要傳入分頁參數，
 *          不適合用在「一次撈出全部」的寄信情境。
 *          新方法直接回傳 List，ArticleService 可直接迴圈處理。
 *
 *    ⚠️ 注意：若日後會員數量龐大（例如超過數萬人），建議改為批次處理（分批撈、分批寄），
 *             避免一次載入大量物件撐爆記憶體或寄信 timeout。
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 🔍 透過 Email 尋找會員（登入、驗證時使用）
    Optional<Member> findByEmail(String email);

    // 🔍 登入專用：同時驗證 Email 與狀態（只抓取正常狀態的會員）
    Optional<Member> findByEmailAndStatus(String email, Integer status);

    // 🔍 檢查 Email 是否已經被註冊過（註冊時防呆用）
    boolean existsByEmail(String email);

    // 🔍 模糊搜尋「姓名」或「電話」，加入分頁功能（後台管理員搜尋會員時使用）
    Page<Member> findByNameContainingOrMobileContaining(String name, String mobile, Pageable pageable);

    // 🔍 根據狀態查詢 - 分頁版（例如查詢所有被停權的會員）
    Page<Member> findByStatus(Integer status, Pageable pageable);

    /**
     * 🔍 根據狀態查詢 - 不分頁版（一次取全部）
     *
     * ✅ [新增] 發布文章寄信通知使用。
     * 呼叫方式範例（ArticleService 內）：
     *   List<Member> activeMembers = memberRepository.findByStatus(1);
     *
     * Spring Data JPA 會自動依方法名稱產生以下 SQL：
     *   SELECT * FROM member WHERE status = ?
     *
     * @param status 會員狀態：1 = 正常有效，0 = 停權
     * @return 符合狀態的所有會員清單
     */
    List<Member> findByStatus(Integer status);

    // 🎂 查詢本月壽星（健身房行銷活動使用）
    // 如果未來「一般會員」也要發放生日優惠券，只要改成 AND m.status >= 0 就可以了
    @Query("SELECT m FROM Member m WHERE MONTH(m.birthday) = :month AND m.status >= 0")
    List<Member> findBirthdaysByMonth(@Param("month") int month);

    // 📊 統計功能：計算目前有效會員總數
    long countByStatus(Integer status);
 // 🔍 撈出所有未停權的會員（status >= 0，一般會員 + 付費會員，排除停權）
    @Query("SELECT m FROM Member m WHERE m.status >= 0")
    List<Member> findAllActiveMembers();
}

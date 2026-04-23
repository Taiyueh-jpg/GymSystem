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
 * * 技術細節：
 * 1. 繼承 JpaRepository 取得基礎 CRUD 功能。 
 * 2. 使用 Optional 處理查詢結果，避免 NullPointerException。
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long>{

	// 🔍 透過 Email 尋找會員 (登入、驗證時使用)
    Optional<Member> findByEmail(String email);
    
    // 🔍 登入專用：同時驗證 Email 與 狀態 (只抓取正常狀態的會員)
    Optional<Member> findByEmailAndStatus(String email, Integer status);
    
    // 🔍 檢查 Email 是否已經被註冊過 (註冊時防呆用)
    boolean existsByEmail(String email);

    // 🔍 模糊搜尋「姓名」或「電話」，並加入分頁功能 (後台管理員搜尋會員時使用)
    Page<Member> findByNameContainingOrMobileContaining(String name, String mobile, Pageable pageable);

    // 🔍 根據狀態查詢-分頁版 (例如查詢所有被停權的會員)
    Page<Member> findByStatus(Integer status, Pageable pageable);

    // 🎂 查詢本月壽星 (健身房行銷活動使用)
    //如果未來「一般會員」也要發放生日優惠卷來吸引他們買課，只要改成 AND m.status >= 0 就可以了
    @Query("SELECT m FROM Member m WHERE MONTH(m.birthday) = :month AND m.status = 1")
    List<Member> findBirthdaysByMonth(@Param("month") int month);
    
    // 📊 統計功能：計算目前有效會員總數
    long countByStatus(Integer status);
}

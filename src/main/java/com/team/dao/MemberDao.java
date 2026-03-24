package com.team.dao;

import com.team.model.*; // 匯入剛剛寫好的實體類別
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// ==========================================
// 👑 同學 A 負責區域：會員與管理員 DAO
// ==========================================

@Repository
public interface MemberDao extends JpaRepository<Member, Long> {
    // Spring Data JPA 魔術：只要依照命名規則，系統會自動生成「用 Email 尋找會員」的 SQL
    Member findByEmail(String email);
    
    // 檢查 Email 是否已註冊
    boolean existsByEmail(String email);
}

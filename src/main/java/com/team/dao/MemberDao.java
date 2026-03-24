package com.team.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.team.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// ==========================================
// 👑 同學 A 負責區域：會員與管理員 DAO
// ==========================================

public interface MemberDao extends JpaRepository<Member, Long> {
    
    // 🌟 架構師魔法：只要寫 findBy + 欄位名稱，Spring 就會自動幫你寫好 SQL 語法！
    // 這等同於 SQL: SELECT * FROM member WHERE email = ?
    Member findByEmail(String email);
    
}

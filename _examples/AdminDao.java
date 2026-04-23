package com.team.dao;

import com.team.model.*; // 匯入剛剛寫好的實體類別
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// ==========================================
// 👑 同學 A 負責區域：會員與管理員 DAO
// ==========================================


@Repository
public interface AdminDao extends JpaRepository<Admin, Long> {
    // 管理員登入用的查詢
    Admin findByCode(String code);
}
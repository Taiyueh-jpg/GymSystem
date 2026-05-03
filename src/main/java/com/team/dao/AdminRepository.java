package com.team.dao;

import com.team.model.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // 🔍 登入驗證專用
    Optional<Admin> findByEmailAndStatus(String email, Integer status);
    
    // 🔍 檢查帳號是否重複
    boolean existsByEmail(String email);

    // 👨‍🏫 獲取特定角色的名單 (例如：抓取所有 "coach" 且狀態為 1 的在職教練名單)
    List<Admin> findByRoleAndStatus(String role, Integer status);

    // 🔍 後台員工管理：支援依姓名模糊搜尋與角色過濾
    Page<Admin> findByNameContainingAndRole(String name, String role, Pageable pageable);

    // 🚀 新增：支援姓名或信箱的模糊搜尋，並帶有分頁功能 (供員工清單總覽使用)
    Page<Admin> findByNameContainingOrEmailContaining(String name, String email, Pageable pageable);
}
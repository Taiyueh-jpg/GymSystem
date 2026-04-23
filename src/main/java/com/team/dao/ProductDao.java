package com.team.dao;

import com.team.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// ==========================================
// 🛒 同學 C 負責區域：商品 DAO
// ==========================================

@Repository
public interface ProductDao extends JpaRepository<Product, Long> {

    // 依商品名稱模糊搜尋（前台搜尋列用）
    List<Product> findByPnameContaining(String keyword);
}

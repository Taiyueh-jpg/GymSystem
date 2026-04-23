package com.team.dao;

import com.team.model.*; // 匯入剛剛寫好的實體類別
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//==========================================
//🛒 同學 D 負責區域：商品商城 DAO
//==========================================

@Repository
public interface ProductDao extends JpaRepository<Product, Long> {
 // 模糊搜尋商品名稱 (例如搜尋 "乳清")
 List<Product> findByPnameContaining(String keyword);
}

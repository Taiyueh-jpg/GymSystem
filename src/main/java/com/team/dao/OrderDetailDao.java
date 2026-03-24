package com.team.dao;

import com.team.model.*; // 匯入剛剛寫好的實體類別
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//==========================================
//📦 同學 E 負責區域：訂單與明細 DAO
//==========================================

@Repository
public interface OrderDetailDao extends JpaRepository<OrderDetail, Long> {
 // 查詢某筆訂單底下的所有購買明細
 List<OrderDetail> findByOrderId(Long orderId);
}
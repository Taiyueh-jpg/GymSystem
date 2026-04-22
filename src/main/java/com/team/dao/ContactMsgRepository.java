package com.team.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.model.ContactMsg;

@Repository
public interface ContactMsgRepository extends JpaRepository<ContactMsg, Long> {

    // 依會員查詢
    List<ContactMsg> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 查詢所有未讀（admin 用）
    List<ContactMsg> findByIsReadFalseOrderByCreatedAtDesc();

    // 依狀態查詢
    List<ContactMsg> findByMsgStatusOrderByCreatedAtDesc(String msgStatus);

    // ✅ 新增：查詢被 keyword filter 標記的留言
    List<ContactMsg> findByIsFlaggedTrueOrderByCreatedAtDesc();

    // ✅ 新增：Guest 用 email 查詢自己的留言
    List<ContactMsg> findByGuestEmailOrderByCreatedAtDesc(String guestEmail);
}

package com.team.dao;

import com.team.model.ContactMsg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMsgDao extends JpaRepository<ContactMsg, Long> {
    
    // 讓管理員可以抓出所有「未處理 (new)」的留言
    List<ContactMsg> findByMsgStatusOrderByCreatedAtAsc(String msgStatus);
    
    // 讓會員可以查詢自己過去發問過的紀錄
    List<ContactMsg> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
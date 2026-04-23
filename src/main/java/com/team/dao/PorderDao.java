package com.team.dao;

import com.team.model.Porder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PorderDao extends JpaRepository<Porder, Long> {
    // 透過會員 ID 查詢所有訂單，並依下單時間降序排列 (最新在前面)
    List<Porder> findByMemberIdOrderByOrderDateDesc(Long memberId);

    // 🌟 查詢全公司所有訂單，並依照時間由新到舊排序 (後台用)
    List<Porder> findAllByOrderByOrderDateDesc();
}
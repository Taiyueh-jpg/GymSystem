package com.team.dao;

import com.team.model.*; // 匯入剛剛寫好的實體類別
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//==========================================
//📅 同學 C 負責區域：預約紀錄 DAO
//==========================================

@Repository
public interface ReservationDao extends JpaRepository<Reservation, Long> {
 // 查詢某位會員所有的預約紀錄
 List<Reservation> findByMemberId(Long memberId);
 
 // 防呆機制：檢查該會員是否已經預約過這堂課 (防止重複預約)
 boolean existsByMemberIdAndCourseId(Long memberId, Long courseId);
}

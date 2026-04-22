package com.team.dao;

import com.team.model.*; // 匯入剛剛寫好的實體類別
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//==========================================
//🏋️‍♂️ 同學 B 負責區域：課程 DAO
//==========================================

@Repository
public interface CourseDao extends JpaRepository<Course, Long> {
 // 依據課程類型 (1對1 或 團課) 進行搜尋
 List<Course> findByCourseType(String courseType);
}
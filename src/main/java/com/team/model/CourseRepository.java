package com.team.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // 查詢狀態為開放的課程
    List<Course> findByStatus(Integer status);

    // 依課程類型查詢，例如 group / personal
    List<Course> findByCourseType(String courseType);

    // 依教練編號查詢課程
    List<Course> findByCoachId(Long coachId);

    // 依教練編號 + 狀態查詢
    List<Course> findByCoachIdAndStatus(Long coachId, Integer status);

    // 依教練編號 + 課程類型查詢
    List<Course> findByCoachIdAndCourseType(Long coachId, String courseType);

    // 依教練編號 + 課程類型 + 狀態查詢
    List<Course> findByCoachIdAndCourseTypeAndStatus(Long coachId, String courseType, Integer status);

    // 依課程類型 + 狀態查詢
    List<Course> findByCourseTypeAndStatus(String courseType, Integer status);
}
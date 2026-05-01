package com.team.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseReservationRepository extends JpaRepository<CourseReservation, Long> {

    List<CourseReservation> findByCourseId(Long courseId);

    List<CourseReservation> findByMemberId(Long memberId);

    Optional<CourseReservation> findByCourseIdAndMemberId(Long courseId, Long memberId);

    boolean existsByCourseIdAndMemberId(Long courseId, Long memberId);

    List<CourseReservation> findByCourseIdAndReservationStatus(Long courseId, String reservationStatus);

    List<CourseReservation> findByMemberIdAndReservationStatus(Long memberId, String reservationStatus);

    // 新增：依會員 + 狀態查詢
    List<CourseReservation> findByMemberIdAndReservationStatusOrderByReservationIdAsc(Long memberId, String reservationStatus);

    // 新增：依課程 + 狀態查詢
    List<CourseReservation> findByCourseIdAndReservationStatusOrderByReservationIdAsc(Long courseId, String reservationStatus);
}
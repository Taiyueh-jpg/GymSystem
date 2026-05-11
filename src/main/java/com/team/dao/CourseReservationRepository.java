package com.team.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.model.CourseReservation;

public interface CourseReservationRepository extends JpaRepository<CourseReservation, Long> {

    List<CourseReservation> findByCourseId(Long courseId);

    List<CourseReservation> findByMemberId(Long memberId);

    Optional<CourseReservation> findByCourseIdAndMemberId(Long courseId, Long memberId);

    Optional<CourseReservation> findByCourseIdAndMemberIdAndReservationStatus(
            Long courseId,
            Long memberId,
            String reservationStatus
    );

    boolean existsByCourseIdAndMemberId(Long courseId, Long memberId);

    boolean existsByCourseIdAndMemberIdAndReservationStatus(
            Long courseId,
            Long memberId,
            String reservationStatus
    );

    List<CourseReservation> findByCourseIdAndReservationStatus(Long courseId, String reservationStatus);

    List<CourseReservation> findByMemberIdAndReservationStatus(Long memberId, String reservationStatus);

    List<CourseReservation> findByMemberIdAndReservationStatusOrderByReservationIdAsc(Long memberId, String reservationStatus);

    List<CourseReservation> findByCourseIdAndReservationStatusOrderByReservationIdAsc(Long courseId, String reservationStatus);
}
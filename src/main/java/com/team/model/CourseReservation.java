package com.team.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "course_reservation")
@Data
public class CourseReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "reservation_status", nullable = false, length = 20)
    private String reservationStatus;

    @Column(name = "reserved_at", nullable = false)
    private Date reservedAt;

    @Column(name = "cancelled_at")
    private Date cancelledAt;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "used_pt_order_id")
    private Long usedPtOrderId;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;
}
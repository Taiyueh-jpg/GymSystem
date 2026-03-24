package com.team.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

//==========================================
//📅 同學 C 負責區域：預約紀錄
//==========================================

@Entity
@Table(name = "reservation")
public class Reservation {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long reservationId;
 
 // 紀錄是哪位會員預約的
 @Column(nullable = false)
 private Long memberId;
 
 // 紀錄預約了哪堂課
 @Column(nullable = false)
 private Long courseId;
 
 @Column(nullable = false)
 private LocalDateTime reservationTime; // 預約時間
 
 @Column(nullable = false)
 private String status; // 狀態：SUCCESS (成功), CANCELLED (取消), WAITLIST (候補)

 // Getters and Setters...
 public Long getReservationId() { return reservationId; }
 public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
 public Long getMemberId() { return memberId; }
 public void setMemberId(Long memberId) { this.memberId = memberId; }
 public Long getCourseId() { return courseId; }
 public void setCourseId(Long courseId) { this.courseId = courseId; }
 public LocalDateTime getReservationTime() { return reservationTime; }
 public void setReservationTime(LocalDateTime reservationTime) { this.reservationTime = reservationTime; }
 public String getStatus() { return status; }
 public void setStatus(String status) { this.status = status; }
}

package com.team.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

//==========================================
//🏋️‍♂️ 同學 B 負責區域：課程管理
//==========================================

@Entity
@Table(name = "course")
public class Course {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long courseId;
 
 @Column(nullable = false)
 private String courseName;
 
 private String courseType; // 1-on-1, Group
 private String coachName;
 
 @Column(nullable = false)
 private Integer capacity; // 人數上限
 
 @Column(nullable = false)
 private Integer enrolledCount = 0; // 目前報名人數 (預設 0)

 // Getters and Setters...
 public Long getCourseId() { return courseId; }
 public void setCourseId(Long courseId) { this.courseId = courseId; }
 public String getCourseName() { return courseName; }
 public void setCourseName(String courseName) { this.courseName = courseName; }
 public String getCourseType() { return courseType; }
 public void setCourseType(String courseType) { this.courseType = courseType; }
 public String getCoachName() { return coachName; }
 public void setCoachName(String coachName) { this.coachName = coachName; }
 public Integer getCapacity() { return capacity; }
 public void setCapacity(Integer capacity) { this.capacity = capacity; }
 public Integer getEnrolledCount() { return enrolledCount; }
 public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }
}

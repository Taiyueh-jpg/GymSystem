package com.team.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.team.model.Course;
import com.team.model.CourseRepository;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    // 查全部課程
    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }

    // 依課程 ID 查單一課程
    public Course findCourseById(Long courseId) {
        return courseRepository.findById(courseId).orElse(null);
    }

    // 新增課程
    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    // 修改課程
    public Course updateCourse(Course course) {
        return courseRepository.save(course);
    }

    // 查詢某位教練的所有課程
    public List<Course> findCoursesByCoachId(Long coachId) {
        return courseRepository.findByCoachId(coachId);
    }

    // 查詢開放中的課程
    public List<Course> findOpenCourses() {
        return courseRepository.findByStatus(1);
    }

    // 依課程狀態查詢
    public List<Course> findCoursesByStatus(Integer status) {
        return courseRepository.findByStatus(status);
    }

    // 依課程類型查詢
    public List<Course> findCoursesByType(String courseType) {
        return courseRepository.findByCourseType(courseType);
    }

    // 查某位教練的開放課程
    public List<Course> findOpenCoursesByCoachId(Long coachId) {
        return courseRepository.findByCoachIdAndStatus(coachId, 1);
    }

    // 查某位教練的某種類型課程
    public List<Course> findCoursesByCoachIdAndType(Long coachId, String courseType) {
        return courseRepository.findByCoachIdAndCourseType(coachId, courseType);
    }

    // 查某位教練開放中的某種類型課程
    public List<Course> findOpenCoursesByCoachIdAndType(Long coachId, String courseType) {
        return courseRepository.findByCoachIdAndCourseTypeAndStatus(coachId, courseType, 1);
    }

    // 查開放中的某種類型課程
    public List<Course> findOpenCoursesByType(String courseType) {
        return courseRepository.findByCourseTypeAndStatus(courseType, 1);
    }
    public List<Course> findOpenNotFullCourses() {
        List<Course> openCourses = courseRepository.findByStatus(1);
        List<Course> result = new ArrayList<>();

        for (Course course : openCourses) {
            int capacity = course.getCapacity() == null ? 0 : course.getCapacity();
            int enrolledCount = course.getEnrolledCount() == null ? 0 : course.getEnrolledCount();

            if (enrolledCount < capacity) {
                result.add(course);
            }
        }

        return result;
    }
    public List<Course> findOpenNotFullCoursesByCoachId(Long coachId) {
        List<Course> courses = courseRepository.findByCoachIdAndStatus(coachId, 1);
        List<Course> result = new ArrayList<>();

        for (Course course : courses) {
            int capacity = course.getCapacity() == null ? 0 : course.getCapacity();
            int enrolledCount = course.getEnrolledCount() == null ? 0 : course.getEnrolledCount();

            if (enrolledCount < capacity) {
                result.add(course);
            }
        }

        return result;
    }
    
}
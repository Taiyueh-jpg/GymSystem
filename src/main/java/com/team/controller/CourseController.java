package com.team.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.team.model.Course;
import com.team.service.CourseService;

@RestController
@RequestMapping("/courses")
@CrossOrigin
public class CourseController {

    @Autowired
    private CourseService courseService;

    // 查全部課程
    @GetMapping
    public List<Map<String, Object>> getAllCourses() {
        List<Course> courses = courseService.findAllCourses();
        return convertCourseList(courses);
    }

    // 查單一課程
    @GetMapping("/{courseId}")
    public ResponseEntity<?> getCourseById(@PathVariable Long courseId) {
        Course course = courseService.findCourseById(courseId);
        if (course == null) {
            return new ResponseEntity<>("找不到課程資料", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(convertCourse(course), HttpStatus.OK);
    }

    // 查開放中的課程
    @GetMapping("/open")
    public List<Map<String, Object>> getOpenCourses() {
        List<Course> courses = courseService.findOpenCourses();
        return convertCourseList(courses);
    }

    // 依課程狀態查詢
    @GetMapping("/status/{status}")
    public List<Map<String, Object>> getCoursesByStatus(@PathVariable Integer status) {
        List<Course> courses = courseService.findCoursesByStatus(status);
        return convertCourseList(courses);
    }

    // 查某種類型的課程
    @GetMapping("/type/{courseType}")
    public List<Map<String, Object>> getCoursesByType(@PathVariable String courseType) {
        List<Course> courses = courseService.findCoursesByType(courseType);
        return convertCourseList(courses);
    }

    // 查某位教練的所有課程
    @GetMapping("/coach/{coachId}")
    public List<Map<String, Object>> getCoursesByCoachId(@PathVariable Long coachId) {
        List<Course> courses = courseService.findCoursesByCoachId(coachId);
        return convertCourseList(courses);
    }

    // 查開放中的某種類型課程
    @GetMapping("/open/type/{courseType}")
    public List<Map<String, Object>> getOpenCoursesByType(@PathVariable String courseType) {
        List<Course> courses = courseService.findOpenCoursesByType(courseType);
        return convertCourseList(courses);
    }

    // 查某位教練的開放課程
    @GetMapping("/coach/{coachId}/open")
    public List<Map<String, Object>> getOpenCoursesByCoachId(@PathVariable Long coachId) {
        List<Course> courses = courseService.findOpenCoursesByCoachId(coachId);
        return convertCourseList(courses);
    }

    // 查某位教練的某種類型課程
    @GetMapping("/coach/{coachId}/type/{courseType}")
    public List<Map<String, Object>> getCoursesByCoachIdAndType(@PathVariable Long coachId,
                                                                @PathVariable String courseType) {
        List<Course> courses = courseService.findCoursesByCoachIdAndType(coachId, courseType);
        return convertCourseList(courses);
    }

    // 查某位教練的開放中的某種類型課程
    @GetMapping("/coach/{coachId}/open/type/{courseType}")
    public List<Map<String, Object>> getOpenCoursesByCoachIdAndType(@PathVariable Long coachId,
                                                                    @PathVariable String courseType) {
        List<Course> courses = courseService.findOpenCoursesByCoachIdAndType(coachId, courseType);
        return convertCourseList(courses);
    }

    // 新增課程
    @PostMapping
    public ResponseEntity<String> createCourse(@RequestBody Course course) {
        String validateMessage = validateCourse(course);
        if (validateMessage != null) {
            return new ResponseEntity<>(validateMessage, HttpStatus.BAD_REQUEST);
        }

        Date now = new Date();
        course.setCreatedAt(now);
        course.setUpdatedAt(now);

        courseService.saveCourse(course);
        return new ResponseEntity<>("新增課程成功", HttpStatus.OK);
    }

    // 修改課程
    @PutMapping("/{courseId}")
    public ResponseEntity<String> updateCourse(@PathVariable Long courseId, @RequestBody Course course) {
        Course existingCourse = courseService.findCourseById(courseId);
        if (existingCourse == null) {
            return new ResponseEntity<>("找不到要修改的課程", HttpStatus.NOT_FOUND);
        }

        course.setCourseId(courseId);
        course.setCreatedAt(existingCourse.getCreatedAt());
        course.setUpdatedAt(new Date());

        String validateMessage = validateCourse(course);
        if (validateMessage != null) {
            return new ResponseEntity<>(validateMessage, HttpStatus.BAD_REQUEST);
        }

        courseService.updateCourse(course);
        return new ResponseEntity<>("修改課程成功", HttpStatus.OK);
    }

    // 驗證課程資料
    private String validateCourse(Course course) {
        if (course.getCourseType() == null ||
                !(course.getCourseType().equals("group") || course.getCourseType().equals("personal"))) {
            return "courseType 只能是 group 或 personal";
        }

        if (course.getStatus() == null ||
                !(course.getStatus() == 0 || course.getStatus() == 1 || course.getStatus() == 2)) {
            return "status 只能是 0、1、2";
        }

        if (course.getCapacity() == null || course.getCapacity() < 0) {
            return "capacity 不能小於 0";
        }

        if (course.getEnrolledCount() == null || course.getEnrolledCount() < 0) {
            return "enrolledCount 不能小於 0";
        }

        if (course.getEnrolledCount() > course.getCapacity()) {
            return "enrolledCount 不能大於 capacity";
        }

        return null;
    }

    // 把 Course entity 轉成精簡格式
    private Map<String, Object> convertCourse(Course course) {
        Map<String, Object> item = new LinkedHashMap<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        item.put("courseId", course.getCourseId());
        item.put("courseName", course.getCourseName());
        item.put("courseType", course.getCourseType());
        item.put("coachId", course.getCoachId());
        item.put("description", course.getDescription());

        item.put("courseDate", course.getCourseDate() != null ? dateFormat.format(course.getCourseDate()) : null);
        item.put("startTime", course.getStartTime() != null ? timeFormat.format(course.getStartTime()) : null);
        item.put("endTime", course.getEndTime() != null ? timeFormat.format(course.getEndTime()) : null);

        item.put("capacity", course.getCapacity());
        item.put("enrolledCount", course.getEnrolledCount());

        Integer capacity = course.getCapacity();
        Integer enrolledCount = course.getEnrolledCount();

        if (capacity != null && enrolledCount != null) {
            item.put("remainingSeats", capacity - enrolledCount);
        } else {
            item.put("remainingSeats", null);
        }

        item.put("status", course.getStatus());

        return item;
    }

    // 把 Course List 轉成精簡格式 List
    private List<Map<String, Object>> convertCourseList(List<Course> courses) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Course course : courses) {
            result.add(convertCourse(course));
        }
        return result;
    }
 // 查單一課程名額資訊
    @GetMapping("/{courseId}/seat-info")
    public ResponseEntity<?> getCourseSeatInfo(@PathVariable Long courseId) {
        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return new ResponseEntity<>("找不到課程資料", HttpStatus.NOT_FOUND);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        int capacity = course.getCapacity() == null ? 0 : course.getCapacity();
        int enrolledCount = course.getEnrolledCount() == null ? 0 : course.getEnrolledCount();
        int remainingSeats = Math.max(0, capacity - enrolledCount);

        result.put("courseId", course.getCourseId());
        result.put("courseName", course.getCourseName());
        result.put("courseType", course.getCourseType());
        result.put("status", course.getStatus());
        result.put("capacity", capacity);
        result.put("enrolledCount", enrolledCount);
        result.put("remainingSeats", remainingSeats);
        result.put("isFull", remainingSeats == 0);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
 // 查全部開放中且未額滿的課程
    @GetMapping("/open/not-full")
    public List<Map<String, Object>> getOpenNotFullCourses() {
        List<Course> courses = courseService.findOpenNotFullCourses();
        return convertCourseList(courses);
    }
 // 查某教練名下開放中且未額滿的課程
    @GetMapping("/coach/{coachId}/open/not-full")
    public List<Map<String, Object>> getOpenNotFullCoursesByCoachId(@PathVariable Long coachId) {
        List<Course> courses = courseService.findOpenNotFullCoursesByCoachId(coachId);
        return convertCourseList(courses);
    }
}
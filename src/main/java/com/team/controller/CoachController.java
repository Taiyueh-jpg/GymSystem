package com.team.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.team.model.Admin;
import com.team.model.Course;
import com.team.service.CoachService;
import com.team.service.CourseService;

@RestController
@RequestMapping("/coaches")
@CrossOrigin
public class CoachController {

    @Autowired
    private CoachService coachService;

    @Autowired
    private CourseService courseService;

    // 查全部教練
    @GetMapping
    public List<Admin> getAllCoaches() {
        return coachService.findAllCoaches();
    }

    // 查全部啟用中的教練
    @GetMapping("/active")
    public List<Admin> getActiveCoaches() {
        return coachService.findActiveCoaches();
    }

    // 查單一教練
    @GetMapping("/{coachId}")
    public ResponseEntity<?> getCoachById(@PathVariable Long coachId) {
        Admin coach = coachService.findCoachById(coachId);

        if (coach == null) {
            return new ResponseEntity<>("找不到教練資料", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(coach, HttpStatus.OK);
    }

    // 查某教練的課表
    @GetMapping("/{coachId}/courses")
    public ResponseEntity<?> getCoursesByCoachId(@PathVariable Long coachId) {
        Admin coach = coachService.findCoachById(coachId);

        if (coach == null) {
            return new ResponseEntity<>("找不到教練資料", HttpStatus.NOT_FOUND);
        }

        List<Course> courses = courseService.findCoursesByCoachId(coachId);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }
 // 查某教練的開放中課程
    @GetMapping("/{coachId}/courses/open")
    public ResponseEntity<?> getOpenCoursesByCoachId(@PathVariable Long coachId) {
        Admin coach = coachService.findCoachById(coachId);

        if (coach == null) {
            return new ResponseEntity<>("找不到教練資料", HttpStatus.NOT_FOUND);
        }

        List<Course> courses = courseService.findOpenCoursesByCoachId(coachId);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }
 // 查某教練某種類型的全部課程
    @GetMapping("/{coachId}/courses/type/{courseType}")
    public ResponseEntity<?> getCoursesByCoachIdAndType(@PathVariable Long coachId,
                                                        @PathVariable String courseType) {
        Admin coach = coachService.findCoachById(coachId);

        if (coach == null) {
            return new ResponseEntity<>("找不到教練資料", HttpStatus.NOT_FOUND);
        }

        List<Course> courses = courseService.findCoursesByCoachIdAndType(coachId, courseType);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    // 查某教練某種類型的開放課程
    @GetMapping("/{coachId}/courses/open/type/{courseType}")
    public ResponseEntity<?> getOpenCoursesByCoachIdAndType(@PathVariable Long coachId,
                                                            @PathVariable String courseType) {
        Admin coach = coachService.findCoachById(coachId);

        if (coach == null) {
            return new ResponseEntity<>("找不到教練資料", HttpStatus.NOT_FOUND);
        }

        List<Course> courses = courseService.findOpenCoursesByCoachIdAndType(coachId, courseType);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }
}
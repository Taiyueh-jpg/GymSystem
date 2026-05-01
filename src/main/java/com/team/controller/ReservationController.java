package com.team.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.team.model.Course;
import com.team.model.CourseReservation;
import com.team.service.ReservationService;

@RestController
@RequestMapping("/reservations")
@CrossOrigin
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> getReservationsByMemberId(@PathVariable Long memberId) {
        Map<String, Object> memberSummary = reservationService.getMemberReservationSummary(memberId);
        if (memberSummary == null) {
            return new ResponseEntity<>("找不到會員資料", HttpStatus.BAD_REQUEST);
        }

        List<CourseReservation> reservations = reservationService.findReservationsByMemberId(memberId);
        if (reservations == null || reservations.isEmpty()) {
            return new ResponseEntity<>("此會員目前沒有預約紀錄", HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    @GetMapping("/member/{memberId}/active")
    public ResponseEntity<?> getActiveReservationsByMemberId(@PathVariable Long memberId) {
        Map<String, Object> memberSummary = reservationService.getMemberReservationSummary(memberId);
        if (memberSummary == null) {
            return new ResponseEntity<>("找不到會員資料", HttpStatus.BAD_REQUEST);
        }

        List<CourseReservation> reservations = reservationService.findActiveReservationsByMemberId(memberId);
        if (reservations == null || reservations.isEmpty()) {
            return new ResponseEntity<>("此會員目前沒有有效預約紀錄", HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    @GetMapping("/member/{memberId}/status/{status}")
    public ResponseEntity<?> getReservationsByMemberIdAndStatus(@PathVariable Long memberId,
            @PathVariable String status) {
        if (!isValidStatus(status)) {
            return new ResponseEntity<>("status 只能是 reserved、cancelled、completed、no_show", HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> memberSummary = reservationService.getMemberReservationSummary(memberId);
        if (memberSummary == null) {
            return new ResponseEntity<>("找不到會員資料", HttpStatus.BAD_REQUEST);
        }

        List<CourseReservation> reservations = reservationService.findReservationsByMemberIdAndStatus(memberId, status);
        if (reservations == null || reservations.isEmpty()) {
            return new ResponseEntity<>("此會員目前沒有 " + status + " 狀態的預約紀錄", HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    @GetMapping("/member/{memberId}/summary")
    public ResponseEntity<?> getMemberReservationSummary(@PathVariable Long memberId) {
        Map<String, Object> result = reservationService.getMemberReservationSummary(memberId);

        if (result == null) {
            return new ResponseEntity<>("找不到會員資料", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/member/{memberId}/remaining-pt-sessions")
    public ResponseEntity<?> getMemberRemainingPtSessions(@PathVariable Long memberId) {
        Map<String, Object> result = reservationService.getMemberRemainingPtSessions(memberId);

        if (result == null) {
            return new ResponseEntity<>("找不到會員資料", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/member/{memberId}/available-personal-courses")
    public ResponseEntity<?> getAvailablePersonalCoursesForMember(@PathVariable Long memberId) {
        Object result = reservationService.findAvailablePersonalCoursesForMember(memberId);

        if (result instanceof String) {
            String message = (String) result;

            if ("找不到會員資料".equals(message) || "此會員目前不可預約課程".equals(message)) {
                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(message, HttpStatus.OK);
        }

        @SuppressWarnings("unchecked")
        List<Course> courses = (List<Course>) result;

        if (courses == null || courses.isEmpty()) {
            return new ResponseEntity<>("目前沒有可預約的私人教練課程", HttpStatus.OK);
        }

        return new ResponseEntity<>(convertCourseList(courses), HttpStatus.OK);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getReservationsByCourseId(@PathVariable Long courseId) {
        Map<String, Object> courseSummary = reservationService.getCourseReservationSummary(courseId);
        if (courseSummary == null) {
            return new ResponseEntity<>("找不到課程資料", HttpStatus.BAD_REQUEST);
        }

        List<CourseReservation> reservations = reservationService.findReservationsByCourseId(courseId);
        if (reservations == null || reservations.isEmpty()) {
            return new ResponseEntity<>("此課程目前沒有預約紀錄", HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    @GetMapping("/personal/course/{courseId}")
    public ResponseEntity<?> getPersonalReservationsByCourseId(@PathVariable Long courseId) {
        Object result = reservationService.findPersonalReservationsByCourseId(courseId);

        if (result instanceof String) {
            String message = (String) result;
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        @SuppressWarnings("unchecked")
        List<CourseReservation> reservations = (List<CourseReservation>) result;

        if (reservations == null || reservations.isEmpty()) {
            return new ResponseEntity<>("此私人教練課程目前沒有預約紀錄", HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    @GetMapping("/personal/course/{courseId}/active")
    public ResponseEntity<?> getActivePersonalReservationsByCourseId(@PathVariable Long courseId) {
        Object result = reservationService.findActivePersonalReservationsByCourseId(courseId);

        if (result instanceof String) {
            String message = (String) result;
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        @SuppressWarnings("unchecked")
        List<CourseReservation> reservations = (List<CourseReservation>) result;

        if (reservations == null || reservations.isEmpty()) {
            return new ResponseEntity<>("此私人教練課程目前沒有有效預約紀錄", HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    @GetMapping("/course/{courseId}/active")
    public ResponseEntity<?> getActiveReservationsByCourseId(@PathVariable Long courseId) {
        Map<String, Object> courseSummary = reservationService.getCourseReservationSummary(courseId);
        if (courseSummary == null) {
            return new ResponseEntity<>("找不到課程資料", HttpStatus.BAD_REQUEST);
        }

        List<CourseReservation> reservations = reservationService.findActiveReservationsByCourseId(courseId);
        if (reservations == null || reservations.isEmpty()) {
            return new ResponseEntity<>("此課程目前沒有有效預約紀錄", HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    @GetMapping("/course/{courseId}/status/{status}")
    public ResponseEntity<?> getReservationsByCourseIdAndStatus(@PathVariable Long courseId,
            @PathVariable String status) {
        if (!isValidStatus(status)) {
            return new ResponseEntity<>("status 只能是 reserved、cancelled、completed、no_show", HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> courseSummary = reservationService.getCourseReservationSummary(courseId);
        if (courseSummary == null) {
            return new ResponseEntity<>("找不到課程資料", HttpStatus.BAD_REQUEST);
        }

        List<CourseReservation> reservations = reservationService.findReservationsByCourseIdAndStatus(courseId, status);
        if (reservations == null || reservations.isEmpty()) {
            return new ResponseEntity<>("此課程目前沒有 " + status + " 狀態的預約紀錄", HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> reserveCourse(@RequestBody Map<String, Object> request) {
        if (request == null) {
            return new ResponseEntity<>("request body 不可為空", HttpStatus.BAD_REQUEST);
        }

        if (!request.containsKey("courseId") || request.get("courseId") == null
                || request.get("courseId").toString().trim().isEmpty()) {
            return new ResponseEntity<>("courseId 不可為空", HttpStatus.BAD_REQUEST);
        }

        if (!request.containsKey("memberId") || request.get("memberId") == null
                || request.get("memberId").toString().trim().isEmpty()) {
            return new ResponseEntity<>("memberId 不可為空", HttpStatus.BAD_REQUEST);
        }

        Long courseId;
        Long memberId;
        String remark = request.get("remark") != null ? request.get("remark").toString() : null;

        try {
            courseId = Long.valueOf(request.get("courseId").toString());
            memberId = Long.valueOf(request.get("memberId").toString());
        } catch (Exception e) {
            return new ResponseEntity<>("courseId 與 memberId 必須為數字", HttpStatus.BAD_REQUEST);
        }

        String result = reservationService.reserveCourse(courseId, memberId, remark);

        if ("預約成功".equals(result) || "預約成功(已恢復先前取消的預約)".equals(result)) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/personal")
    public ResponseEntity<String> reservePersonalCourse(@RequestBody Map<String, Object> request) {
        if (request == null) {
            return new ResponseEntity<>("request body 不可為空", HttpStatus.BAD_REQUEST);
        }

        if (!request.containsKey("courseId") || request.get("courseId") == null
                || request.get("courseId").toString().trim().isEmpty()) {
            return new ResponseEntity<>("courseId 不可為空", HttpStatus.BAD_REQUEST);
        }

        if (!request.containsKey("memberId") || request.get("memberId") == null
                || request.get("memberId").toString().trim().isEmpty()) {
            return new ResponseEntity<>("memberId 不可為空", HttpStatus.BAD_REQUEST);
        }

        Long courseId;
        Long memberId;
        String remark = request.get("remark") != null ? request.get("remark").toString() : null;

        try {
            courseId = Long.valueOf(request.get("courseId").toString());
            memberId = Long.valueOf(request.get("memberId").toString());
        } catch (Exception e) {
            return new ResponseEntity<>("courseId 與 memberId 必須為數字", HttpStatus.BAD_REQUEST);
        }

        String result = reservationService.reservePersonalCourse(courseId, memberId, remark);

        if ("私人教練課預約成功".equals(result) || "私人教練課預約成功(已恢復先前取消的預約)".equals(result)) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<String> cancelReservation(@PathVariable Long reservationId) {
        String result = reservationService.cancelReservation(reservationId);

        if ("取消預約成功".equals(result)) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/course/{courseId}/summary")
    public ResponseEntity<?> getCourseReservationSummary(@PathVariable Long courseId) {
        Map<String, Object> result = reservationService.getCourseReservationSummary(courseId);

        if (result == null) {
            return new ResponseEntity<>("找不到課程資料", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private List<Map<String, Object>> convertReservationList(List<CourseReservation> reservations) {
        List<Map<String, Object>> result = new ArrayList<>();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (CourseReservation reservation : reservations) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("reservationId", reservation.getReservationId());
            item.put("courseId", reservation.getCourseId());
            item.put("memberId", reservation.getMemberId());
            item.put("status", reservation.getReservationStatus());
            item.put("reservedAt", reservation.getReservedAt() != null ? dateTimeFormat.format(reservation.getReservedAt()) : null);
            item.put("cancelledAt", reservation.getCancelledAt() != null ? dateTimeFormat.format(reservation.getCancelledAt()) : null);
            item.put("remark", reservation.getRemark());
            item.put("usedPtOrderId", reservation.getUsedPtOrderId());
            result.add(item);
        }

        return result;
    }

    private List<Map<String, Object>> convertCourseList(List<Course> courses) {
        List<Map<String, Object>> result = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        for (Course course : courses) {
            Map<String, Object> item = new LinkedHashMap<>();
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

            int capacity = course.getCapacity() == null ? 0 : course.getCapacity();
            int enrolled = course.getEnrolledCount() == null ? 0 : course.getEnrolledCount();
            item.put("remainingSeats", capacity - enrolled);

            item.put("status", course.getStatus());
            result.add(item);
        }

        return result;
    }

    private boolean isValidStatus(String status) {
        return "reserved".equalsIgnoreCase(status)
                || "cancelled".equalsIgnoreCase(status)
                || "completed".equalsIgnoreCase(status)
                || "no_show".equalsIgnoreCase(status);
    }
}
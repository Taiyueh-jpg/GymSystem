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
import com.team.dao.MemberRepository;
import com.team.service.ReservationService;

@RestController
@RequestMapping({"/reservations", "/api/reservations"})
@CrossOrigin
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private MemberRepository memberRepository;

    /*
     * 重要規則：
     * ------------------------------------------------------------
     * 前端 AngularJS 的 GET 查詢會預期清單 API 回傳 JSON Array。
     *
     * 所以：
     * 1. 查不到會員 / 查不到課程：回 BAD_REQUEST + 文字
     * 2. 查詢成功但沒有資料：回 []，不要回中文純文字
     * 3. POST / PUT 操作成功或失敗：可以回文字，前端已使用 transformResponse 接收
     */

    // 查某會員所有預約紀錄
    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> getReservationsByMemberId(@PathVariable Long memberId) {
        Map<String, Object> memberSummary = reservationService.getMemberReservationSummary(memberId);

        if (memberSummary == null) {
            return new ResponseEntity<>("找不到會員資料", HttpStatus.BAD_REQUEST);
        }

        List<CourseReservation> reservations = reservationService.findReservationsByMemberId(memberId);

        if (reservations == null || reservations.isEmpty()) {
            return new ResponseEntity<>(emptyList(), HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    // 查某會員有效預約紀錄
    @GetMapping("/member/{memberId}/active")
    public ResponseEntity<?> getActiveReservationsByMemberId(@PathVariable Long memberId) {
        Map<String, Object> memberSummary = reservationService.getMemberReservationSummary(memberId);

        if (memberSummary == null) {
            return new ResponseEntity<>("找不到會員資料", HttpStatus.BAD_REQUEST);
        }

        List<CourseReservation> reservations = reservationService.findActiveReservationsByMemberId(memberId);

        if (reservations == null || reservations.isEmpty()) {
            return new ResponseEntity<>(emptyList(), HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    // 查某會員某狀態預約紀錄
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
            return new ResponseEntity<>(emptyList(), HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    // 查某會員預約統計
    @GetMapping("/member/{memberId}/summary")
    public ResponseEntity<?> getMemberReservationSummary(@PathVariable Long memberId) {
        Map<String, Object> result = reservationService.getMemberReservationSummary(memberId);

        if (result == null) {
            return new ResponseEntity<>("找不到會員資料", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 查會員剩餘私教堂數
    @GetMapping("/member/{memberId}/remaining-pt-sessions")
    public ResponseEntity<?> getMemberRemainingPtSessions(@PathVariable Long memberId) {
        Map<String, Object> result = reservationService.getMemberRemainingPtSessions(memberId);

        if (result == null) {
            return new ResponseEntity<>("找不到會員資料", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 查會員可預約私人教練課程
    @GetMapping("/member/{memberId}/available-personal-courses")
    public ResponseEntity<?> getAvailablePersonalCoursesForMember(@PathVariable Long memberId) {
        Object result = reservationService.findAvailablePersonalCoursesForMember(memberId);

        if (result instanceof String) {
            String message = (String) result;

            if ("找不到會員資料".equals(message) || "此會員目前不可預約課程".equals(message)) {
                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(emptyList(), HttpStatus.OK);
        }

        @SuppressWarnings("unchecked")
        List<Course> courses = (List<Course>) result;

        if (courses == null || courses.isEmpty()) {
            return new ResponseEntity<>(emptyList(), HttpStatus.OK);
        }

        return new ResponseEntity<>(convertCourseList(courses), HttpStatus.OK);
    }

    // 查某課程所有預約紀錄
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getReservationsByCourseId(@PathVariable Long courseId) {
        Map<String, Object> courseSummary = reservationService.getCourseReservationSummary(courseId);

        if (courseSummary == null) {
            return new ResponseEntity<>("找不到課程資料", HttpStatus.BAD_REQUEST);
        }

        List<CourseReservation> reservations = reservationService.findReservationsByCourseId(courseId);

        if (reservations == null || reservations.isEmpty()) {
            return new ResponseEntity<>(emptyList(), HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    // 查某私人教練課所有預約紀錄
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
            return new ResponseEntity<>(emptyList(), HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    // 查某私人教練課有效預約紀錄
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
            return new ResponseEntity<>(emptyList(), HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    // 查某課程有效預約紀錄
    @GetMapping("/course/{courseId}/active")
    public ResponseEntity<?> getActiveReservationsByCourseId(@PathVariable Long courseId) {
        Map<String, Object> courseSummary = reservationService.getCourseReservationSummary(courseId);

        if (courseSummary == null) {
            return new ResponseEntity<>("找不到課程資料", HttpStatus.BAD_REQUEST);
        }

        List<CourseReservation> reservations = reservationService.findActiveReservationsByCourseId(courseId);

        if (reservations == null || reservations.isEmpty()) {
            return new ResponseEntity<>(emptyList(), HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    // 查某課程某狀態預約紀錄
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
            return new ResponseEntity<>(emptyList(), HttpStatus.OK);
        }

        return new ResponseEntity<>(convertReservationList(reservations), HttpStatus.OK);
    }

    // 預約團體課
    @PostMapping
    public ResponseEntity<String> reserveCourse(@RequestBody Map<String, Object> request) {
        if (request == null) {
            return new ResponseEntity<>("request body 不可為空", HttpStatus.BAD_REQUEST);
        }

        if (!request.containsKey("courseId") ||
                request.get("courseId") == null ||
                request.get("courseId").toString().trim().isEmpty()) {
            return new ResponseEntity<>("courseId 不可為空", HttpStatus.BAD_REQUEST);
        }

        if (!request.containsKey("memberId") ||
                request.get("memberId") == null ||
                request.get("memberId").toString().trim().isEmpty()) {
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
        }

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    // 預約私人教練課
    @PostMapping("/personal")
    public ResponseEntity<String> reservePersonalCourse(@RequestBody Map<String, Object> request) {
        if (request == null) {
            return new ResponseEntity<>("request body 不可為空", HttpStatus.BAD_REQUEST);
        }

        if (!request.containsKey("courseId") ||
                request.get("courseId") == null ||
                request.get("courseId").toString().trim().isEmpty()) {
            return new ResponseEntity<>("courseId 不可為空", HttpStatus.BAD_REQUEST);
        }

        if (!request.containsKey("memberId") ||
                request.get("memberId") == null ||
                request.get("memberId").toString().trim().isEmpty()) {
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

        if ("私人教練課預約成功".equals(result) ||
                "私人教練課預約成功(已恢復先前取消的預約)".equals(result)) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    // 取消預約
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<String> cancelReservation(@PathVariable Long reservationId) {
        String result = reservationService.cancelReservation(reservationId);

        if ("取消預約成功".equals(result)) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    // 後台單筆強制取消：供停課流程備援使用，不套用會員端團課 2 小時取消限制。
    @PutMapping("/{reservationId}/admin-cancel")
    public ResponseEntity<String> cancelReservationByAdmin(@PathVariable Long reservationId) {
        String result = reservationService.cancelReservationByAdmin(reservationId);

        if ("取消預約成功".equals(result)) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    // 後台停課用：批次取消某課程所有有效預約，私教會沿用服務層退堂數邏輯。
    @PutMapping("/course/{courseId}/admin-cancel-active")
    public ResponseEntity<?> cancelActiveReservationsForCourseByAdmin(@PathVariable Long courseId) {
        Map<String, Object> result = reservationService.cancelActiveReservationsForCourseByAdmin(courseId);

        if (Boolean.TRUE.equals(result.get("success"))) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    // 查某課程預約統計
    @GetMapping("/course/{courseId}/summary")
    public ResponseEntity<?> getCourseReservationSummary(@PathVariable Long courseId) {
        Map<String, Object> result = reservationService.getCourseReservationSummary(courseId);

        if (result == null) {
            return new ResponseEntity<>("找不到課程資料", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 給清單查詢 API 使用：查詢成功但沒有資料時，回傳空陣列 []
    private List<Map<String, Object>> emptyList() {
        return new ArrayList<>();
    }

    private List<Map<String, Object>> convertReservationList(List<CourseReservation> reservations) {
        List<Map<String, Object>> result = new ArrayList<>();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (CourseReservation reservation : reservations) {
            Map<String, Object> item = new LinkedHashMap<>();

            item.put("reservationId", reservation.getReservationId());
            item.put("courseId", reservation.getCourseId());
            item.put("memberId", reservation.getMemberId());

            // 後台課程管理需要直接辨識預約學員，所以在預約清單補上會員基本資料。
            memberRepository.findById(reservation.getMemberId()).ifPresent(member -> {
                item.put("memberName", member.getName());
                item.put("memberEmail", member.getEmail());
                item.put("memberMobile", member.getMobile());
                item.put("memberStatus", member.getStatus());
            });

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

            item.put("remainingSeats", Math.max(0, capacity - enrolled));
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

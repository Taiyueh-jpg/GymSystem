package com.team.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.model.Course;
import com.team.model.CourseRepository;
import com.team.model.CourseReservation;
import com.team.model.CourseReservationRepository;
import com.team.model.Member;
import com.team.dao.MemberRepository;
import com.team.model.PtOrder;
import com.team.model.PtOrderRepository;

@Service
public class ReservationService {

    @Autowired
    private CourseReservationRepository reservationRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PtOrderRepository ptOrderRepository;

    public List<CourseReservation> findReservationsByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId);
    }

    public List<CourseReservation> findReservationsByCourseId(Long courseId) {
        return reservationRepository.findByCourseId(courseId);
    }

    public List<CourseReservation> findActiveReservationsByMemberId(Long memberId) {
        return reservationRepository.findByMemberIdAndReservationStatusOrderByReservationIdAsc(memberId, "reserved");
    }

    public List<CourseReservation> findActiveReservationsByCourseId(Long courseId) {
        return reservationRepository.findByCourseIdAndReservationStatusOrderByReservationIdAsc(courseId, "reserved");
    }

    public List<CourseReservation> findReservationsByMemberIdAndStatus(Long memberId, String status) {
        return reservationRepository.findByMemberIdAndReservationStatusOrderByReservationIdAsc(memberId, status);
    }

    public List<CourseReservation> findReservationsByCourseIdAndStatus(Long courseId, String status) {
        return reservationRepository.findByCourseIdAndReservationStatusOrderByReservationIdAsc(courseId, status);
    }

    public Object findPersonalReservationsByCourseId(Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            return "找不到課程資料";
        }

        Course course = courseOpt.get();

        if (!"personal".equalsIgnoreCase(course.getCourseType())) {
            return "此課程不是私人教練課程";
        }

        return reservationRepository.findByCourseId(courseId);
    }

    public Object findActivePersonalReservationsByCourseId(Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            return "找不到課程資料";
        }

        Course course = courseOpt.get();

        if (!"personal".equalsIgnoreCase(course.getCourseType())) {
            return "此課程不是私人教練課程";
        }

        return reservationRepository.findByCourseIdAndReservationStatusOrderByReservationIdAsc(courseId, "reserved");
    }

    @Transactional
    public String reserveCourse(Long courseId, Long memberId, String remark) {
        Date now = new Date();

        Optional<Member> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            return "找不到會員資料";
        }

        Member member = memberOpt.get();

        if (!canReserve(member)) {
            return "此會員目前不可預約課程";
        }

        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            return "找不到課程資料";
        }

        Course course = courseOpt.get();

        if (!"group".equalsIgnoreCase(course.getCourseType())) {
            return "此課程不是團體課程，私人教練課請使用私人教練預約功能";
        }

        if (course.getStatus() == null || course.getStatus() != 1) {
            return "此課程目前不可預約";
        }

        if (isCourseStarted(course, now)) {
            return "課程已開始，無法預約";
        }

        Optional<CourseReservation> existingOpt = reservationRepository.findByCourseIdAndMemberId(courseId, memberId);

        if (existingOpt.isPresent()) {
            CourseReservation existing = existingOpt.get();

            if ("reserved".equalsIgnoreCase(existing.getReservationStatus())) {
                return "您已預約過這堂課";
            }

            if ("cancelled".equalsIgnoreCase(existing.getReservationStatus())) {
                int enrolled = course.getEnrolledCount() == null ? 0 : course.getEnrolledCount();
                int capacity = course.getCapacity() == null ? 0 : course.getCapacity();

                if (enrolled >= capacity) {
                    return "此課程已額滿";
                }

                existing.setReservationStatus("reserved");
                existing.setReservedAt(now);
                existing.setCancelledAt(null);
                existing.setRemark(remark);
                existing.setUpdatedAt(now);

                reservationRepository.save(existing);

                course.setEnrolledCount(enrolled + 1);
                course.setUpdatedAt(now);
                courseRepository.save(course);

                return "預約成功(已恢復先前取消的預約)";
            }

            return "此預約紀錄目前不可重新預約";
        }

        int enrolled = course.getEnrolledCount() == null ? 0 : course.getEnrolledCount();
        int capacity = course.getCapacity() == null ? 0 : course.getCapacity();

        if (enrolled >= capacity) {
            return "此課程已額滿";
        }

        CourseReservation reservation = new CourseReservation();
        reservation.setCourseId(courseId);
        reservation.setMemberId(memberId);
        reservation.setReservationStatus("reserved");
        reservation.setReservedAt(now);
        reservation.setCancelledAt(null);
        reservation.setRemark(remark);
        reservation.setUsedPtOrderId(null);
        reservation.setCreatedAt(now);
        reservation.setUpdatedAt(now);

        reservationRepository.save(reservation);

        course.setEnrolledCount(enrolled + 1);
        course.setUpdatedAt(now);
        courseRepository.save(course);

        return "預約成功";
    }

    @Transactional
    public String reservePersonalCourse(Long courseId, Long memberId, String remark) {
        Date now = new Date();

        Optional<Member> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            return "找不到會員資料";
        }

        Member member = memberOpt.get();

        if (!canReserve(member)) {
            return "此會員目前不可預約課程";
        }

        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            return "找不到課程資料";
        }

        Course course = courseOpt.get();

        if (!"personal".equalsIgnoreCase(course.getCourseType())) {
            return "此課程不是私人教練課程";
        }

        if (course.getStatus() == null || course.getStatus() != 1) {
            return "此課程目前不可預約";
        }

        if (isCourseStarted(course, now)) {
            return "課程已開始，無法預約";
        }

        Optional<CourseReservation> existingOpt = reservationRepository.findByCourseIdAndMemberId(courseId, memberId);

        if (existingOpt.isPresent()) {
            CourseReservation existing = existingOpt.get();

            if ("reserved".equalsIgnoreCase(existing.getReservationStatus())) {
                return "您已預約過這堂私人教練課";
            }

            if (!"cancelled".equalsIgnoreCase(existing.getReservationStatus())) {
                return "此私人教練課預約紀錄目前不可重新預約";
            }

            int enrolled = course.getEnrolledCount() == null ? 0 : course.getEnrolledCount();
            int capacity = course.getCapacity() == null ? 0 : course.getCapacity();

            if (enrolled >= capacity) {
                return "此課程已額滿";
            }

            PtOrder usableOrder = findFirstUsablePtOrder(memberId, now);
            if (usableOrder == null) {
                return "私教堂數不足，請先購買方案";
            }

            deductOneSession(usableOrder, now);

            existing.setReservationStatus("reserved");
            existing.setReservedAt(now);
            existing.setCancelledAt(null);
            existing.setRemark(remark);
            existing.setUsedPtOrderId(usableOrder.getPtOrderId());
            existing.setUpdatedAt(now);

            reservationRepository.save(existing);

            course.setEnrolledCount(enrolled + 1);
            course.setUpdatedAt(now);
            courseRepository.save(course);

            return "私人教練課預約成功(已恢復先前取消的預約)";
        }

        int enrolled = course.getEnrolledCount() == null ? 0 : course.getEnrolledCount();
        int capacity = course.getCapacity() == null ? 0 : course.getCapacity();

        if (enrolled >= capacity) {
            return "此課程已額滿";
        }

        PtOrder usableOrder = findFirstUsablePtOrder(memberId, now);
        if (usableOrder == null) {
            return "私教堂數不足，請先購買方案";
        }

        deductOneSession(usableOrder, now);

        CourseReservation reservation = new CourseReservation();
        reservation.setCourseId(courseId);
        reservation.setMemberId(memberId);
        reservation.setReservationStatus("reserved");
        reservation.setReservedAt(now);
        reservation.setCancelledAt(null);
        reservation.setRemark(remark);
        reservation.setUsedPtOrderId(usableOrder.getPtOrderId());
        reservation.setCreatedAt(now);
        reservation.setUpdatedAt(now);

        reservationRepository.save(reservation);

        course.setEnrolledCount(enrolled + 1);
        course.setUpdatedAt(now);
        courseRepository.save(course);

        return "私人教練課預約成功";
    }

    @Transactional
    public String cancelReservation(Long reservationId) {
        Date now = new Date();

        Optional<CourseReservation> reservationOpt = reservationRepository.findById(reservationId);
        if (reservationOpt.isEmpty()) {
            return "找不到預約資料";
        }

        CourseReservation reservation = reservationOpt.get();

        if (!"reserved".equalsIgnoreCase(reservation.getReservationStatus())) {
            return "只有 reserved 狀態的預約可以取消";
        }

        Optional<Course> courseOpt = courseRepository.findById(reservation.getCourseId());
        if (courseOpt.isEmpty()) {
            return "找不到對應課程資料";
        }

        Course course = courseOpt.get();

        if (isCourseStarted(course, now)) {
            return "課程已開始，無法取消";
        }

        reservation.setReservationStatus("cancelled");
        reservation.setCancelledAt(now);
        reservation.setUpdatedAt(now);
        reservationRepository.save(reservation);

        int enrolled = course.getEnrolledCount() == null ? 0 : course.getEnrolledCount();
        course.setEnrolledCount(Math.max(0, enrolled - 1));
        course.setUpdatedAt(now);
        courseRepository.save(course);

        if ("personal".equalsIgnoreCase(course.getCourseType()) && reservation.getUsedPtOrderId() != null) {
            Optional<PtOrder> orderOpt = ptOrderRepository.findById(reservation.getUsedPtOrderId());

            if (orderOpt.isPresent()) {
                PtOrder order = orderOpt.get();
                restoreOneSession(order, now);
            }
        }

        return "取消預約成功";
    }

    private PtOrder findFirstUsablePtOrder(Long memberId, Date now) {
        List<PtOrder> usableOrders = ptOrderRepository.findUsablePaidOrdersByMemberId(memberId, now);

        if (usableOrders == null || usableOrders.isEmpty()) {
            return null;
        }

        return usableOrders.get(0);
    }

    private void deductOneSession(PtOrder order, Date now) {
        int remaining = order.getRemainingSessions() == null ? 0 : order.getRemainingSessions();
        int used = order.getUsedSessions() == null ? 0 : order.getUsedSessions();

        if (remaining <= 0) {
            return;
        }

        order.setRemainingSessions(remaining - 1);
        order.setUsedSessions(used + 1);
        order.setUpdatedAt(now);

        ptOrderRepository.save(order);
    }

    private void restoreOneSession(PtOrder order, Date now) {
        int total = order.getTotalSessions() == null ? 0 : order.getTotalSessions();
        int remaining = order.getRemainingSessions() == null ? 0 : order.getRemainingSessions();
        int used = order.getUsedSessions() == null ? 0 : order.getUsedSessions();

        if (remaining < total) {
            order.setRemainingSessions(remaining + 1);
        }

        if (used > 0) {
            order.setUsedSessions(used - 1);
        }

        order.setUpdatedAt(now);

        ptOrderRepository.save(order);
    }

    private boolean canReserve(Member member) {
        if (member == null) {
            return false;
        }

        if (member.getStatus() == null) {
            return false;
        }

        return member.getStatus() == 1;
    }

    private boolean isCourseStarted(Course course, Date now) {
        if (course == null || course.getCourseDate() == null || course.getStartTime() == null || now == null) {
            return false;
        }

        Date courseStart = mergeDateAndTime(course.getCourseDate(), course.getStartTime());

        return !courseStart.after(now);
    }

    @SuppressWarnings("deprecation")
    private Date mergeDateAndTime(Date datePart, Date timePart) {
        Date result = new Date(datePart.getTime());

        result.setHours(timePart.getHours());
        result.setMinutes(timePart.getMinutes());
        result.setSeconds(timePart.getSeconds());

        return result;
    }

    public Map<String, Object> getCourseReservationSummary(Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            return null;
        }

        Map<String, Object> result = new LinkedHashMap<>();

        int reservedCount = reservationRepository.findByCourseIdAndReservationStatus(courseId, "reserved").size();
        int cancelledCount = reservationRepository.findByCourseIdAndReservationStatus(courseId, "cancelled").size();
        int completedCount = reservationRepository.findByCourseIdAndReservationStatus(courseId, "completed").size();
        int noShowCount = reservationRepository.findByCourseIdAndReservationStatus(courseId, "no_show").size();

        result.put("courseId", courseId);
        result.put("reservedCount", reservedCount);
        result.put("cancelledCount", cancelledCount);
        result.put("completedCount", completedCount);
        result.put("noShowCount", noShowCount);
        result.put("totalRecords", reservedCount + cancelledCount + completedCount + noShowCount);

        return result;
    }

    public Map<String, Object> getMemberReservationSummary(Long memberId) {
        Optional<Member> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            return null;
        }

        Map<String, Object> result = new LinkedHashMap<>();

        int reservedCount = reservationRepository.findByMemberIdAndReservationStatus(memberId, "reserved").size();
        int cancelledCount = reservationRepository.findByMemberIdAndReservationStatus(memberId, "cancelled").size();
        int completedCount = reservationRepository.findByMemberIdAndReservationStatus(memberId, "completed").size();
        int noShowCount = reservationRepository.findByMemberIdAndReservationStatus(memberId, "no_show").size();

        result.put("memberId", memberId);
        result.put("reservedCount", reservedCount);
        result.put("cancelledCount", cancelledCount);
        result.put("completedCount", completedCount);
        result.put("noShowCount", noShowCount);
        result.put("totalRecords", reservedCount + cancelledCount + completedCount + noShowCount);

        return result;
    }

    public Object findAvailablePersonalCoursesForMember(Long memberId) {
        Optional<Member> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            return "找不到會員資料";
        }

        Member member = memberOpt.get();

        if (!canReserve(member)) {
            return "此會員目前不可預約課程";
        }

        List<Course> courses = courseRepository.findByCourseTypeAndStatus("personal", 1);
        List<Course> availableCourses = new ArrayList<>();

        Date now = new Date();

        for (Course course : courses) {
            int enrolled = course.getEnrolledCount() == null ? 0 : course.getEnrolledCount();
            int capacity = course.getCapacity() == null ? 0 : course.getCapacity();

            if (enrolled < capacity && !isCourseStarted(course, now)) {
                availableCourses.add(course);
            }
        }

        return availableCourses;
    }

    public Map<String, Object> getMemberRemainingPtSessions(Long memberId) {
        Optional<Member> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            return null;
        }

        Date now = new Date();
        Long remaining = ptOrderRepository.sumRemainingSessionsByMemberId(memberId, now);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("memberId", memberId);
        result.put("remainingSessions", remaining == null ? 0 : remaining.intValue());

        return result;
    }
}
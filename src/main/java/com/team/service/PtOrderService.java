package com.team.service;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.team.model.Member;
import com.team.dao.MemberRepository;
import com.team.model.PtOrder;
import com.team.model.PtOrderRepository;
import com.team.model.PtPackage;
import com.team.model.PtPackageRepository;

@Service
public class PtOrderService {

    @Autowired
    private PtOrderRepository ptOrderRepository;

    @Autowired
    private PtPackageRepository ptPackageRepository;

    @Autowired
    private MemberRepository memberRepository;

    public Object purchasePackage(Long memberId, Long packageId) {
        Date now = new Date();

        if (memberId == null) {
            return "memberId 不可為空";
        }

        if (packageId == null) {
            return "packageId 不可為空";
        }

        Optional<Member> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            return "找不到會員資料";
        }

        Member member = memberOpt.get();

        if (member.getStatus() == null || member.getStatus() != 1) {
            return "此會員目前不可購買私教方案";
        }

        Optional<PtPackage> packageOpt = ptPackageRepository.findById(packageId);
        if (packageOpt.isEmpty()) {
            return "找不到私教方案";
        }

        PtPackage ptPackage = packageOpt.get();

        if (ptPackage.getStatus() == null || ptPackage.getStatus() != 1) {
            return "此私教方案目前未開放購買";
        }

        if (ptPackage.getSessionCount() == null || ptPackage.getSessionCount() <= 0) {
            return "此私教方案堂數設定異常";
        }

        if (ptPackage.getPrice() == null) {
            return "此私教方案金額設定異常";
        }

        PtOrder order = new PtOrder();
        order.setMemberId(memberId);
        order.setPackageId(packageId);
        order.setTotalSessions(ptPackage.getSessionCount());
        order.setUsedSessions(0);
        order.setRemainingSessions(ptPackage.getSessionCount());
        order.setTotalAmount(ptPackage.getPrice());
        order.setOrderStatus("paid");
        order.setPurchasedAt(now);
        order.setExpiredAt(addMonths(now, 6));
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        PtOrder savedOrder = ptOrderRepository.save(order);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "購買私教方案成功");
        result.put("ptOrderId", savedOrder.getPtOrderId());
        result.put("memberId", savedOrder.getMemberId());
        result.put("packageId", savedOrder.getPackageId());
        result.put("packageName", ptPackage.getPackageName());
        result.put("totalSessions", savedOrder.getTotalSessions());
        result.put("usedSessions", savedOrder.getUsedSessions());
        result.put("remainingSessions", savedOrder.getRemainingSessions());
        result.put("totalAmount", savedOrder.getTotalAmount());
        result.put("orderStatus", savedOrder.getOrderStatus());
        result.put("purchasedAt", savedOrder.getPurchasedAt());
        result.put("expiredAt", savedOrder.getExpiredAt());

        return result;
    }

    private Date addMonths(Date date, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }
}
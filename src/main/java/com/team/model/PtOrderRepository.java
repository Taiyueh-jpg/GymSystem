package com.team.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PtOrderRepository extends JpaRepository<PtOrder, Long> {

    List<PtOrder> findByMemberId(Long memberId);

    List<PtOrder> findByMemberIdAndOrderStatus(Long memberId, String orderStatus);

    @Query("""
            SELECT o
            FROM PtOrder o
            WHERE o.memberId = :memberId
              AND o.orderStatus = 'paid'
              AND o.remainingSessions > 0
              AND (o.expiredAt IS NULL OR o.expiredAt > :now)
            ORDER BY o.purchasedAt ASC, o.ptOrderId ASC
            """)
    List<PtOrder> findUsablePaidOrdersByMemberId(@Param("memberId") Long memberId, @Param("now") Date now);

    @Query("""
            SELECT COALESCE(SUM(o.remainingSessions), 0)
            FROM PtOrder o
            WHERE o.memberId = :memberId
              AND o.orderStatus = 'paid'
              AND o.remainingSessions > 0
              AND (o.expiredAt IS NULL OR o.expiredAt > :now)
            """)
    Long sumRemainingSessionsByMemberId(@Param("memberId") Long memberId, @Param("now") Date now);
}
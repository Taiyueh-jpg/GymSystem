package com.team.model;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "pt_order")
@Data
public class PtOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pt_order_id")
    private Long ptOrderId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Column(name = "total_sessions", nullable = false)
    private Integer totalSessions;

    @Column(name = "used_sessions", nullable = false)
    private Integer usedSessions;

    @Column(name = "remaining_sessions", nullable = false)
    private Integer remainingSessions;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "order_status", nullable = false, length = 20)
    private String orderStatus;

    @Column(name = "purchased_at", nullable = false)
    private Date purchasedAt;

    @Column(name = "expired_at")
    private Date expiredAt;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;
}
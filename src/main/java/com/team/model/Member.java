package com.team.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 👤 會員資料實體 (Member Entity)
 * 負責人：芳羽
 * * 技術細節：
 * 1. 嚴格對應資料庫結構：包含 created_at 與 updated_at。
 * 2. 使用 Lombok @Data 簡化 Getter/Setter。
 * 3. 針對資料庫自動產生的時間戳記，使用 insertable=false 與 updatable=false 避免 Java 端覆蓋資料庫邏輯。
 */
@Entity
@Table(name = "member")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "mobile", length = 255)
    private String mobile;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    // 💡 確保時間完全交由資料庫 (MySQL) 控制，避免 Java 端覆蓋
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // 💡 確保時間完全交由資料庫 (MySQL) 控制，避免 Java 端覆蓋
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
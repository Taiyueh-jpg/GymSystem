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

    /**
     * 📅 建立時間
     * insertable = false, updatable = false 讓 Hibernate 在執行 SQL 時
     * 忽略此欄位，交由資料庫 DEFAULT CURRENT_TIMESTAMP 處理。
     */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 🔄 更新時間
     * 同樣交由資料庫的 ON UPDATE CURRENT_TIMESTAMP 自動處理，
     * 確保資料庫與程式端的行為一致，避免時區或同步問題。
     */
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
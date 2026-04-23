package com.team.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 👑 管理員/教練實體 (Admin Entity)
 * 負責人：芳羽 (隊長 - 帳號與權限核心)
 * * 說明：此表負責儲存內部員工帳號，透過 role 欄位區分「管理者(admin)」與「教練(coach)」。
 */
@Entity
@Table(name = "admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Builder.Default
    @Column(name = "role", nullable = false, length = 50)
    private String role = "coach"; // 預設角色為教練

    @Builder.Default
    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1:在職/正常, 0:離職/停用

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
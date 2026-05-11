package com.team.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 👑 管理員/教練實體 (Admin Entity)
 * 負責人：芳羽 (隊長 - 帳號與權限核心)
 *
 * 說明：此表負責儲存內部員工帳號，透過 role 欄位區分「管理者(admin)」與「教練(coach)」。
 *
 * ✅ [修改紀錄 - 資安修補]
 *    問題：password 欄位原本會被序列化進 JSON 回傳給前端，造成密碼明文外洩。
 *    修法：在 password 欄位上加 @JsonIgnore，讓 Jackson 序列化時自動忽略此欄位。
 *    影響範圍：所有回傳 Admin 物件的 API（含 /api/articles/list 內巢狀的 admin 物件），
 *             password 欄位都將不再出現在 JSON 回應中，無需逐一修改 Controller。
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

    /**
     * 🔒 [資安] 密碼欄位
     * @JsonIgnore：告訴 Jackson 序列化時完全跳過此欄位。
     * 無論透過哪支 API 回傳 Admin 物件，password 都不會出現在 JSON 回應裡。
     * 注意：反序列化（前端傳 JSON 進來）依然可以寫入，登入時接收密碼不受影響。
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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

    // 📅 建立時間：交由資料庫 DEFAULT CURRENT_TIMESTAMP 自動處理
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // 🔄 更新時間：交由資料庫 ON UPDATE CURRENT_TIMESTAMP 自動處理
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}

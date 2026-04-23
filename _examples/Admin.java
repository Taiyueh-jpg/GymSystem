package com.team.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

//==========================================
//👑 同學 A 負責區域：身份與權限
//==========================================

@Entity
@Table(name = "admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    @Column(nullable = false)
    private String password;
    
    private String name;
    private String role; // 例如: SUPER_ADMIN, COURSE_ADMIN

    // Getters and Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
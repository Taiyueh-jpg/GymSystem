package com.team.dao;

import com.team.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {  // Long，不是 Integer

    // 後續登入用（先預留）
    // Optional<Admin> findByEmailAndPassword(String email, String password);
}
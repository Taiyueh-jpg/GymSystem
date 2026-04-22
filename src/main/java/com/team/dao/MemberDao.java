package com.team.dao;

import com.team.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberDao extends JpaRepository<Member, Long> {
    
    // 透過 Email 尋找會員 (登入時使用)
    Optional<Member> findByEmail(String email);
    
    // 檢查 Email 是否已經被註冊過 (註冊時防呆用)
    boolean existsByEmail(String email);
}
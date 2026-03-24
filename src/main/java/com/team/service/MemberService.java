package com.team.service;

import com.team.dao.MemberDao;
import com.team.model.Member;
import org.springframework.stereotype.Service;

/**
 * 👑 同學 A 負責區域：會員業務邏輯
 */
@Service
public class MemberService {

    // 注入剛剛寫好的 DAO
    private final MemberDao memberDao;

    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    /**
     * 會員註冊邏輯
     */
    public Member register(Member member) {
        // 1. 檢查 Email 是否已被註冊
        if (memberDao.existsByEmail(member.getEmail())) {
            throw new RuntimeException("此 Email 已經被註冊過了！");
        }
        
        // 2. 密碼加密 (實務上應使用 BCrypt，此處為團隊初期開發簡化)
        // member.setPassword(SecurityUtil.encrypt(member.getPassword()));
        
        // 3. 儲存進資料庫
        return memberDao.save(member);
    }

    /**
     * 會員登入邏輯
     */
    public Member login(String email, String password) {
        Member member = memberDao.findByEmail(email);
        
        // 驗證帳號是否存在與密碼是否正確
        if (member == null || !member.getPassword().equals(password)) {
            throw new RuntimeException("帳號或密碼錯誤！");
        }
        
        return member;
    }
}
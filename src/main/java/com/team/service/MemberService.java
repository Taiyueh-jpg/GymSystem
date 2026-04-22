package com.team.service;

import com.team.dao.MemberDao;
import com.team.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Autowired
    private MemberDao memberDao;

    /**
     * 註冊功能
     */
    public Member register(Member member) {
        // 1. 檢查信箱是否重複
        if (memberDao.existsByEmail(member.getEmail())) {
            throw new RuntimeException("這個 Email 已經被註冊過囉！");
        }
        
        // 2. TODO: 專題後期可以在這裡加入 BCrypt 密碼加密
        // member.setPassword( BCrypt.hashpw(member.getPassword(), BCrypt.gensalt()) );

        // 3. 儲存進資料庫
        return memberDao.save(member);
    }

    /**
     * 登入功能
     */
    public Member login(String email, String password) {
        // 1. 透過 email 去資料庫找人
        Member member = memberDao.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("找不到此帳號，請確認 Email 是否正確！"));

        // 2. 比對密碼 (TODO: 後期若有加密，這裡要改成 BCrypt.checkpw)
        if (!member.getPassword().equals(password)) {
            throw new RuntimeException("密碼錯誤！");
        }

        // 3. 檢查帳號是否被停權
        if (member.getStatus() == 0) {
            throw new RuntimeException("此帳號已被停權，請聯絡管理員。");
        }

        return member; // 登入成功，把這個人的資料交出去
    }
}
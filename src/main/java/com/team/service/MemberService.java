package com.team.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.team.dao.MemberDao;
import com.team.model.Member;
import com.team.model.MemberRegisterDTO;

@Service
public class MemberService {

    @Autowired
    private MemberDao memberDao;
    
 // 處理登入邏輯的方法
    public Member login(String email, String password) {
        
        // 1. 去資料庫尋找這個信箱的會員
        Member member = memberDao.findByEmail(email);
        
        // 2. 判斷有沒有這個人？密碼對不對？
        if (member != null && member.getPassword().equals(password)) {
            System.out.println("🔓 登入成功：放行 " + member.getName());
            return member; // 密碼正確，把會員資料回傳交給櫃台
        } else {
            System.out.println("❌ 登入失敗：帳號或密碼錯誤");
            return null;   // 密碼錯誤或找不到人，回傳空值
        }
    }	
    
    // 處理註冊邏輯的方法
    public void registerNewMember(MemberRegisterDTO dto) {
        // 1. 建立一個全新的實體物件 (準備存進資料庫的格式)
        Member newMember = new Member();
        
        // 2. 把 DTO 裡面的資料，倒進這個實體物件裡
        newMember.setName(dto.getName());
        newMember.setEmail(dto.getEmail());
        newMember.setPassword(dto.getPassword()); // 實務上這裡還要幫密碼加密，我們後續再加
        newMember.setMobile(dto.getPhone());
        
        // 3. 呼叫 DAO，正式存入 MySQL！
        memberDao.save(newMember);
        
        
        
        System.out.println("✅ 會員資料已成功存入 SQL 資料庫！");
    }
}
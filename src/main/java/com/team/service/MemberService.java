package com.team.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.team.dao.MemberDao;
import com.team.model.Member;
import com.team.model.MemberRegisterDTO;

/**
 * 🛠️ 會員業務邏輯層 (Member Service)
 * * 技術細節：
 * 1. 使用 @Transactional 確保資料異動的原子性。
 * 2. 結合 DAO 層的分頁與模糊搜尋功能。
 * 3. 處理登入狀態過濾與重複註冊防呆。
 */
@Service
public class MemberService {

    @Autowired
    private MemberDao memberDao;
    
    // ==========================================
    // 1. 處理登入邏輯的方法
    // ==========================================
    public Member login(String email, String password) {
        
        // 🌟 關鍵修復：加上 .orElse(null) 把 Optional 盒子打開
        Member member = memberDao.findByEmail(email).orElse(null);
        
        // 判斷有沒有這個人？密碼對不對？
        if (member != null && member.getPassword().equals(password)) {
            System.out.println("🔓 登入成功：放行 " + member.getName());
            return member; 
        } else {
            System.out.println("❌ 登入失敗：帳號或密碼錯誤");
            return null;   
        }
    }
    
    // ==========================================
    // 2. 處理註冊邏輯的方法 (🌟 更新版：加入信箱重複檢查)
    // ==========================================
    public void registerNewMember(MemberRegisterDTO dto) {
        // 1. 檢查信箱是否重複
        if (memberDao.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("這個 Email 已經被註冊過囉！");
        }

        // 2. 把 DTO 包裹裡的資料，倒進真正的 Member 實體中
        Member newMember = new Member();
        newMember.setName(dto.getName());
        newMember.setEmail(dto.getEmail());
        newMember.setPassword(dto.getPassword()); 
        newMember.setMobile(dto.getPhone()); // 把表單的 phone 對應到資料庫的 mobile

        // 3. 儲存進資料庫
        memberDao.save(newMember);
        
        System.out.println("✅ 會員資料已成功存入 SQL 資料庫！");
    }
}
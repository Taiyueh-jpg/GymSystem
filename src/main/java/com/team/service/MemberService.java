package com.team.service;

import com.team.dao.MemberRepository;
import com.team.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    private MemberRepository memberRepo;

    /**
     * 🔑 會員登入驗證
     * 開放狀態 0(一般會員) 與 1(付費會員) 登入，僅阻擋 -1(停權)。
     */
    public Member login(String email, String password) {
        // 👉 用 findByEmail，並用 filter 過濾掉停權帳號
        return memberRepo.findByEmail(email)
                .filter(m -> m.getPassword().equals(password)) // 比對密碼
                .filter(m -> m.getStatus() >= 0)               // 確保狀態是 0 或 1，排除 -1(停權)
                .orElseThrow(() -> new RuntimeException("帳號密碼錯誤或帳號已被停權"));
    }

    /**
     * 🔐 會員註冊
     * 包含重複帳號檢查與初始狀態設定。
     */
    @Transactional
    public Member register(Member member) {
        if (memberRepo.existsByEmail(member.getEmail())) {
            throw new RuntimeException("該電子郵件 [ " + member.getEmail() + " ] 已被使用");
        }
        
        // 👉 新註冊預設為「一般會員 (無會籍)」，狀態設為 0
        member.setStatus(0); 
        return memberRepo.save(member);
    }

    /**
     * 👤 取得單一會員個人資料
     */
    public Member getMemberProfile(Long id) {
        return memberRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到編號為 " + id + " 的會員"));
    }

    /**
     * 📝 修改個人資料
     * 僅更新允許變動的欄位 (姓名、電話、地址、生日)。
     */
    @Transactional
    public Member updateProfile(Long id, Member updatedInfo) {
        return memberRepo.findById(id).map(existingMember -> {
            existingMember.setName(updatedInfo.getName());
            existingMember.setMobile(updatedInfo.getMobile());
            existingMember.setAddress(updatedInfo.getAddress());
            existingMember.setBirthday(updatedInfo.getBirthday());
            return memberRepo.save(existingMember);
        }).orElseThrow(() -> new RuntimeException("更新失敗：找不到該會員"));
    }

    /**
     * 🔍 後台管理：多功能分頁搜尋
     * 同時支援姓名與電話的模糊搜尋。
     */
    public Page<Member> searchMembers(String keyword, Pageable pageable) {
        return memberRepo.findByNameContainingOrMobileContaining(keyword, keyword, pageable);
    }

    /**
     * 📋 後台管理：依狀態查詢會員列表
     */
    public Page<Member> getMembersByStatus(Integer status, Pageable pageable) {
        return memberRepo.findByStatus(status, pageable);
    }

    /**
     * 🎂 行銷活動：查詢當月壽星
     * 自動取得當前月份進行查詢。
     */
    public List<Member> getBirthdaysOfCurrentMonth() {
        int currentMonth = LocalDate.now().getMonthValue();
        return memberRepo.findBirthdaysByMonth(currentMonth);
    }

    /**
     * 📊 戰情室數據：計算有效會員總數
     * 供【報表/經營分析】模組調用。
     */
    public long getActiveMemberCount() {
        return memberRepo.countByStatus(1);
    }

    /**
     * 🚫 會員停權/復權處理
     */
    @Transactional
    public void updateMemberStatus(Long id, Integer newStatus) {
        memberRepo.findById(id).ifPresent(m -> {
            m.setStatus(newStatus);
            memberRepo.save(m);
        });
    }
}
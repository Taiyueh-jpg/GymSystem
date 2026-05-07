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

/**
 * 🛠️ 會員業務邏輯層 (Member Service)
 *
 * 本版新增：
 * 1. 手機格式防呆：若有填手機，必須符合 09 開頭 + 8 位數字，共 10 碼。
 * 2. 修改密碼邏輯：確認舊密碼正確、新密碼與確認密碼一致，才允許更新。
 *
 * 注意：
 * 目前專案登入邏輯仍是明文密碼比對，所以此處先維持現有架構。
 * 若之後要改 BCrypt，需要同步修改 register / login / changePassword。
 */
@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepo;

    private static final String MOBILE_PATTERN = "^09\\d{8}$";

    /**
     * 🔑 會員登入驗證
     * 開放狀態 0(一般會員) 與 1(付費會員) 登入，僅阻擋 -1(停權)。
     */
    public Member login(String email, String password) {
        return memberRepo.findByEmail(email)
                .filter(m -> m.getPassword().equals(password))
                .filter(m -> m.getStatus() >= 0)
                .orElseThrow(() -> new RuntimeException("帳號密碼錯誤或帳號已被停權"));
    }

    /**
     * 🔐 會員註冊
     * 包含重複帳號檢查、手機格式防呆與初始狀態設定。
     */
    @Transactional
    public Member register(Member member) {
        if (member.getEmail() == null || member.getEmail().isBlank()) {
            throw new RuntimeException("電子郵件不可空白");
        }

        if (member.getPassword() == null || member.getPassword().isBlank()) {
            throw new RuntimeException("密碼不可空白");
        }

        if (member.getName() == null || member.getName().isBlank()) {
            throw new RuntimeException("姓名不可空白");
        }

        validateMobile(member.getMobile());

        if (memberRepo.existsByEmail(member.getEmail())) {
            throw new RuntimeException("該電子郵件 [ " + member.getEmail() + " ] 已被使用");
        }

        // 新註冊預設為「一般會員 / 無會籍」，狀態設為 0
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
     * 僅更新允許變動的欄位：姓名、電話、地址、生日。
     *
     * 手機格式：
     * - 可空白
     * - 若有填，必須是 09 開頭 + 8 位數字，共 10 碼
     * - 範例：0912345678
     */
    @Transactional
    public Member updateProfile(Long id, Member updatedInfo) {
        return memberRepo.findById(id).map(existingMember -> {

            if (updatedInfo.getName() == null || updatedInfo.getName().isBlank()) {
                throw new RuntimeException("姓名不可空白");
            }

            validateMobile(updatedInfo.getMobile());

            existingMember.setName(updatedInfo.getName());
            existingMember.setMobile(normalizeBlankToNull(updatedInfo.getMobile()));
            existingMember.setAddress(updatedInfo.getAddress());
            existingMember.setBirthday(updatedInfo.getBirthday());

            return memberRepo.save(existingMember);
        }).orElseThrow(() -> new RuntimeException("更新失敗：找不到該會員"));
    }

    /**
     * 🔑 修改會員密碼
     *
     * 防呆規則：
     * 1. 舊密碼不可空白
     * 2. 新密碼不可空白
     * 3. 確認密碼不可空白
     * 4. 新密碼與確認密碼必須一致
     * 5. 舊密碼必須正確
     * 6. 新密碼不可與舊密碼相同
     */
    @Transactional
    public void changePassword(Long memberId, String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new RuntimeException("請輸入目前密碼");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("請輸入新密碼");
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new RuntimeException("請再次輸入新密碼");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("新密碼與再次輸入的新密碼不一致");
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException("新密碼至少需要 6 個字元");
        }

        Member member = memberRepo.findById(memberId)
                .orElseThrow(() -> new RuntimeException("找不到會員資料"));

        if (!member.getPassword().equals(currentPassword)) {
            throw new RuntimeException("目前密碼不正確");
        }

        if (member.getPassword().equals(newPassword)) {
            throw new RuntimeException("新密碼不可與目前密碼相同");
        }

        member.setPassword(newPassword);
        memberRepo.save(member);
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
     */
    public List<Member> getBirthdaysOfCurrentMonth() {
        int currentMonth = LocalDate.now().getMonthValue();
        return memberRepo.findBirthdaysByMonth(currentMonth);
    }

    /**
     * 📊 戰情室數據：計算有效會員總數
     */
    public long getActiveMemberCount() {
        return memberRepo.countByStatus(1);
    }

    /**
     * 🚫 會員停權 / 復權處理
     */
    @Transactional
    public void updateMemberStatus(Long id, Integer newStatus) {
        memberRepo.findById(id).ifPresent(m -> {
            m.setStatus(newStatus);
            memberRepo.save(m);
        });
    }

    /**
     * 手機格式驗證：
     * 可空白；若有填，必須符合 09 開頭 + 8 位數字。
     */
    private void validateMobile(String mobile) {
        if (mobile == null || mobile.isBlank()) {
            return;
        }

        if (!mobile.matches(MOBILE_PATTERN)) {
            throw new RuntimeException("手機格式錯誤，請輸入 09 開頭且共 10 碼的手機號碼，例如：0912345678");
        }
    }

    private String normalizeBlankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
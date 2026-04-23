package com.team.service;

import com.team.dao.AdminRepository;
import com.team.model.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//封裝了後台管理員工的核心邏輯
@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepo;

    /**
     * 🔑 內部員工登入
     */
    public Admin login(String email, String password) {
        return adminRepo.findByEmailAndStatus(email, 1)
                .filter(a -> a.getPassword().equals(password))
                .orElseThrow(() -> new RuntimeException("帳號密碼錯誤或該員工已離職/停權"));
    }

    /**
     * ➕ 新增員工 (教練或管理者)
     * 通常由現有的 Admin 於後台執行
     */
    @Transactional
    public Admin addStaff(Admin admin) {
        if (adminRepo.existsByEmail(admin.getEmail())) {
            throw new RuntimeException("該員工信箱已被註冊使用");
        }
        // 確保預設狀態為在職
        admin.setStatus(1);
        return adminRepo.save(admin);
    }

    /**
     * 👨‍🏫 取得所有「在職教練」清單
     * 💡 隊長提示：這個方法是為了提供給「同學 B (課程預約)」模組使用的。
     */
    public List<Admin> getActiveCoaches() {
        return adminRepo.findByRoleAndStatus("coach", 1);
    }

    /**
     * 🚫 員工離職/停用處理
     */
    @Transactional
    public void updateStaffStatus(Long adminId, Integer newStatus) {
        adminRepo.findById(adminId).ifPresent(a -> {
            a.setStatus(newStatus);
            adminRepo.save(a);
        });
    }
}
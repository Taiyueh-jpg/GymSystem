package com.team.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.team.dao.AdminRepository;
import com.team.model.Admin;

@Service
public class CoachService {

    @Autowired
    private AdminRepository adminRepository;

    // 查全部教練
    public List<Admin> findAllCoaches() {
        return adminRepository.findAll()
                .stream()
                .filter(admin -> admin != null
                        && admin.getRole() != null
                        && "coach".equalsIgnoreCase(admin.getRole()))
                .collect(Collectors.toList());
    }

    // 查全部啟用中的教練
    public List<Admin> findActiveCoaches() {
        return adminRepository.findByRoleAndStatus("coach", 1);
    }

    // 查單一教練
    public Admin findCoachById(Long coachId) {
        Admin admin = adminRepository.findById(coachId).orElse(null);

        if (admin == null) {
            return null;
        }

        // 不是 coach 也視為查無資料
        if (admin.getRole() == null || !"coach".equalsIgnoreCase(admin.getRole())) {
            return null;
        }

        return admin;
    }
}
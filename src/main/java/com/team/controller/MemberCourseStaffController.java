package com.team.controller;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.dao.AdminRepository;
import com.team.dao.MemberRepository;
import com.team.model.Admin;
import com.team.model.Member;

@RestController
@RequestMapping("/api/member-course/staff")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
public class MemberCourseStaffController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private MemberRepository memberRepository;

    @GetMapping("/members")
    public ResponseEntity<?> getMembersForStaffReservation(@RequestParam Long operatorAdminId) {
        Admin operator = adminRepository.findById(operatorAdminId).orElse(null);

        if (operator == null || NumberUtil.notOne(operator.getStatus())) {
            return new ResponseEntity<>("操作人員不存在或帳號未啟用", HttpStatus.FORBIDDEN);
        }

        if (!"admin".equals(operator.getRole()) && !"coach".equals(operator.getRole())) {
            return new ResponseEntity<>("權限不足：僅限教練或管理者操作", HttpStatus.FORBIDDEN);
        }

        List<Map<String, Object>> result = memberRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Member::getMemberId))
                .map(this::convertMember)
                .collect(Collectors.toList());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private Map<String, Object> convertMember(Member member) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("memberId", member.getMemberId());
        item.put("email", member.getEmail());
        item.put("name", member.getName());
        item.put("mobile", member.getMobile());
        item.put("status", member.getStatus());
        return item;
    }

    private static class NumberUtil {
        private static boolean notOne(Integer value) {
            return value == null || value.intValue() != 1;
        }
    }
}

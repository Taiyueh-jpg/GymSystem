package com.team.service;

import com.team.dao.MemberRepository;
import com.team.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 定時任務服務
 *
 */
@Service
public class ScheduledTaskService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailService emailService;

    /**
     * 🎂 每月1號早上9點自動寄送生日優惠券
     *
     * cron 格式：秒 分 時 日 月 星期
     * "0 0 9 1 * *" = 每月1號 09:00:00
     *
     * 寄信對象：當月壽星且 status >= 0（一般會員 + 付費會員，排除停權）
     * 寄信結果寫入 email_log（email_type = 'birthday'）
     */
    @Scheduled(cron = "0 0 9 1 * *")
    public void sendMonthlyBirthdayEmails() {
        int currentMonth = LocalDate.now().getMonthValue();
        List<Member> birthdayMembers = memberRepository.findBirthdaysByMonth(currentMonth);

        for (Member member : birthdayMembers) {
            emailService.sendBirthdayNotification(member);
        }
    }
}

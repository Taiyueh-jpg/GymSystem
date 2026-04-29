package com.team.controller;

import com.team.model.ContactMsg;
import com.team.model.Member;
import com.team.service.ContactMsgService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactMsgController {

    @Autowired
    private ContactMsgService contactMsgService;

    /**
     * POST 請求：送出客服表單
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitContactForm(@RequestBody ContactMsg msg, HttpSession session) {
        try {
            // 🌟 魔法合體：嘗試從 Session 拿出通行證 (確認是不是芳羽模組登入的會員)
            Member currentMember = (Member) session.getAttribute("loggedInMember");

            if (currentMember != null) {
                // 如果有登入：強制把留言的 MemberId 綁定成這個人，覆蓋掉前端亂傳的值！
                msg.setMemberId(currentMember.getMemberId());
                msg.setGuestName(currentMember.getName());
                msg.setGuestEmail(currentMember.getEmail());
            } else {
                // 如果沒登入：檢查前端有沒有乖乖填寫訪客信箱與姓名
                if (msg.getGuestEmail() == null || msg.getGuestName() == null) {
                    return ResponseEntity.badRequest().body("訪客請務必填寫姓名與聯絡信箱！");
                }
            }

            // 呼叫 Service 執行過濾與存檔
            ContactMsg savedMsg = contactMsgService.submitMessage(msg);
            return ResponseEntity.ok("表單送出成功！客服編號：" + savedMsg.getMsgId());

        } catch (RuntimeException e) {
            // 如果觸發髒話過濾器，就會走到這裡，回傳 400 錯誤給前端
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * GET 請求：後台抓取未處理留言
     */
    @GetMapping("/admin/new")
    public ResponseEntity<?> getNewMessages() {
        return ResponseEntity.ok(contactMsgService.getNewMessages());
    }
}

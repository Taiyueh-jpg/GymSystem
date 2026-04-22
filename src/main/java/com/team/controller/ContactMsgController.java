package com.team.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.team.model.ContactMsg;
import com.team.service.ContactMsgService;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
public class ContactMsgController {

    @Autowired
    private ContactMsgService contactMsgService;

    // POST /api/contact/msg
    @PostMapping(value = "/msg", consumes = "multipart/form-data")
    public ResponseEntity<?> createMsg(
            @RequestPart("msg") ContactMsg msg,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            ContactMsg saved = contactMsgService.createMsg(msg, files);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("附件上傳失敗：" + e.getMessage());
        }
    }

    // GET /api/contact/msg (admin 取全部)
    @GetMapping("/msg")
    public ResponseEntity<List<ContactMsg>> getAllMsgs() {
        return ResponseEntity.ok(contactMsgService.getAllMsgs());
    }

    // GET /api/contact/msg/guest?email=xxx (訪客查詢自己的留言)
    @GetMapping("/msg/guest")
    public ResponseEntity<?> getMsgsByGuestEmail(@RequestParam String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("請提供 Email");
        }
        return ResponseEntity.ok(contactMsgService.getMsgsByGuestEmail(email));
    }

    // GET /api/contact/msg/{id}
    @GetMapping("/msg/{id}")
    public ResponseEntity<?> getMsgById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(contactMsgService.getMsgById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/contact/msg/member/{memberId}
    @GetMapping("/msg/member/{memberId}")
    public ResponseEntity<List<ContactMsg>> getMsgsByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(contactMsgService.getMsgsByMember(memberId));
    }

    // GET /api/contact/msg/unread (admin 用)
    @GetMapping("/msg/unread")
    public ResponseEntity<List<ContactMsg>> getUnreadMsgs() {
        return ResponseEntity.ok(contactMsgService.getUnreadMsgs());
    }

    // PUT /api/contact/msg/{id}/read (admin 標記已讀)
    @PutMapping("/msg/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(contactMsgService.markAsRead(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/contact/msg/{id}/reply (admin 回覆)
    @PutMapping("/msg/{id}/reply")
    public ResponseEntity<?> replyMsg(
            @PathVariable Long id,
            @RequestBody ReplyRequest request) {
        try {
            return ResponseEntity.ok(
                    contactMsgService.replyMsg(id, request.replyContent(), request.adminId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE /api/contact/msg/{id} (admin 刪除)
    @DeleteMapping("/msg/{id}")
    public ResponseEntity<?> deleteMsg(@PathVariable Long id) {
        try {
            contactMsgService.deleteMsg(id);
            return ResponseEntity.ok("留言已刪除");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    record ReplyRequest(String replyContent, Long adminId) {}
}

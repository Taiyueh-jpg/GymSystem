package com.team.service;

import com.team.dao.ContactMsgDao;
import com.team.model.ContactMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ContactMsgService {

    @Autowired
    private ContactMsgDao contactMsgDao;

    // ⛔ 模擬資料庫裡的關鍵字黑名單 (專題後期可以改成去 keyword_filter 資料表撈取)
    private final List<String> BAD_WORDS = Arrays.asList("笨蛋", "白痴", "爛", "退錢");

    /**
     * 提交客服表單
     */
    public ContactMsg submitMessage(ContactMsg msg) {
        
        // 1. 執行「關鍵字過濾」檢查
        for (String badWord : BAD_WORDS) {
            if (msg.getContent().contains(badWord) || msg.getSubject().contains(badWord)) {
                // 如果發現髒話，直接拋出錯誤，拒絕存入資料庫！
                throw new RuntimeException("您的留言包含不雅或敏感字眼：「" + badWord + "」，請修正後再送出。");
            }
        }

        // 2. 驗證通過，存入資料庫
        return contactMsgDao.save(msg);
    }

    /**
     * 後台管理員查詢未處理的留言
     */
    public List<ContactMsg> getNewMessages() {
        return contactMsgDao.findByMsgStatusOrderByCreatedAtAsc("new");
    }
}

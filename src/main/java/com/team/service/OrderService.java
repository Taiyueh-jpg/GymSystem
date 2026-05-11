package com.team.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.dao.OrderDetailDao;
import com.team.dao.PorderDao;
import com.team.model.OrderDetail;
import com.team.model.Porder;

@Service
public class OrderService {

    @Autowired
    private PorderDao porderDao;

    @Autowired
    private OrderDetailDao orderDetailDao;

    /**
     * 執行結帳邏輯
     * @param porder 訂單主檔物件 (包含會員ID、配送方式、付款方式、總金額)
     * @param details 訂單明細列表 (包含多筆商品ID、數量、單價)
     * @return 儲存成功的訂單主檔
     */
    @Transactional // 重要：確保交易完整性，若發生錯誤會自動 Rollback
    public Porder processCheckout(Porder porder, List<OrderDetail> details) {
        
        // 1. 補齊訂單主檔的系統欄位
        porder.setOrderDate(LocalDateTime.now()); // 設定當下下單時間
        
        // 如果前端沒傳狀態，預設為 "paid" (配合資料庫設計)
        if (porder.getStatus() == null) {
            porder.setStatus("paid");
        }

        // 2. 先儲存訂單主檔 (Porder)
        // 儲存後 JPA 會自動將資料庫生成的 order_id 填回 porder 物件中
        Porder savedPorder = porderDao.save(porder);

        // 3. 處理每一筆訂單明細 (OrderDetail)
        for (OrderDetail detail : details) {
            // 👇 加入這行監視器：印出 Java 實際收到的商品 ID
            System.out.println("👉 準備存入明細，收到的商品 ID 為：" + detail.getProductId());

            // 將明細與剛產生的訂單主檔 ID 進行關聯
            detail.setOrderId(savedPorder.getOrderId());
            
            // 儲存明細
            orderDetailDao.save(detail);
        }

        // 4. 返回儲存成功的結果
        return savedPorder;
    }

    /**
     * 查詢特定會員的所有訂單 (供會員中心使用)
     */
    public List<Porder> getMemberOrderHistory(Long memberId) {
        return porderDao.findByMemberIdOrderByOrderDateDesc(memberId);
    }

    public List<OrderDetail> getOrderDetails(Long orderId) {
        return orderDetailDao.findByOrderId(orderId);
    }

    /**
     * 修改訂單狀態 (後台管理員使用)
     */
    @Transactional
    public Porder updateOrderStatus(Long orderId, String newStatus) {
        // 1. 先使用內建的 findById 從資料庫把這筆訂單找出來
        // 如果找不到這筆訂單，就拋出一個錯誤
        Porder porder = porderDao.findById(orderId)
            .orElseThrow(() -> new RuntimeException("找不到訂單編號：" + orderId));

        // 2. 更新狀態
        porder.setStatus(newStatus);

        // 3. 再次呼叫 save()。
        // JPA 非常聰明，它發現這筆訂單已經有 ID 了，就會自動執行 UPDATE 語法，而不是 INSERT！
        return porderDao.save(porder);
    }

    /**
     * 查詢全公司所有訂單 (後台管理員專用)
     */
    public List<Porder> getAllOrders() {
        return porderDao.findAllByOrderByOrderDateDesc();
    }
    
    
    /**
     * 編輯訂單明細數量 (後端重構版)
     * 包含：刪除舊明細、寫入新明細、重新計算總金額
     */
    @Transactional
    public void updateOrderDetails(Long orderId, List<OrderDetail> updatedDetails) {
        // 1. 抓出主訂單
        Porder porder = porderDao.findById(orderId)
            .orElseThrow(() -> new RuntimeException("找不到訂單編號：" + orderId));

        // 2. 抓出舊明細並全數刪除
        List<OrderDetail> oldDetails = orderDetailDao.findByOrderId(orderId);
        orderDetailDao.deleteAll(oldDetails);

        // 初始化新總金額
        BigDecimal newTotal = BigDecimal.ZERO;

        // 3. 重新寫入前端傳來的新明細，並累加金額
        for (OrderDetail detail : updatedDetails) {
            detail.setOrderId(orderId); 
            detail.setDetailId(null); // 讓資料庫重新生成 PK
            orderDetailDao.save(detail);

            // 計算這項商品的小計：單價 x 數量
            BigDecimal subtotal = detail.getUnitPrice().multiply(new BigDecimal(detail.getQuantity()));
            // 累加到總金額
            newTotal = newTotal.add(subtotal);
        }

        // 4. 更新主檔的總金額並儲存！
        porder.setTotalAmount(newTotal);
        porderDao.save(porder);
    }

    /**
     * 🗑️ 刪除訂單 (包含主檔與明細) - 後台管理員專用
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        // 1. 必須先刪除該訂單底下的所有明細 (避免 Foreign Key 外鍵約束報錯)
        List<OrderDetail> details = orderDetailDao.findByOrderId(orderId);
        orderDetailDao.deleteAll(details);
        
        // 2. 確認明細清空後，再刪除訂單主檔
        porderDao.deleteById(orderId);
    }
}
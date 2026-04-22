package com.team.service;

import com.team.dao.OrderDetailDao;
import com.team.dao.PorderDao;
import com.team.model.OrderDetail;
import com.team.model.Porder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 📦 同學 E 負責區域：訂單結帳與物流業務邏輯
 */
@Service
public class OrderService {

    private final PorderDao porderDao;
    private final OrderDetailDao orderDetailDao;

    public OrderService(PorderDao porderDao, OrderDetailDao orderDetailDao) {
        this.porderDao = porderDao;
        this.orderDetailDao = orderDetailDao;
    }

    // 接收前端傳來的購物車明細 DTO (資料傳輸物件)
    public record CartItemDTO(Long productId, Integer quantity, BigDecimal unitPrice) {}

    /**
     * 核心結帳邏輯
     * ⚠️ @Transactional 保證交易一致性：主檔與明細必須「同生共死」
     */
    @Transactional
    public Porder createOrder(Long memberId, List<CartItemDTO> cartItems, String deliveryMethod, String paymentType) {
        
        // 1. 計算商品總額
        BigDecimal itemsTotal = BigDecimal.ZERO;
        for (CartItemDTO item : cartItems) {
            BigDecimal subTotal = item.unitPrice().multiply(new BigDecimal(item.quantity()));
            itemsTotal = itemsTotal.add(subTotal);
        }

        // 2. 處理物流與運費加總邏輯
        BigDecimal shippingFee = BigDecimal.ZERO;
        switch (deliveryMethod) {
            case "HOME_DELIVERY": // 宅配
                shippingFee = new BigDecimal("100");
                break;
            case "CVS": // 超商
                shippingFee = new BigDecimal("60");
                break;
            case "STORE_PICKUP": // 店取
                shippingFee = BigDecimal.ZERO;
                break;
            default:
                throw new RuntimeException("不支援的物流方式");
        }
        
        // 最終總金額 = 商品總額 + 運費
        BigDecimal finalTotalAmount = itemsTotal.add(shippingFee);

        // 3. 寫入訂單主檔 (Porder)
        Porder porder = new Porder();
        porder.setMemberId(memberId);
        porder.setOrderDate(LocalDateTime.now());
        porder.setStatus("PENDING"); // 預設狀態：處理中
        porder.setPaymentType(paymentType);
        porder.setDeliveryMethod(deliveryMethod);
        porder.setTotalAmount(finalTotalAmount);
        
        // 儲存主檔並取得系統自動產生的 OrderId
        Porder savedOrder = porderDao.save(porder);

        // 4. 寫入訂單明細表 (OrderDetail)
        for (CartItemDTO item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(savedOrder.getOrderId()); // 綁定剛剛的主檔 ID
            detail.setProductId(item.productId());
            detail.setQuantity(item.quantity());
            detail.setUnitPrice(item.unitPrice());
            orderDetailDao.save(detail);
        }

        return savedOrder;
    }
}
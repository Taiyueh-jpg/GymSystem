package com.team.dto;

import com.team.model.Porder;
import com.team.model.OrderDetail;
import java.util.List;

/**
 * 用來接收結帳時的 JSON 資料包
 */
public class CheckoutRequest {
    private Porder porder;
    private List<OrderDetail> details;

    // Getters and Setters
    public Porder getPorder() { return porder; }
    public void setPorder(Porder porder) { this.porder = porder; }
    public List<OrderDetail> getDetails() { return details; }
    public void setDetails(List<OrderDetail> details) { this.details = details; }
}
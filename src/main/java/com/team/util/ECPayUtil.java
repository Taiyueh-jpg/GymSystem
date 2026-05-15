package com.team.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * 🛠️ 綠界金流工具類別 (ECPay Util)
 */
@Component
public class ECPayUtil {

    private static final String MERCHANT_ID = "2000132";
    private static final String HASH_KEY = "5294y06JbISpM5x9";
    private static final String HASH_IV = "v77hoKGq4kWxNNkn";

    public Map<String, String> generateECPayParams(Long orderId, int totalAmount, String itemName) {
        
        Map<String, String> params = new HashMap<>();
        params.put("MerchantID", MERCHANT_ID);
        // 縮短訂單號長度，避免超過綠界 20 碼限制
        String tradeNo = "GYM" + (System.currentTimeMillis() / 1000) + "R" + orderId;
        params.put("MerchantTradeNo", tradeNo);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        params.put("MerchantTradeDate", sdf.format(new Date()));
        
        params.put("PaymentType", "aio");
        params.put("TotalAmount", String.valueOf(totalAmount));
        params.put("TradeDesc", "GymSystem_Order");
        params.put("ItemName", itemName);
        
        // 這些網址在測試階段使用 localhost 不會影響結帳畫面的開啟
        params.put("ReturnURL", "http://localhost:8080/api/orders/ecpay/callback");
        params.put("ClientBackURL", "http://localhost:8080/history.html");
        params.put("ChoosePayment", "ALL");
        params.put("EncryptType", "1");

        // 產生檢查碼 CheckMacValue
        String checkMacValue = generateCheckMacValue(params);
        params.put("CheckMacValue", checkMacValue);

        return params;
    }

    private String generateCheckMacValue(Map<String, String> params) {
        try {
            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);

            StringBuilder sb = new StringBuilder();
            sb.append("HashKey=").append(HASH_KEY).append("&");
            for (String key : keys) {
                sb.append(key).append("=").append(params.get(key)).append("&");
            }
            sb.append("HashIV=").append(HASH_IV);

            // 🔥 終極修復：100% 一字不漏移植綠界官方 Java SDK 的詭異加密邏輯
            String urlEncoded = URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8.toString());
            
            // 必須先轉小寫，再進行這些官方指定的字元取代
            urlEncoded = urlEncoded.toLowerCase()
                                   .replace("%2d", "-")
                                   .replace("%5f", "_")
                                   .replace("%2e", ".")
                                   .replace("%21", "!")
                                   .replace("%2a", "*")
                                   .replace("%28", "(")
                                   .replace("%29", ")")
                                   .replace("%20", "+"); // 🎯 就是這裡，官方強制要把 %20 換成 +

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(urlEncoded.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("產生綠界檢查碼失敗", e);
        }
    }
}
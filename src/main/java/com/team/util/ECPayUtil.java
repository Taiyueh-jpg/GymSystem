package com.team.util;

import java.net.URLEncoder;
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
 *
 * 修正說明：
 * 1. CheckMacValue：對每個 value 個別 URLEncode（不是整串），
 *    key 與 & = 結構字元保持不動，符合綠界官方規範。
 * 2. 移除後端回傳 inline <script> 的 HTML form，
 *    改為 generateECPayParams() 回傳 Map，讓前端 JS 動態建立 form 送出，
 *    避免 CSP 阻擋 inline script。
 */
@Component
public class ECPayUtil {

    private static final String MERCHANT_ID = "2000132";
    private static final String HASH_KEY = "5294y06JbISpM5x9";
    private static final String HASH_IV = "v77hoKGq4kWxNNkn";
    public static final String ECPAY_URL = "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5";

    /**
     * 產生送往綠界所需的所有參數（含 CheckMacValue 與 ecpayUrl）
     * 供 Controller 回傳 JSON 給前端，由前端動態建立 form 並送出
     */
    public Map<String, String> generateECPayParams(Long orderId, int totalAmount, String itemName) {

        Map<String, String> params = new HashMap<>();
        params.put("MerchantID", MERCHANT_ID);

        String tradeNo = "GYM" + orderId + "T" + System.currentTimeMillis();
        params.put("MerchantTradeNo", tradeNo);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        params.put("MerchantTradeDate", sdf.format(new Date()));

        params.put("PaymentType", "aio");
        params.put("TotalAmount", String.valueOf(totalAmount));

        // 防呆：使用純英數避免編碼問題
        params.put("TradeDesc", "GymSystem_Order");
        params.put("ItemName", "GymSystem_Order_No_" + orderId);

        params.put("ReturnURL", "http://localhost:8080/api/orders/ecpay/callback");
        params.put("ClientBackURL", "http://localhost:8080/history.html");
        params.put("ChoosePayment", "ALL");
        params.put("EncryptType", "1");

        // 計算 CheckMacValue（params 中不含 CheckMacValue）
        String checkMacValue = generateCheckMacValue(params);
        params.put("CheckMacValue", checkMacValue);

        // 附帶綠界 URL，讓前端知道要 POST 到哪裡
        params.put("ecpayUrl", ECPAY_URL);

        return params;
    }

    /**
     * 依綠界官方規範計算 CheckMacValue
     *
     * 正確流程：
     * 1. 參數依 key 字母升冪排序
     * 2. 對每個 value 個別 URLEncode（key 與 & = 保留不動）
     * 3. 組成字串：HashKey=xxx&key1=encodedVal1&...&HashIV=xxx
     * 4. 轉小寫
     * 5. SHA-256 Hash
     * 6. 轉大寫
     */
    private String generateCheckMacValue(Map<String, String> params) {
        try {
            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);

            StringBuilder sb = new StringBuilder();
            sb.append("HashKey=").append(HASH_KEY).append("&");

            for (String key : keys) {
                // ✅ 對每個 value 個別 URLEncode
                // 這樣 URL 的 :// 會正確 encode，& = 結構字元不受影響
                String encodedValue = URLEncoder.encode(params.get(key), "UTF-8")
                    .replace("+", "%20")   // URLEncoder 把空格變 +，綠界要求是 %20
                    .replace("*", "%2A")
                    .replace("%7E", "~")
                	.replace("%2F", "/");
                sb.append(key).append("=").append(encodedValue).append("&");
            }

            sb.append("HashIV=").append(HASH_IV);

            // 轉小寫
            String encoded = sb.toString().toLowerCase();

            System.out.println("=== ECPay encode string ===");
            System.out.println(encoded);

            // SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(encoded.getBytes("UTF-8"));

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

package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatPayService {

    @Value("${wechat.miniapp.app-id:}")
    private String appId;

    @Value("${wechat.pay.mch-id:}")
    private String mchId;

    @Value("${wechat.pay.api-key:}")
    private String apiKey;

    @Value("${wechat.pay.notify-url:}")
    private String notifyUrl;

    private static final SecureRandom RANDOM = new SecureRandom();

    public boolean isConfigured() {
        return appId != null && !appId.isEmpty()
                && mchId != null && !mchId.isEmpty()
                && apiKey != null && !apiKey.isEmpty();
    }

    public Map<String, Object> createJsapiOrder(String orderNo, String description,
                                                  int amountInFen, String openid) {
        if (!isConfigured()) {
            throw new BusinessException("微信支付未配置，请联系管理员");
        }

        Map<String, String> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("nonce_str", generateNonceStr());
        params.put("body", description);
        params.put("out_trade_no", orderNo);
        params.put("total_fee", String.valueOf(amountInFen));
        params.put("spbill_create_ip", "127.0.0.1");
        params.put("notify_url", notifyUrl);
        params.put("trade_type", "JSAPI");
        params.put("openid", openid);

        String sign = generateSign(params);
        params.put("sign", sign);

        log.info("[微信支付] 创建JSAPI订单: orderNo={}, amount={}分, openid={}", orderNo, amountInFen, openid);

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("amount", amountInFen);
        result.put("signType", "MD5");
        result.put("paySign", sign);
        result.put("nonceStr", params.get("nonce_str"));
        result.put("timeStamp", String.valueOf(Instant.now().getEpochSecond()));
        return result;
    }

    public boolean verifyCallback(Map<String, String> callbackParams) {
        if (!callbackParams.containsKey("sign")) {
            return false;
        }
        String receivedSign = callbackParams.remove("sign");
        String calculatedSign = generateSign(callbackParams);
        return calculatedSign.equalsIgnoreCase(receivedSign);
    }

    private String generateSign(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            if (value != null && !value.isEmpty()) {
                sb.append(key).append("=").append(value).append("&");
            }
        }
        sb.append("key=").append(apiKey);

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            throw new BusinessException("签名生成失败: " + e.getMessage());
        }
    }

    private String generateNonceStr() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

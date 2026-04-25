package com.pettrail.pettrailbackend.service;

import com.alibaba.fastjson.JSONObject;
import com.pettrail.pettrailbackend.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WxSubscribeMessageService {

    @Value("${wechat.miniapp.app-id}")
    private String appId;

    @Value("${wechat.miniapp.app-secret}")
    private String appSecret;

    private final RedisTemplate<String, Object> redisTemplate;
    private final SysConfigService sysConfigService;

    private static final String ACCESS_TOKEN_KEY = "wechat:access_token:subscribe";
    private static final long ACCESS_TOKEN_TTL_MINUTES = 110;

    public WxSubscribeMessageService(RedisTemplate<String, Object> redisTemplate,
                                     SysConfigService sysConfigService) {
        this.redisTemplate = redisTemplate;
        this.sysConfigService = sysConfigService;
    }

    public String getAccessToken() {
        try {
            Object cached = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
            if (cached != null) {
                return cached.toString();
            }
        } catch (Exception e) {
            log.warn("读取access_token缓存异常: {}", e.getMessage());
        }

        String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appId, appSecret);
        try {
            String response = HttpUtil.doGet(url);
            JSONObject json = JSONObject.parseObject(response);
            String accessToken = json.getString("access_token");
            if (accessToken != null && !accessToken.isEmpty()) {
                redisTemplate.opsForValue().set(ACCESS_TOKEN_KEY, accessToken, ACCESS_TOKEN_TTL_MINUTES, TimeUnit.MINUTES);
                return accessToken;
            }
            log.error("获取access_token失败: errcode={}, errmsg={}", json.getInteger("errcode"), json.getString("errmsg"));
        } catch (Exception e) {
            log.error("获取access_token异常: {}", e.getMessage());
        }
        return null;
    }

    public boolean sendSubscribeMessage(String openid, String templateId, Map<String, Object> data) {
        return sendSubscribeMessage(openid, templateId, data, null);
    }

    public boolean sendSubscribeMessage(String openid, String templateId, Map<String, Object> data, String page) {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            log.error("发送订阅消息失败: access_token为空");
            return false;
        }
        if (openid == null || openid.isEmpty()) {
            log.warn("发送订阅消息失败: openid为空");
            return false;
        }

        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("touser", openid);
        body.put("template_id", templateId);
        body.put("data", data);
        if (page != null && !page.isEmpty()) {
            body.put("page", page);
        }

        try {
            String response = HttpUtil.doPost(url, JSONObject.toJSONString(body), "application/json");
            JSONObject json = JSONObject.parseObject(response);
            int errcode = json.getIntValue("errcode");
            if (errcode == 0) {
                log.info("发送订阅消息成功: openid={}, templateId={}", openid, templateId);
                return true;
            }
            if (errcode == 43101) {
                log.warn("用户未订阅该模板消息: openid={}, templateId={}", openid, templateId);
            } else {
                log.error("发送订阅消息失败: errcode={}, errmsg={}, openid={}, templateId={}",
                        errcode, json.getString("errmsg"), openid, templateId);
            }
            return false;
        } catch (Exception e) {
            log.error("发送订阅消息异常: openid={}, error={}", openid, e.getMessage());
            return false;
        }
    }

    public boolean sendCheckinReminder(String openid, String itemName, String remindTime, String page) {
        String templateId = getConfigValue("wx.subscribe.template.checkin");
        if (templateId == null || templateId.isEmpty()) {
            log.warn("打卡提醒模板ID未配置，跳过微信订阅消息");
            return false;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing1", Map.of("value", truncate(itemName, 20)));
        data.put("time2", Map.of("value", remindTime));

        return sendSubscribeMessage(openid, templateId, data, page);
    }

    public boolean sendVaccineReminder(String openid, String vaccineName, String nextDate, String page) {
        String templateId = getConfigValue("wx.subscribe.template.vaccine");
        if (templateId == null || templateId.isEmpty()) {
            log.warn("疫苗提醒模板ID未配置，跳过微信订阅消息");
            return false;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing1", Map.of("value", truncate(vaccineName, 20)));
        data.put("time2", Map.of("value", nextDate));
        data.put("thing3", Map.of("value", "请及时为宠物接种疫苗"));

        return sendSubscribeMessage(openid, templateId, data, page);
    }

    public boolean sendParasiteReminder(String openid, String parasiteType, String nextDate, String page) {
        String templateId = getConfigValue("wx.subscribe.template.parasite");
        if (templateId == null || templateId.isEmpty()) {
            log.warn("驱虫提醒模板ID未配置，跳过微信订阅消息");
            return false;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing1", Map.of("value", truncate(parasiteType, 20)));
        data.put("time2", Map.of("value", nextDate));
        data.put("thing3", Map.of("value", "请及时为宠物进行驱虫"));

        return sendSubscribeMessage(openid, templateId, data, page);
    }

    private String getConfigValue(String key) {
        try {
            return sysConfigService.getValue(key);
        } catch (Exception e) {
            return null;
        }
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) + "…" : str;
    }
}

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
    private final WxSubscribeAuthorizationService authorizationService;

    private static final String ACCESS_TOKEN_KEY = "wechat:access_token:subscribe";
    private static final long ACCESS_TOKEN_TTL_MINUTES = 110;

    public WxSubscribeMessageService(RedisTemplate<String, Object> redisTemplate,
                                     SysConfigService sysConfigService,
                                     WxSubscribeAuthorizationService authorizationService) {
        this.redisTemplate = redisTemplate;
        this.sysConfigService = sysConfigService;
        this.authorizationService = authorizationService;
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

    public boolean sendSubscribeMessage(Long userId, String templateType, String openid,
                                        String templateId, Map<String, Object> data, String page) {
        log.info("====== sendSubscribeMessage 开始: userId={}, templateType={}, templateId={}, openid={} ======",
                userId, templateType, templateId, openid);

        boolean hasCredit = authorizationService.consumeCredit(userId, templateType);
        log.info("授权积分检查结果: userId={}, templateType={}, hasCredit={}", userId, templateType, hasCredit);
        if (!hasCredit) {
            log.warn("订阅消息授权积分不足，跳过发送: userId={}, templateType={}", userId, templateType);
            return false;
        }

        String accessToken = getAccessToken();
        log.info("access_token获取结果: {}", accessToken != null ? "成功" : "失败");
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

        log.info("请求微信订阅消息API: url={}, body={}", url.substring(0, url.indexOf("access_token=") + "access_token=".length() + 10) + "...", body);

        try {
            String response = HttpUtil.doPost(url, JSONObject.toJSONString(body), "application/json");
            log.info("微信订阅消息API响应: {}", response);
            JSONObject json = JSONObject.parseObject(response);
            int errcode = json.getIntValue("errcode");
            if (errcode == 0) {
                log.info("发送订阅消息成功: userId={}, openid={}, templateType={}", userId, openid, templateType);
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
            log.error("发送订阅消息异常: openid={}, error={}", openid, e.getMessage(), e);
            return false;
        }
    }

    public boolean sendCheckinReminder(Long userId, String openid, String itemName, String userName,
                                       String remindTime, String theme, String checkinTimeRange, String page) {
        String templateId = getConfigValue("wx.subscribe.template.checkin");
        if (templateId == null || templateId.isEmpty()) {
            log.warn("打卡提醒模板ID未配置，跳过微信订阅消息");
            return false;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing1", Map.of("value", truncate(blankToDefault(itemName, "打卡"), 20)));
        data.put("name3", Map.of("value", truncate(blankToDefault(userName, "宠友"), 10)));
        data.put("time2", Map.of("value", blankToDefault(remindTime, "")));
        data.put("thing4", Map.of("value", truncate(blankToDefault(theme, "记得按时打卡哦"), 20)));
        data.put("time5", Map.of("value", blankToDefault(checkinTimeRange, "")));

        log.info("发送打卡订阅消息: templateId={}, data={}", templateId, data);
        return sendSubscribeMessage(userId, "checkin", openid, templateId, data, page);
    }

    public boolean sendVaccineReminder(Long userId, String openid, String petName,
                                       String vaccineName, String nextDate, String note, String page) {
        String templateId = getConfigValue("wx.subscribe.template.vaccine");
        if (templateId == null || templateId.isEmpty()) {
            log.warn("疫苗提醒模板ID未配置，跳过微信订阅消息");
            return false;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing1", Map.of("value", truncate(blankToDefault(petName, "宠物"), 20)));
        data.put("time2", Map.of("value", blankToDefault(nextDate, "")));
        data.put("thing3", Map.of("value", truncate(blankToDefault(vaccineName, "疫苗"), 20)));
        data.put("thing4", Map.of("value", truncate(blankToDefault(note, "定期疫苗保障宠物健康哟"), 20)));

        log.info("发送疫苗订阅消息: templateId={}, data={}", templateId, data);
        return sendSubscribeMessage(userId, "vaccine", openid, templateId, data, page);
    }

    public boolean sendParasiteReminder(Long userId, String openid, String petName,
                                        String parasiteType, String nextDate, String note, String page) {
        String templateId = getConfigValue("wx.subscribe.template.parasite");
        if (templateId == null || templateId.isEmpty()) {
            log.warn("驱虫提醒模板ID未配置，跳过微信订阅消息");
            return false;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing1", Map.of("value", truncate(blankToDefault(petName, "宠物"), 20)));
        data.put("time2", Map.of("value", blankToDefault(nextDate, "")));
        data.put("thing3", Map.of("value", truncate(blankToDefault(parasiteType, "内外同驱"), 20)));
        data.put("thing4", Map.of("value", truncate(blankToDefault(note, "定期驱虫保障宠物健康哟~"), 20)));

        log.info("发送驱虫订阅消息: templateId={}, data={}", templateId, data);
        return sendSubscribeMessage(userId, "parasite", openid, templateId, data, page);
    }

    public boolean sendFeedingReminder(Long userId, String openid, String mealName, String feedTime, String note, String page) {
        String templateId = getConfigValue("wx.subscribe.template.feeding");
        if (templateId == null || templateId.isEmpty()) {
            log.warn("喂食提醒模板ID未配置，跳过微信订阅消息");
            return false;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing1", Map.of("value", truncate(blankToDefault(mealName, "喂食"), 20)));
        data.put("time2", Map.of("value", blankToDefault(feedTime, "")));
        data.put("thing3", Map.of("value", truncate(blankToDefault(note, "该给宠物喂食啦"), 20)));

        return sendSubscribeMessage(userId, "feeding", openid, templateId, data, page);
    }

    private String getConfigValue(String key) {
        try {
            return sysConfigService.getValue(key);
        } catch (Exception e) {
            return null;
        }
    }

    private String truncate(String str, int maxLen) {
        if (str == null || str.isEmpty()) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) + "…" : str;
    }

    private String blankToDefault(String str, String defaultValue) {
        if (str == null || str.trim().isEmpty()) return defaultValue;
        return str;
    }
}

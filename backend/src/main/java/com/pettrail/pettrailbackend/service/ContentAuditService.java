package com.pettrail.pettrailbackend.service;

import com.alibaba.fastjson.JSONObject;
import com.pettrail.pettrailbackend.util.HttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentAuditService {

    @Value("${wechat.miniapp.app-id}")
    private String appId;

    @Value("${wechat.miniapp.app-secret}")
    private String appSecret;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ACCESS_TOKEN_CACHE_KEY = "wechat:access_token";
    private static final long ACCESS_TOKEN_CACHE_TTL_MINUTES = 110;
    private static final int MAX_RETRY = 2;

    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "法轮", "法轮功", "六四", "天安门", "台独", "藏独", "疆独",
            "反华", "颠覆", "暴恐", "恐怖袭击", "自制炸药", "制造炸弹",
            "卖淫", "招嫖", "约炮", "色情直播", "裸聊",
            "代开发票", "假币", "洗钱", "赌博网站", "网络赌博",
            "毒品", "冰毒", "海洛因", "大麻", "可卡因",
            "枪支", "买卖枪支", "仿真枪", "管制刀具"
    );

    public boolean auditText(String content, String openid) {
        if (content == null || content.trim().isEmpty()) {
            return true;
        }

        if (!localSensitiveWordCheck(content)) {
            log.warn("本地敏感词检测命中，内容被拦截：content={}", content);
            return false;
        }

        return auditTextWithRetry(content, openid, 0, false);
    }

    private boolean auditTextWithRetry(String content, String openid, int retryCount, boolean tokenRefreshed) {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("获取微信 access_token 失败，使用本地敏感词审核结果放行");
                return true;
            }

            String url = "https://api.weixin.qq.com/wxa/msg_sec_check?access_token=" + accessToken;

            JSONObject body = new JSONObject();
            body.put("content", content);
            body.put("version", 2);
            body.put("scene", 1);
            if (openid != null && !openid.isEmpty()) {
                body.put("openid", openid);
            }

            String response = HttpUtil.doPost(url, body.toJSONString());
            JSONObject result = JSONObject.parseObject(response);

            int errcode = result.getIntValue("errcode");
            if (errcode == 0) {
                JSONObject detail = result.getJSONObject("detail");
                if (detail != null) {
                    String suggest = detail.getString("suggest");
                    if ("risky".equals(suggest)) {
                        String label = detail.getString("label");
                        log.warn("文本内容违规：label={}, content={}", label, content);
                        return false;
                    }
                }

                String resultSuggest = result.getString("result") != null
                        ? JSONObject.parseObject(result.getString("result")).getString("suggest")
                        : null;
                if ("risky".equals(resultSuggest)) {
                    log.warn("文本内容违规（result）：content={}", content);
                    return false;
                }

                log.debug("文本审核通过：content={}", content);
                return true;
            }

            if ((errcode == 40001 || errcode == 42001) && !tokenRefreshed) {
                log.warn("access_token 无效或过期（errcode={}），清除缓存后重试", errcode);
                redisTemplate.delete(ACCESS_TOKEN_CACHE_KEY);
                return auditTextWithRetry(content, openid, retryCount, true);
            }

            if (isRetryableError(errcode) && retryCount < MAX_RETRY) {
                log.warn("微信文本审核接口返回可重试错误：errcode={}，第{}次重试", errcode, retryCount + 1);
                sleepBeforeRetry(retryCount);
                return auditTextWithRetry(content, openid, retryCount + 1, tokenRefreshed);
            }

            log.error("微信文本审核接口返回不可恢复错误：errcode={}, errmsg={}", errcode, result.getString("errmsg"));
            return true;
        } catch (IOException e) {
            if (retryCount < MAX_RETRY) {
                log.warn("文本审核网络异常：{}，第{}次重试", e.getMessage(), retryCount + 1);
                sleepBeforeRetry(retryCount);
                return auditTextWithRetry(content, openid, retryCount + 1, tokenRefreshed);
            }
            log.error("文本审核网络异常，已达最大重试次数，放行：{}", e.getMessage());
            return true;
        } catch (Exception e) {
            log.error("文本审核异常，放行：{}", e.getMessage());
            return true;
        }
    }

    public boolean auditText(String content) {
        return auditText(content, null);
    }

    public boolean auditImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return true;
        }

        return auditImageWithRetry(imageUrl, 0, false);
    }

    private boolean auditImageWithRetry(String imageUrl, int retryCount, boolean tokenRefreshed) {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("获取微信 access_token 失败，跳过图片审核");
                return true;
            }

            String url = "https://api.weixin.qq.com/wxa/img_sec_check?access_token=" + accessToken;

            JSONObject body = new JSONObject();
            body.put("media_url", imageUrl);

            String response = HttpUtil.doPost(url, body.toJSONString());
            JSONObject result = JSONObject.parseObject(response);

            int errcode = result.getIntValue("errcode");
            if (errcode == 0) {
                log.debug("图片审核通过：imageUrl={}", imageUrl);
                return true;
            }

            if (errcode == 87014) {
                log.warn("图片内容违规：imageUrl={}", imageUrl);
                return false;
            }

            if ((errcode == 40001 || errcode == 42001) && !tokenRefreshed) {
                log.warn("access_token 无效或过期（errcode={}），清除缓存后重试", errcode);
                redisTemplate.delete(ACCESS_TOKEN_CACHE_KEY);
                return auditImageWithRetry(imageUrl, retryCount, true);
            }

            if (isRetryableError(errcode) && retryCount < MAX_RETRY) {
                log.warn("微信图片审核接口返回可重试错误：errcode={}，第{}次重试", errcode, retryCount + 1);
                sleepBeforeRetry(retryCount);
                return auditImageWithRetry(imageUrl, retryCount + 1, tokenRefreshed);
            }

            log.error("微信图片审核接口返回不可恢复错误：errcode={}, errmsg={}", errcode, result.getString("errmsg"));
            return true;
        } catch (IOException e) {
            if (retryCount < MAX_RETRY) {
                log.warn("图片审核网络异常：{}，第{}次重试", e.getMessage(), retryCount + 1);
                sleepBeforeRetry(retryCount);
                return auditImageWithRetry(imageUrl, retryCount + 1, tokenRefreshed);
            }
            log.error("图片审核网络异常，已达最大重试次数，放行：{}", e.getMessage());
            return true;
        } catch (Exception e) {
            log.error("图片审核异常，放行：{}", e.getMessage());
            return true;
        }
    }

    private boolean localSensitiveWordCheck(String content) {
        String lower = content.toLowerCase();
        for (String word : SENSITIVE_WORDS) {
            if (lower.contains(word)) {
                return false;
            }
        }
        return true;
    }

    private boolean isRetryableError(int errcode) {
        return errcode == -1 || errcode == 42900 || errcode == 45009;
    }

    private void sleepBeforeRetry(int retryCount) {
        try {
            long delay = (long) Math.pow(2, retryCount) * 500;
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String getAccessToken() {
        Object cached = redisTemplate.opsForValue().get(ACCESS_TOKEN_CACHE_KEY);
        if (cached != null) {
            return cached.toString();
        }

        try {
            String url = String.format(
                    "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                    appId, appSecret
            );

            String response = HttpUtil.doGet(url);
            JSONObject result = JSONObject.parseObject(response);

            String accessToken = result.getString("access_token");
            if (accessToken == null) {
                log.error("获取微信 access_token 失败：errcode={}, errmsg={}",
                        result.getInteger("errcode"), result.getString("errmsg"));
                return null;
            }

            redisTemplate.opsForValue().set(ACCESS_TOKEN_CACHE_KEY, accessToken,
                    ACCESS_TOKEN_CACHE_TTL_MINUTES, TimeUnit.MINUTES);

            log.info("成功获取微信 access_token");
            return accessToken;
        } catch (Exception e) {
            log.error("获取微信 access_token 异常：{}", e.getMessage());
            return null;
        }
    }
}

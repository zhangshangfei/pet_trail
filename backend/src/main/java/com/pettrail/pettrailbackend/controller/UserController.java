package com.pettrail.pettrailbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.service.UserService;
import com.pettrail.pettrailbackend.service.FollowService;
import com.pettrail.pettrailbackend.service.PostService;
import com.pettrail.pettrailbackend.util.HttpUtil;
import com.pettrail.pettrailbackend.util.JwtUtil;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FollowService followService;
    private final PostService postService;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Value("${wechat.miniapp.app-id}")
    private String appId;

    @Value("${wechat.miniapp.app-secret}")
    private String appSecret;

    /**
     * 微信登录（使用 code 换取 token）
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody JSONObject data) {
        log.info("========== 微信登录开始 ==========");
        log.info("请求参数：data={}", data);
        log.info("当前配置的 AppID: {}", appId);
        
        String code = data.getString("code");

        if (code == null || code.isEmpty()) {
            log.error("缺少 code 参数");
            return Result.error(400, "缺少 code 参数");
        }

        // 防止 code 被重复使用（微信的 code 只能使用一次）
        String codeKey = "wechat:login:code:" + code;
        Boolean isFirstUse = redisTemplate.opsForValue().setIfAbsent(codeKey, "used", 5, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(isFirstUse)) {
            log.warn("⚠️ Code 已被使用，拒绝重复登录！code: {}", code);
            return Result.error(400, "登录码已使用，请重新获取");
        }

        log.info("获取到的 code: {}", code);

        try {
            // 1. 构建请求微信的 URL（隐藏 secret 用于日志安全）
            String maskedSecret = appSecret.substring(0, Math.min(8, appSecret.length())) + "****";
            log.info("调用微信接口，AppID: {}, Secret: {}", appId, maskedSecret);
            
            String wxUrl = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    appId, appSecret, code
            );

            // 2. 调用微信接口
            log.info("请求微信接口 URL: {}", wxUrl);
            String wxResponse = HttpUtil.doGet(wxUrl);
            log.info("微信接口响应: {}", wxResponse);
            
            JSONObject json = JSONObject.parseObject(wxResponse);
            String openid = json.getString("openid");
            String errmsg = json.getString("errmsg");
            Integer errcode = json.getInteger("errcode");

            // 3. 检查是否成功获取到 openid
            if (openid == null || openid.isEmpty()) {
                log.error("微信登录失败 - errcode: {}, errmsg: {}", errcode, errmsg);
                log.error("可能原因：1. AppID/AppSecret 不匹配  2. Code 已过期或已被使用  3. Code 无效");
                
                // 删除 code 标记，允许重新获取
                redisTemplate.delete(codeKey);
                
                return Result.error(401, "微信登录失败：" + errmsg);
            }

            log.info("成功获取 openid: {}", openid);

            // 4. 业务登录
            User user = userService.login(openid);
            String token = jwtUtil.generateToken(user.getId(), user.getOpenid());

            log.info("登录成功，用户 ID: {}", user.getId());
            log.info("========== 微信登录完成 ==========");

            return Result.success(new LoginResponse(user, token));
        } catch (Exception e) {
            log.error("登录异常：{}", e.getMessage(), e);
            // 发生异常时删除 code 标记
            redisTemplate.delete(codeKey);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 用户注册（备用接口）
     */
    @PostMapping("/register")
    public Result<?> register(
            @RequestParam String openid,
            @RequestParam(required = false) String unionid,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String avatar) {
        log.info("用户注册：openid={}, nickname={}", openid, nickname);
        try {
            User user = userService.register(openid, unionid, nickname, avatar);
            String token = jwtUtil.generateToken(user.getId(), user.getOpenid());
            return Result.success(new LoginResponse(user, token));
        } catch (Exception e) {
            log.error("注册失败：{}", e.getMessage(), e);
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户资料（需要认证）
     */
    @GetMapping("/profile")
    public Result<User> getProfile() {
        try {
            Long userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            User user = userService.getProfile(userId);
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户资料失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/discover")
    public Result<List<Map<String, Object>>> discoverUsers(
            @RequestParam(defaultValue = "recommend") String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        try {
            Long currentUserId = UserContext.getCurrentUserId();

            List<User> users = userService.discoverUsers(currentUserId, type, keyword, page, size);

            List<Long> followedIds = new ArrayList<>();
            if (currentUserId != null) {
                followedIds = followService.getFolloweeIds(currentUserId);
            }

            List<Long> finalFollowedIds = followedIds;
            List<Map<String, Object>> result = new ArrayList<>();
            for (User user : users) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", user.getId());
                item.put("nickname", user.getNickname());
                item.put("avatar", user.getAvatar());
                item.put("gender", user.getGender());
                item.put("createdAt", user.getCreatedAt());
                item.put("isFollowing", finalFollowedIds.contains(user.getId()));
                item.put("followerCount", followService.getFollowerCount(user.getId()));
                item.put("followeeCount", followService.getFolloweeCount(user.getId()));
                item.put("postCount", postService.getUserPostCount(user.getId()));
                result.add(item);
            }

            return Result.success(result);
        } catch (Exception e) {
            log.error("发现用户失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getProfile(id);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("nickname", user.getNickname());
            result.put("avatar", user.getAvatar());
            result.put("gender", user.getGender());
            result.put("createdAt", user.getCreatedAt());

            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId != null && !currentUserId.equals(id)) {
                result.put("isFollowing", followService.isFollowing(currentUserId, id));
            } else {
                result.put("isFollowing", false);
            }

            result.put("followerCount", followService.getFollowerCount(id));
            result.put("followeeCount", followService.getFolloweeCount(id));
            result.put("postCount", postService.getUserPostCount(id));

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户信息失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户资料（需要认证）
     */
    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody java.util.Map<String, Object> requestBody) {
        String nickname = (String) requestBody.get("nickname");
        String avatar = (String) requestBody.get("avatar");
        String phone = (String) requestBody.get("phone");
        Integer gender = requestBody.get("gender") != null ? Integer.parseInt(requestBody.get("gender").toString()) : null;
        
        log.info("更新用户资料");
        try {
            Long userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            User user = userService.updateProfile(userId, nickname, avatar, phone, gender);
            return Result.success(user);
        } catch (Exception e) {
            log.error("更新用户资料失败：{}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 登录响应 DTO
     */
    public static class LoginResponse {
        private final User user;
        private final String token;
        private final Long expireTime;

        public LoginResponse(User user, String token) {
            this.user = user;
            this.token = token;
            this.expireTime = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000; // 7 天
        }

        public User getUser() {
            return user;
        }

        public String getToken() {
            return token;
        }

        public Long getExpireTime() {
            return expireTime;
        }
    }
}

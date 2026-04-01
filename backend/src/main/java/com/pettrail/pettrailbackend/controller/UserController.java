package com.pettrail.pettrailbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.service.UserService;
import com.pettrail.pettrailbackend.util.HttpUtil;
import com.pettrail.pettrailbackend.util.JwtUtil;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${wechat.miniapp.app-id}")
    private String appId;

    @Value("${wechat.miniapp.app-secret}")
    private String appSecret;

    /**
     * 微信登录（使用 code 换取 token）
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody JSONObject data) {
        log.info("用户登录：data={}", data);
        String code = data.getString("code");
        
        if (code == null || code.isEmpty()) {
            return Result.error(400, "缺少 code 参数");
        }

        try {
            // 1. 构建请求微信的 URL
            String wxUrl = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    appId, appSecret, code
            );

            // 2. 调用微信接口
            String wxResponse = HttpUtil.doGet(wxUrl);
            JSONObject json = JSONObject.parseObject(wxResponse);
            String openid = json.getString("openid");

            // 3. 检查是否成功获取到 openid
            if (openid == null) {
                String errMsg = json.getString("errmsg");
                log.warn("微信登录失败：{}", errMsg);
                return Result.error(401, "微信登录失败：" + errMsg);
            }

            // 4. 业务登录
            User user = userService.login(openid);
            String token = jwtUtil.generateToken(user.getId(), user.getOpenid());

            return Result.success(new LoginResponse(user, token));
        } catch (Exception e) {
            log.error("登录失败：{}", e.getMessage(), e);
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

    /**
     * 更新用户资料（需要认证）
     */
    @PutMapping("/profile")
    public Result<User> updateProfile(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String avatar,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer gender) {
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

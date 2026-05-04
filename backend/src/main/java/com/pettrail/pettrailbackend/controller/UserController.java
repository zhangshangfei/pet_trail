package com.pettrail.pettrailbackend.controller;

import com.alibaba.fastjson2.JSONObject;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.dto.UserUpdateDTO;
import com.pettrail.pettrailbackend.dto.WxLoginDTO;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.security.JwtAuthenticationFilter;
import com.pettrail.pettrailbackend.service.UserService;
import com.pettrail.pettrailbackend.service.FollowService;
import com.pettrail.pettrailbackend.service.PostService;
import com.pettrail.pettrailbackend.service.RecommendService;
import com.pettrail.pettrailbackend.util.HttpUtil;
import com.pettrail.pettrailbackend.util.JwtUtil;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;
    private final FollowService followService;
    private final PostService postService;
    private final RecommendService recommendService;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Value("${wechat.miniapp.app-id}")
    private String appId;

    @Value("${wechat.miniapp.app-secret}")
    private String appSecret;

    @PostMapping("/login")
    public Result<?> login(@RequestBody WxLoginDTO dto) {
        String code = dto.getCode();
        if (code == null || code.isEmpty()) {
            return Result.error(400, "缺少 code 参数");
        }

        String codeKey = "wechat:login:code:" + code;
        Boolean isFirstUse = redisTemplate.opsForValue().setIfAbsent(codeKey, "used", 5, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(isFirstUse)) {
            return Result.error(400, "登录码已使用，请重新获取");
        }

        String wxUrl = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code);

        String wxResponse;
        try {
            wxResponse = HttpUtil.doGet(wxUrl);
        } catch (Exception e) {
            redisTemplate.delete(codeKey);
            return Result.error("微信接口调用失败：" + e.getMessage());
        }
        JSONObject json = JSONObject.parseObject(wxResponse);
        String openid = json.getString("openid");
        String errmsg = json.getString("errmsg");
        if (openid == null || openid.isEmpty()) {
            redisTemplate.delete(codeKey);
            return Result.error(401, "微信登录失败：" + errmsg);
        }

        User user = userService.login(openid);
        String token = jwtUtil.generateToken(user.getId(), user.getOpenid());
        return Result.success(new LoginResponse(user, token));
    }

    @PostMapping("/register")
    public Result<?> register(
            @RequestParam String openid,
            @RequestParam(required = false) String unionid,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String avatar) {
        User user = userService.register(openid, unionid, nickname, avatar);
        String token = jwtUtil.generateToken(user.getId(), user.getOpenid());
        return Result.success(new LoginResponse(user, token));
    }

    @GetMapping("/profile")
    public Result<User> getProfile() {
        Long userId = requireLogin();
        return Result.success(userService.getProfile(userId));
    }

    @GetMapping("/discover")
    public Result<List<Map<String, Object>>> discoverUsers(
            @RequestParam(defaultValue = "recommend") String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Long currentUserId = UserContext.getCurrentUserId();

        if ("recommend".equals(type) && (keyword == null || keyword.trim().isEmpty())) {
            return Result.success(recommendService.recommendUsers(currentUserId, page, size));
        }

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
            if ("hot".equals(type)) {
                item.put("recommendReason", "hot");
            } else if ("new".equals(type)) {
                item.put("recommendReason", "new_user");
            }
            result.add(item);
        }
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getUserById(@PathVariable Long id) {
        User user = userService.getProfile(id);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
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
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody UserUpdateDTO dto) {
        Long userId = requireLogin();
        return Result.success(userService.updateProfile(userId, dto.getNickname(), dto.getAvatar(), dto.getPhone(), dto.getGender()));
    }

    @DeleteMapping("/account")
    public Result<?> deactivateAccount(HttpServletRequest request) {
        Long userId = requireLogin();
        blacklistCurrentToken(request);
        userService.deactivateUser(userId);
        return Result.success("账号已注销");
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        blacklistCurrentToken(request);
        return Result.success("已退出登录");
    }

    private void blacklistCurrentToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            long remainingSeconds = jwtUtil.getRemainingTimeInSeconds(token);
            if (remainingSeconds > 0) {
                String tokenHash = JwtAuthenticationFilter.hashToken(token);
                Long currentUserId = UserContext.getCurrentUserId();
                redisTemplate.opsForValue().set(
                        "jwt:blacklist:" + tokenHash,
                        currentUserId != null ? String.valueOf(currentUserId) : "",
                        remainingSeconds,
                        TimeUnit.SECONDS
                );
                log.info("Token已加入黑名单, 剩余有效期: {}秒", remainingSeconds);
            }
        }
    }

    public static class LoginResponse {
        private final User user;
        private final String token;
        private final Long expireTime;

        public LoginResponse(User user, String token) {
            this.user = user;
            this.token = token;
            this.expireTime = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000;
        }

        public User getUser() { return user; }
        public String getToken() { return token; }
        public Long getExpireTime() { return expireTime; }
    }
}

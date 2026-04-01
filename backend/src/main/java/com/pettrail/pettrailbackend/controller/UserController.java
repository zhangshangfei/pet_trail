package com.pettrail.pettrailbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.service.UserService;
import com.pettrail.pettrailbackend.util.HttpUtil;
import com.pettrail.pettrailbackend.util.JwtUtil;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;



    // 你的小程序配置，建议放在配置文件 application.yml 中
    private static final String APP_ID = "wxb210d75568abe1f7";
    private static final String APP_SECRET = "3279099fe5b8e0ad126fc4b8a4797ede";

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> data) {
        log.info("用户登录: data={}", data);
        Map<String, Object> result = new HashMap<>();
        String code = data.get("code");
        try {
            // 1. 构建请求微信的 URL
            String wxUrl = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    APP_ID, APP_SECRET, code
            );

            // 2. 调用工具类，发送 GET 请求给微信服务器
            String wxResponse = HttpUtil.doGet(wxUrl);

            // 3. 解析微信返回的 JSON
            JSONObject json = JSONObject.parseObject(wxResponse);
            String openid = json.getString("openid");

            // 4. 检查是否成功获取到 openid
            if (openid == null) {
                result.put("success", false);
                result.put("message", "微信登录失败: " + json.getString("errmsg"));
                return result;
            }

            // 5. 后续业务逻辑...
            User user = userService.login(openid);
            String token = jwtUtil.generateToken(user.getId(), user.getOpenid());

            result.put("success", true);
            result.put("message", "登录成功");
            result.put("token", token);
            result.put("data", user);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "登录失败: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


    // /**
    //  * 微信登录
    //  */
    // @PostMapping("/login")
    // public Map<String, Object> login(@RequestParam String openid) {
    //     log.info("用户登录: openid={}", openid);
    //     Map<String, Object> result = new HashMap<>();
    //     try {
    //         User user = userService.login(openid);
    //         String token = jwtUtil.generateToken(user.getId(), user.getOpenid());
    //         result.put("success", true);
    //         result.put("data", user);
    //         result.put("token", token);
    //         result.put("message", "登录成功");
    //     } catch (Exception e) {
    //         log.error("登录失败: {}", e.getMessage(), e);
    //         result.put("success", false);
    //         result.put("message", "登录失败: " + e.getMessage());
    //     }
    //     return result;
    // }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Map<String, Object> register(
            @RequestParam String openid,
            @RequestParam(required = false) String unionid,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String avatar) {
        log.info("用户注册: openid={}, nickname={}", openid, nickname);
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.register(openid, unionid, nickname, avatar);
            String token = jwtUtil.generateToken(user.getId(), user.getOpenid());
            result.put("success", true);
            result.put("data", user);
            result.put("token", token);
            result.put("message", "注册成功");
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "注册失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取用户资料（需要认证）
     */
    @GetMapping("/profile")
    public Map<String, Object> getProfile() {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = UserContext.getCurrentUserId();
            User user = userService.getProfile(userId);
            result.put("success", true);
            result.put("data", user);
            result.put("message", "获取成功");
        } catch (Exception e) {
            log.error("获取用户资料失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新用户资料（需要认证）
     */
    @PutMapping("/profile")
    public Map<String, Object> updateProfile(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String avatar,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer gender) {
        log.info("更新用户资料");
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = UserContext.getCurrentUserId();
            User user = userService.updateProfile(userId, nickname, avatar, phone, gender);
            result.put("success", true);
            result.put("data", user);
            result.put("message", "更新成功");
        } catch (Exception e) {
            log.error("更新用户资料失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }
}

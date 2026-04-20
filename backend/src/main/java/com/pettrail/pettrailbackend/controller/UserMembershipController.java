package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.UserMembership;
import com.pettrail.pettrailbackend.service.UserMembershipService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
public class UserMembershipController {

    private final UserMembershipService userMembershipService;

    @GetMapping("/info")
    public Result<Map<String, Object>> getMembershipInfo() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        Map<String, Object> info = userMembershipService.getMembershipInfo(userId);
        return Result.success(info);
    }

    @PostMapping("/subscribe")
    public Result<UserMembership> subscribe(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        String plan = body.get("plan") != null ? body.get("plan").toString() : "monthly";
        if (!"monthly".equals(plan) && !"yearly".equals(plan)) {
            return Result.error(400, "无效的订阅计划");
        }

        try {
            UserMembership membership = userMembershipService.subscribe(userId, plan);
            return Result.success(membership);
        } catch (Exception e) {
            log.error("订阅失败: {}", e.getMessage(), e);
            return Result.error("订阅失败：" + e.getMessage());
        }
    }

    @PostMapping("/cancel")
    public Result<Void> cancelSubscription() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        try {
            userMembershipService.cancelSubscription(userId);
            return Result.success();
        } catch (Exception e) {
            log.error("取消订阅失败: {}", e.getMessage(), e);
            return Result.error("取消失败：" + e.getMessage());
        }
    }

    @GetMapping("/check")
    public Result<Map<String, Object>> checkPro() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        boolean isPro = userMembershipService.isPro(userId);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("isPro", isPro);
        return Result.success(result);
    }
}

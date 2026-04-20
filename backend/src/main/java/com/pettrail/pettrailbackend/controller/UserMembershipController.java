package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.UserMembership;
import com.pettrail.pettrailbackend.service.UserMembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
public class UserMembershipController extends BaseController {

    private final UserMembershipService userMembershipService;

    @GetMapping("/info")
    public Result<Map<String, Object>> getMembershipInfo() {
        Long userId = requireLogin();
        return Result.success(userMembershipService.getMembershipInfo(userId));
    }

    @PostMapping("/subscribe")
    public Result<UserMembership> subscribe(@RequestBody Map<String, Object> body) {
        Long userId = requireLogin();
        String plan = body.get("plan") != null ? body.get("plan").toString() : "monthly";
        if (!"monthly".equals(plan) && !"yearly".equals(plan)) {
            return Result.error(400, "无效的订阅计划");
        }
        return Result.success(userMembershipService.subscribe(userId, plan));
    }

    @PostMapping("/cancel")
    public Result<Void> cancelSubscription() {
        Long userId = requireLogin();
        userMembershipService.cancelSubscription(userId);
        return Result.success();
    }

    @GetMapping("/check")
    public Result<Map<String, Object>> checkPro() {
        Long userId = requireLogin();
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("isPro", userMembershipService.isPro(userId));
        return Result.success(result);
    }
}

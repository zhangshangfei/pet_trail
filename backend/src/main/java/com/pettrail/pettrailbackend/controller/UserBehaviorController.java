package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.UserBehavior;
import com.pettrail.pettrailbackend.service.UserBehaviorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/behaviors")
@RequiredArgsConstructor
@Tag(name = "用户行为", description = "用户行为追踪接口")
public class UserBehaviorController extends BaseController {

    private final UserBehaviorService userBehaviorService;

    @PostMapping
    @Operation(summary = "记录用户行为")
    public Result<Void> recordBehavior(@RequestBody Map<String, Object> body) {
        Long userId = requireLogin();
        String action = (String) body.get("action");
        String targetType = (String) body.get("targetType");
        Long targetId = body.get("targetId") != null ? Long.valueOf(body.get("targetId").toString()) : null;
        Integer duration = body.get("duration") != null ? Integer.valueOf(body.get("duration").toString()) : null;
        String extra = body.get("extra") != null ? body.get("extra").toString() : null;
        userBehaviorService.recordBehavior(userId, action, targetType, targetId, duration, extra);
        return Result.success(null);
    }

    @PostMapping("/view")
    @Operation(summary = "记录浏览行为")
    public Result<Void> recordView(@RequestBody Map<String, Object> body) {
        Long userId = requireLogin();
        String targetType = (String) body.get("targetType");
        Long targetId = Long.valueOf(body.get("targetId").toString());
        Integer duration = body.get("duration") != null ? Integer.valueOf(body.get("duration").toString()) : 0;
        userBehaviorService.recordView(userId, targetType, targetId, duration);
        return Result.success(null);
    }
}

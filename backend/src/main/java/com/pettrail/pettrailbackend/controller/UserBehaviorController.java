package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.dto.UserBehaviorDTO;
import com.pettrail.pettrailbackend.dto.UserBehaviorViewDTO;
import com.pettrail.pettrailbackend.service.UserBehaviorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/behaviors")
@RequiredArgsConstructor
@Tag(name = "用户行为", description = "用户行为追踪接口")
public class UserBehaviorController extends BaseController {

    private final UserBehaviorService userBehaviorService;

    @PostMapping
    @Operation(summary = "记录用户行为")
    public Result<Void> recordBehavior(@RequestBody UserBehaviorDTO dto) {
        Long userId = requireLogin();
        userBehaviorService.recordBehavior(userId, dto.getAction(), dto.getTargetType(), dto.getTargetId(), dto.getDuration(), dto.getExtra());
        return Result.success(null);
    }

    @PostMapping("/view")
    @Operation(summary = "记录浏览行为")
    public Result<Void> recordView(@RequestBody UserBehaviorViewDTO dto) {
        Long userId = requireLogin();
        userBehaviorService.recordView(userId, dto.getTargetType(), dto.getTargetId(), dto.getDuration() != null ? dto.getDuration() : 0);
        return Result.success(null);
    }
}

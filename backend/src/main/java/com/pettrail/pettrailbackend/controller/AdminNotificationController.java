package com.pettrail.pettrailbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Notification;
import com.pettrail.pettrailbackend.mapper.NotificationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "Admin-通知管理", description = "后台通知管理")
public class AdminNotificationController {

    private final NotificationMapper notificationMapper;

    @GetMapping
    @Operation(summary = "分页查询通知列表")
    public Result<Page<Notification>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type) {
        Page<Notification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(Notification::getUserId, userId);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        wrapper.orderByDesc(Notification::getCreatedAt);

        return Result.success(notificationMapper.selectPage(pageParam, wrapper));
    }

    @PostMapping
    @Operation(summary = "发送系统通知")
    public Result<Void> sendNotification(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String content = body.get("content").toString();

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(0L);
        notification.setType("system");
        notification.setContent(content);
        notification.setIsRead(false);
        notificationMapper.insert(notification);

        return Result.success(null);
    }

    @PostMapping("/broadcast")
    @Operation(summary = "广播通知（发送给所有用户）")
    public Result<Void> broadcast(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        return Result.success(null);
    }
}

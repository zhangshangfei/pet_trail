package com.pettrail.pettrailbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Notification;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.NotificationMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "Admin-通知管理", description = "后台通知管理")
public class AdminNotificationController {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    @GetMapping
    @Operation(summary = "分页查询通知列表")
    public Result<Page<Notification>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead) {
        Page<Notification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(Notification::getUserId, userId);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }
        wrapper.orderByDesc(Notification::getCreatedAt);

        return Result.success(notificationMapper.selectPage(pageParam, wrapper));
    }

    @PostMapping
    @Operation(summary = "发送系统通知")
    public Result<Void> sendNotification(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String content = body.get("content").toString();
        String title = body.get("title") != null ? body.get("title").toString() : null;

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
    @RequireRole("SUPER_ADMIN")
    public Result<Map<String, Object>> broadcast(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        String title = body.getOrDefault("title", "系统通知");

        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getStatus, 1)
        );

        int batchSize = 500;
        List<Notification> batch = new ArrayList<>(batchSize);
        int count = 0;

        for (User user : users) {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setFromUserId(0L);
            notification.setType("system");
            notification.setContent(content);
            notification.setIsRead(false);
            batch.add(notification);
            count++;

            if (batch.size() >= batchSize) {
                for (Notification n : batch) {
                    notificationMapper.insert(n);
                }
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            for (Notification n : batch) {
                notificationMapper.insert(n);
            }
        }

        return Result.success(Map.of("sentCount", count));
    }
}

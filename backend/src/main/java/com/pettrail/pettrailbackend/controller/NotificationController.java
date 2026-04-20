package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.NotificationVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.service.NotificationService;
import com.pettrail.pettrailbackend.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "消息通知", description = "通知相关接口")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "获取通知列表", description = "分页获取当前用户的通知列表，支持类型筛选")
    public Result<List<NotificationVO>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        List<NotificationVO> notifications = notificationService.getNotifications(userId, page, size, type);
        return Result.success(notifications);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数", description = "获取当前用户的未读通知数量")
    public Result<Map<String, Object>> getUnreadCount() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("count", 0);
            return Result.success(data);
        }

        int count = notificationService.getUnreadCount(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知已读", description = "标记指定通知为已读状态")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        notificationService.markAsRead(id, userId);
        return Result.success(null);
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读", description = "将当前用户所有通知标记为已读")
    public Result<Void> markAllAsRead() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        notificationService.markAllAsRead(userId);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "删除指定的通知")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        notificationService.deleteNotification(id, userId);
        return Result.success(null);
    }

    @DeleteMapping("/clear")
    @Operation(summary = "清空所有通知", description = "清空当前用户的所有通知")
    public Result<Void> clearAllNotifications() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        notificationService.clearAllNotifications(userId);
        return Result.success(null);
    }
}

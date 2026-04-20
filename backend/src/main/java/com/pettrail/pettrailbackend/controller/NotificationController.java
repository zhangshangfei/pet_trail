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
public class NotificationController extends BaseController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "获取通知列表")
    public Result<List<NotificationVO>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type) {
        Long userId = requireLogin();
        return Result.success(notificationService.getNotifications(userId, page, size, type));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数")
    public Result<Map<String, Object>> getUnreadCount() {
        Long userId = UserContext.getCurrentUserId();
        Map<String, Object> data = new HashMap<>();
        if (userId == null) {
            data.put("count", 0);
            return Result.success(data);
        }
        data.put("count", notificationService.getUnreadCount(userId));
        return Result.success(data);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知已读")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = requireLogin();
        notificationService.markAsRead(id, userId);
        return Result.success(null);
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读")
    public Result<Void> markAllAsRead() {
        Long userId = requireLogin();
        notificationService.markAllAsRead(userId);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        Long userId = requireLogin();
        notificationService.deleteNotification(id, userId);
        return Result.success(null);
    }

    @DeleteMapping("/clear")
    @Operation(summary = "清空所有通知")
    public Result<Void> clearAllNotifications() {
        Long userId = requireLogin();
        notificationService.clearAllNotifications(userId);
        return Result.success(null);
    }

    @GetMapping("/unread-system")
    @Operation(summary = "获取未读系统消息")
    public Result<List<NotificationVO>> getUnreadSystemNotifications() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.success(List.of());
        }
        List<NotificationVO> notifications = notificationService.getNotifications(userId, 1, 10, "system");
        List<NotificationVO> unreadSystem = notifications.stream()
            .filter(n -> !n.getIsRead())
            .collect(java.util.stream.Collectors.toList());
        return Result.success(unreadSystem);
    }

    @GetMapping("/poll")
    @Operation(summary = "轮询新通知")
    public Result<Map<String, Object>> pollNotifications() {
        Long userId = UserContext.getCurrentUserId();
        Map<String, Object> data = new HashMap<>();
        if (userId == null) {
            data.put("unreadCount", 0);
            data.put("hasNewSystem", false);
            data.put("systemMessages", List.of());
            return Result.success(data);
        }

        int unreadCount = notificationService.getUnreadCount(userId);
        List<NotificationVO> systemMsgs = notificationService.getNotifications(userId, 1, 5, "system");
        List<NotificationVO> unreadSystem = systemMsgs.stream()
            .filter(n -> !n.getIsRead())
            .collect(java.util.stream.Collectors.toList());

        data.put("unreadCount", unreadCount);
        data.put("hasNewSystem", !unreadSystem.isEmpty());
        data.put("systemMessages", unreadSystem);
        return Result.success(data);
    }
}

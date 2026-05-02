package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.NotificationAdminVO;
import com.pettrail.pettrailbackend.dto.NotificationBroadcastDTO;
import com.pettrail.pettrailbackend.dto.NotificationSendDTO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "Admin-通知管理", description = "后台通知管理")
public class AdminNotificationController extends BaseAdminController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "分页查询通知列表")
    public Result<Page<NotificationAdminVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead) {
        return Result.success(notificationService.adminListNotifications(page, size, userId, type, isRead));
    }

    @PostMapping
    @Operation(summary = "发送系统通知")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "notification", action = "send", detail = "发送系统通知")
    public Result<Void> sendNotification(@RequestBody NotificationSendDTO dto) {
        notificationService.adminSendNotification(dto.getUserId(), dto.getTitle(), dto.getContent());
        return Result.success(null);
    }

    @PostMapping("/broadcast")
    @Operation(summary = "广播通知（发送给所有用户）")
    @RequireRole("SUPER_ADMIN")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "notification", action = "broadcast", detail = "广播通知")
    public Result<Map<String, Object>> broadcast(@RequestBody NotificationBroadcastDTO dto) {
        int count = notificationService.adminBroadcast(dto.getTitle(), dto.getContent());
        return Result.success(Map.of("sentCount", count));
    }
}

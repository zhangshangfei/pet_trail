package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.annotation.OperationLog;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.dto.StatusDTO;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.service.AdminDashboardService;
import com.pettrail.pettrailbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin-用户管理", description = "后台用户管理")
public class AdminUserController extends BaseAdminController {

    private final UserService userService;
    private final AdminDashboardService dashboardService;

    @GetMapping
    @Operation(summary = "分页查询用户列表")
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(userService.adminListUsers(page, size, keyword, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<User> getDetail(@PathVariable Long id) {
        return Result.success(userService.adminGetUserDetail(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态（启用/禁用）")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "user", action = "update_status", detail = "更新用户状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusDTO dto) {
        userService.adminUpdateUserStatus(id, dto.getStatus());
        return Result.success(null);
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "获取用户统计信息")
    public Result<Map<String, Object>> getUserStats(@PathVariable Long id) {
        return Result.success(dashboardService.getUserStats(id));
    }
}

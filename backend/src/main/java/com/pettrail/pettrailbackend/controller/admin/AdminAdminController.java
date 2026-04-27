package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Admin;
import com.pettrail.pettrailbackend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/admins")
@RequiredArgsConstructor
@Tag(name = "Admin-管理员管理", description = "后台管理员账号管理")
public class AdminAdminController extends BaseAdminController {

    private final AdminService adminService;

    @GetMapping
    @Operation(summary = "分页查询管理员列表")
    public Result<Page<Admin>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminService.adminListAdmins(page, size));
    }

    @PostMapping
    @Operation(summary = "创建管理员")
    public Result<Admin> create(@RequestBody Admin admin) {
        return Result.success(adminService.adminCreateAdmin(admin));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新管理员信息")
    public Result<Admin> update(@PathVariable Long id, @RequestBody Admin admin) {
        return Result.success(adminService.adminUpdateAdmin(id, admin));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新管理员状态")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.adminUpdateAdminStatus(id, body.get("status"));
        return Result.success("状态更新成功");
    }

    @PutMapping("/{id}/reset-password")
    @Operation(summary = "重置管理员密码")
    public Result<String> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.adminResetPassword(id, body.get("password"));
        return Result.success("密码重置成功");
    }

    @PutMapping("/{id}/change-password")
    @Operation(summary = "修改管理员密码")
    public Result<String> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.adminChangePassword(id, body.get("oldPassword"), body.get("newPassword"));
        return Result.success("密码修改成功");
    }
}

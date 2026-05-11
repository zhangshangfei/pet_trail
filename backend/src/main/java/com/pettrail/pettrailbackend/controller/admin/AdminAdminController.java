package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.AdminVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Admin;
import com.pettrail.pettrailbackend.service.AdminService;
import com.pettrail.pettrailbackend.service.TotpService;
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
    private final TotpService totpService;

    @GetMapping
    @Operation(summary = "分页查询管理员列表")
    public Result<Page<AdminVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminService.adminListAdmins(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取管理员详情")
    public Result<AdminVO> getDetail(@PathVariable Long id) {
        return Result.success(adminService.adminGetAdminDetail(id));
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

    @PostMapping("/{id}/totp/bind")
    @Operation(summary = "绑定2FA")
    public Result<Map<String, String>> bindTotp(@PathVariable Long id) {
        Admin admin = adminService.getById(id);
        if (admin == null) {
            return Result.error(404, "管理员不存在");
        }
        Map<String, String> totpData = totpService.generateSecret(admin.getUsername());
        adminService.updateTotpSecret(id, totpData.get("secret"));
        return Result.success(totpData);
    }

    @PutMapping("/{id}/totp/unbind")
    @Operation(summary = "解绑2FA")
    public Result<String> unbindTotp(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer code = body.get("code");
        Admin admin = adminService.getById(id);
        if (admin == null) {
            return Result.error(404, "管理员不存在");
        }
        if (admin.getTotpSecret() != null && !admin.getTotpSecret().isEmpty()) {
            if (code == null || !totpService.verifyCode(admin.getTotpSecret(), code)) {
                return Result.error(400, "验证码错误");
            }
        }
        adminService.updateTotpSecret(id, null);
        return Result.success("2FA已解绑");
    }

    @PostMapping("/{id}/totp/verify")
    @Operation(summary = "验证2FA验证码")
    public Result<Boolean> verifyTotp(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Admin admin = adminService.getById(id);
        if (admin == null) {
            return Result.error(404, "管理员不存在");
        }
        if (admin.getTotpSecret() == null || admin.getTotpSecret().isEmpty()) {
            return Result.success(true);
        }
        Integer code = body.get("code");
        if (code == null) {
            return Result.error(400, "请输入验证码");
        }
        boolean valid = totpService.verifyCode(admin.getTotpSecret(), code);
        return Result.success(valid);
    }
}

package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.AdminCreateDTO;
import com.pettrail.pettrailbackend.dto.AdminUpdateDTO;
import com.pettrail.pettrailbackend.dto.ChangePasswordDTO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.dto.StatusDTO;
import com.pettrail.pettrailbackend.entity.Admin;
import com.pettrail.pettrailbackend.mapper.AdminMapper;
import com.pettrail.pettrailbackend.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/admins")
@RequiredArgsConstructor
@Tag(name = "Admin-管理员管理", description = "后台管理员管理")
public class AdminAdminController extends BaseAdminController {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @Operation(summary = "管理员列表")
    @RequireRole("SUPER_ADMIN")
    public Result<Page<Admin>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Page<Admin> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Admin::getUsername, keyword).or().like(Admin::getNickname, keyword));
        }
        wrapper.orderByDesc(Admin::getCreatedAt);

        Page<Admin> result = adminMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(a -> a.setPassword(null));
        return Result.success(result);
    }

    @PostMapping
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "admin", action = "create", detail = "创建管理员")
    @Operation(summary = "新增管理员")
    @RequireRole("SUPER_ADMIN")
    public Result<Void> create(@RequestBody AdminCreateDTO dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();
        String nickname = dto.getNickname();
        String role = dto.getRole() != null ? dto.getRole() : "ADMIN";

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return Result.error(400, "用户名和密码不能为空");
        }

        Long count = adminMapper.selectCount(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        if (count > 0) {
            return Result.error(400, "用户名已存在");
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setNickname(nickname != null ? nickname : username);
        admin.setRole(role);
        admin.setStatus(1);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        adminMapper.insert(admin);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "admin", action = "update", detail = "更新管理员")
    @Operation(summary = "编辑管理员")
    @RequireRole("SUPER_ADMIN")
    public Result<Void> update(@PathVariable Long id, @RequestBody AdminUpdateDTO dto) {
        Long currentId = UserContext.getCurrentUserId();
        if (id.equals(currentId)) {
            return Result.error(400, "不能编辑自己");
        }

        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            return Result.error(404, "管理员不存在");
        }

        if (dto.getNickname() != null) {
            admin.setNickname(dto.getNickname());
        }
        if (dto.getRole() != null && !id.equals(currentId)) {
            admin.setRole(dto.getRole());
        }
        admin.setUpdatedAt(LocalDateTime.now());
        adminMapper.updateById(admin);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "admin", action = "update_status", detail = "更新管理员状态")
    @Operation(summary = "启用/禁用管理员")
    @RequireRole("SUPER_ADMIN")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusDTO dto) {
        Long currentId = UserContext.getCurrentUserId();
        if (id.equals(currentId)) {
            return Result.error(400, "不能禁用自己");
        }

        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            return Result.error(404, "管理员不存在");
        }

        admin.setStatus(dto.getStatus());
        admin.setUpdatedAt(LocalDateTime.now());
        adminMapper.updateById(admin);
        return Result.success(null);
    }

    @PutMapping("/{id}/reset-pwd")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "admin", action = "reset_pwd", detail = "重置密码")
    @Operation(summary = "重置管理员密码")
    @RequireRole("SUPER_ADMIN")
    public Result<Void> resetPassword(@PathVariable Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            return Result.error(404, "管理员不存在");
        }

        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setUpdatedAt(LocalDateTime.now());
        adminMapper.updateById(admin);
        return Result.success(null);
    }

    @PutMapping("/password")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "admin", action = "change_pwd", detail = "修改密码")
    @Operation(summary = "修改自己的密码")
    public Result<Void> changePassword(@RequestBody ChangePasswordDTO dto) {
        Long currentId = requireLogin();

        String oldPassword = dto.getOldPassword();
        String newPassword = dto.getNewPassword();

        if (oldPassword == null || newPassword == null) {
            return Result.error(400, "请填写完整");
        }

        Admin admin = adminMapper.selectById(currentId);
        if (admin == null) {
            return Result.error(404, "管理员不存在");
        }

        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            return Result.error(400, "旧密码错误");
        }

        admin.setPassword(passwordEncoder.encode(newPassword));
        admin.setUpdatedAt(LocalDateTime.now());
        adminMapper.updateById(admin);
        return Result.success(null);
    }
}

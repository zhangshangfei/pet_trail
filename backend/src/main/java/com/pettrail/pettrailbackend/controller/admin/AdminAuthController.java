package com.pettrail.pettrailbackend.controller.admin;

import com.pettrail.pettrailbackend.dto.AdminLoginDTO;
import com.pettrail.pettrailbackend.dto.AdminVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "管理员认证", description = "后台管理员登录认证")
public class AdminAuthController extends BaseAdminController {

    private final AdminService adminService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<Map<String, Object>> login(@RequestBody AdminLoginDTO dto) {
        return Result.success(adminService.login(dto.getUsername(), dto.getPassword()));
    }

    @GetMapping("/profile")
    @Operation(summary = "获取管理员信息")
    public Result<AdminVO> getProfile() {
        Long adminId = requireLogin();
        return Result.success(adminService.getProfile(adminId));
    }

    @GetMapping("/permissions")
    @Operation(summary = "获取所有权限码")
    public Result<List<String>> getAllPermissions() {
        return Result.success(AdminService.ALL_PERMISSIONS);
    }
}

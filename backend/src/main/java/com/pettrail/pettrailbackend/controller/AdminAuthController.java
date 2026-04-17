package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.AdminLoginDTO;
import com.pettrail.pettrailbackend.dto.AdminVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.service.AdminService;
import com.pettrail.pettrailbackend.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "管理员认证", description = "后台管理员登录认证")
public class AdminAuthController {

    private final AdminService adminService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<Map<String, Object>> login(@RequestBody AdminLoginDTO dto) {
        try {
            Map<String, Object> result = adminService.login(dto.getUsername(), dto.getPassword());
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    @GetMapping("/profile")
    @Operation(summary = "获取管理员信息")
    public Result<AdminVO> getProfile() {
        Long adminId = UserContext.getCurrentUserId();
        if (adminId == null) {
            return Result.error(401, "未登录");
        }
        try {
            return Result.success(adminService.getProfile(adminId));
        } catch (RuntimeException e) {
            return Result.error(404, e.getMessage());
        }
    }
}

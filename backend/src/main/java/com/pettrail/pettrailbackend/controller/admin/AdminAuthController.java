package com.pettrail.pettrailbackend.controller.admin;

import com.pettrail.pettrailbackend.dto.AdminLoginDTO;
import com.pettrail.pettrailbackend.dto.AdminVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.service.AdminService;
import com.pettrail.pettrailbackend.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "管理员认证", description = "后台管理员登录认证")
public class AdminAuthController extends BaseAdminController {

    private final AdminService adminService;
    private final SysMenuService sysMenuService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<Map<String, Object>> login(@RequestBody AdminLoginDTO dto) {
        Map<String, Object> result = adminService.login(dto.getUsername(), dto.getPassword());
        AdminVO admin = (AdminVO) result.get("admin");
        result.put("menus", sysMenuService.getUserMenuTree(admin.getPermissions()));
        return Result.success(result);
    }

    @GetMapping("/profile")
    @Operation(summary = "获取管理员信息")
    public Result<Map<String, Object>> getProfile() {
        Long adminId = requireLogin();
        AdminVO admin = adminService.getProfile(adminId);
        Map<String, Object> data = new HashMap<>();
        data.put("admin", admin);
        data.put("menus", sysMenuService.getUserMenuTree(admin.getPermissions()));
        return Result.success(data);
    }

    @GetMapping("/permissions")
    @Operation(summary = "获取所有权限码")
    public Result<List<String>> getAllPermissions() {
        return Result.success(AdminService.ALL_PERMISSIONS);
    }
}

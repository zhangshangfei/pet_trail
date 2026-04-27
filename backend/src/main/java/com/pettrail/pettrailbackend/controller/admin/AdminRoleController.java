package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.entity.SysRole;
import com.pettrail.pettrailbackend.entity.SysRoleMenu;
import com.pettrail.pettrailbackend.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Admin-角色管理", description = "角色管理接口")
public class AdminRoleController extends BaseAdminController {

    private final SysRoleService sysRoleService;

    @GetMapping
    @Operation(summary = "角色列表")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SysRole> result = sysRoleService.listPage(page, size);
        return Map.of("success", true, "data", Map.of(
                "records", result.getRecords(),
                "total", result.getTotal()
        ));
    }

    @GetMapping("/all")
    @Operation(summary = "所有角色(下拉选择用)")
    public Map<String, Object> listAll() {
        return Map.of("success", true, "data", sysRoleService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "角色详情")
    public Map<String, Object> getById(@PathVariable Long id) {
        SysRole role = sysRoleService.getById(id);
        List<SysRoleMenu> menus = sysRoleService.getRoleMenus(id);
        return Map.of("success", true, "data", Map.of("role", role, "menus", menus));
    }

    @PostMapping
    @Operation(summary = "创建角色")
    public Map<String, Object> create(@RequestBody SysRole role) {
        return Map.of("success", true, "data", sysRoleService.create(role));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody SysRole role) {
        return Map.of("success", true, "data", sysRoleService.update(id, role));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public Map<String, Object> delete(@PathVariable Long id) {
        sysRoleService.delete(id);
        return Map.of("success", true);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新角色状态")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        sysRoleService.updateStatus(id, body.get("status"));
        return Map.of("success", true);
    }

    @GetMapping("/{id}/menus")
    @Operation(summary = "获取角色菜单权限")
    public Map<String, Object> getRoleMenus(@PathVariable Long id) {
        return Map.of("success", true, "data", sysRoleService.getRoleMenus(id));
    }

    @PutMapping("/{id}/menus")
    @Operation(summary = "保存角色菜单权限")
    public Map<String, Object> saveRoleMenus(@PathVariable Long id, @RequestBody List<SysRoleMenu> menus) {
        sysRoleService.saveRoleMenus(id, menus);
        return Map.of("success", true);
    }
}

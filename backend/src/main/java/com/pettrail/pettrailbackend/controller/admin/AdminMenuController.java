package com.pettrail.pettrailbackend.controller.admin;

import com.pettrail.pettrailbackend.entity.SysMenu;
import com.pettrail.pettrailbackend.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
@Tag(name = "Admin-菜单管理", description = "系统菜单管理接口")
public class AdminMenuController extends BaseAdminController {

    private final SysMenuService sysMenuService;

    @GetMapping("/tree")
    @Operation(summary = "获取完整菜单树")
    public Map<String, Object> getMenuTree() {
        return Map.of("success", true, "data", sysMenuService.getMenuTree());
    }

    @GetMapping("/user")
    @Operation(summary = "获取当前用户菜单树")
    public Map<String, Object> getUserMenuTree() {
        return Map.of("success", true, "data", sysMenuService.getMenuTree());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取菜单详情")
    public Map<String, Object> getDetail(@PathVariable Long id) {
        return Map.of("success", true, "data", sysMenuService.getMenuDetail(id));
    }

    @PostMapping
    @Operation(summary = "创建菜单")
    public Map<String, Object> create(@RequestBody SysMenu menu) {
        return Map.of("success", true, "data", sysMenuService.createMenu(menu));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新菜单")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody SysMenu menu) {
        return Map.of("success", true, "data", sysMenuService.updateMenu(id, menu));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单")
    public Map<String, Object> delete(@PathVariable Long id) {
        sysMenuService.deleteMenu(id);
        return Map.of("success", true);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新菜单状态")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        sysMenuService.updateMenuStatus(id, body.get("status"));
        return Map.of("success", true);
    }

    @PutMapping("/batch-sort")
    @Operation(summary = "批量更新菜单排序")
    public Map<String, Object> batchSort(@RequestBody List<Map<String, Object>> sortList) {
        sysMenuService.batchSort(sortList);
        return Map.of("success", true);
    }
}

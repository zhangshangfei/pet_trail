package com.pettrail.pettrailbackend.controller.admin;

import com.pettrail.pettrailbackend.annotation.OperationLog;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.SysConfig;
import com.pettrail.pettrailbackend.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@Tag(name = "系统配置管理", description = "系统配置CRUD接口")
public class AdminSysConfigController {

    private final SysConfigService sysConfigService;

    @GetMapping
    @Operation(summary = "获取配置列表")
    @RequireRole("ADMIN")
    public Result<List<SysConfig>> list(@RequestParam(required = false) String category) {
        return Result.success(sysConfigService.listByCategory(category));
    }

    @GetMapping("/categories")
    @Operation(summary = "获取配置分类列表")
    @RequireRole("ADMIN")
    public Result<List<String>> listCategories() {
        return Result.success(sysConfigService.listCategories());
    }

    @PostMapping
    @Operation(summary = "新增配置项")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "config", action = "create", detail = "新增系统配置")
    public Result<SysConfig> create(@RequestBody SysConfig config) {
        if (config.getConfigKey() == null || config.getConfigKey().isEmpty()) {
            return Result.error(400, "配置键名不能为空");
        }
        return Result.success(sysConfigService.create(config));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新配置项")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "config", action = "update", detail = "更新系统配置")
    public Result<SysConfig> update(@PathVariable Long id, @RequestBody SysConfig config) {
        SysConfig updated = sysConfigService.update(id, config);
        if (updated == null) {
            return Result.error(404, "配置项不存在");
        }
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置项")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "config", action = "delete", detail = "删除系统配置")
    public Result<Void> delete(@PathVariable Long id) {
        if (!sysConfigService.delete(id)) {
            return Result.error(404, "配置项不存在");
        }
        return Result.success(null);
    }

    @PostMapping("/refresh-cache")
    @Operation(summary = "刷新配置缓存")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "config", action = "refresh_cache", detail = "刷新配置缓存")
    public Result<Void> refreshCache() {
        sysConfigService.invalidateCache();
        return Result.success(null);
    }

    @GetMapping("/value/{key}")
    @Operation(summary = "获取单个配置值")
    @RequireRole("ADMIN")
    public Result<Map<String, String>> getValue(@PathVariable String key) {
        String value = sysConfigService.getValue(key);
        return Result.success(Map.of("key", key, "value", value != null ? value : ""));
    }
}

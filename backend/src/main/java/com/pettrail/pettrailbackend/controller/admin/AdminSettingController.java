package com.pettrail.pettrailbackend.controller.admin;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.dto.SettingBatchUpdateDTO;
import com.pettrail.pettrailbackend.dto.SettingUpdateDTO;
import com.pettrail.pettrailbackend.entity.SystemSetting;
import com.pettrail.pettrailbackend.service.AdminSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@Tag(name = "Admin-系统设置", description = "后台系统设置")
public class AdminSettingController extends BaseAdminController {

    private final AdminSettingService settingService;

    @GetMapping
    @Operation(summary = "获取所有设置")
    public Result<List<SystemSetting>> list() {
        return Result.success(settingService.listAll());
    }

    @PutMapping
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "setting", action = "batch_update", detail = "批量更新设置")
    @Operation(summary = "批量更新设置")
    public Result<Void> batchUpdate(@RequestBody SettingBatchUpdateDTO dto) {
        settingService.batchUpdate(dto.getSettings());
        return Result.success(null);
    }

    @PutMapping("/{key}")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "setting", action = "update", detail = "更新设置")
    @Operation(summary = "更新单个设置")
    public Result<Void> update(@PathVariable String key, @RequestBody SettingUpdateDTO dto) {
        settingService.update(key, dto.getValue());
        return Result.success(null);
    }
}

package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.dto.SettingBatchUpdateDTO;
import com.pettrail.pettrailbackend.dto.SettingUpdateDTO;
import com.pettrail.pettrailbackend.entity.SystemSetting;
import com.pettrail.pettrailbackend.mapper.SystemSettingMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@Tag(name = "Admin-系统设置", description = "后台系统设置")
public class AdminSettingController extends BaseAdminController {

    private final SystemSettingMapper settingMapper;

    @GetMapping
    @Operation(summary = "获取所有设置")
    public Result<List<SystemSetting>> list() {
        return Result.success(settingMapper.selectList(null));
    }

    @PutMapping
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "setting", action = "batch_update", detail = "批量更新设置")
    @Operation(summary = "批量更新设置")
    public Result<Void> batchUpdate(@RequestBody SettingBatchUpdateDTO dto) {
        List<Map<String, String>> settings = dto.getSettings();
        if (settings != null) {
            for (Map<String, String> item : settings) {
                String key = item.get("settingKey");
                String value = item.get("settingValue");
                if (key != null) {
                    SystemSetting existing = settingMapper.selectOne(
                            new LambdaQueryWrapper<SystemSetting>().eq(SystemSetting::getSettingKey, key));
                    if (existing != null) {
                        existing.setSettingValue(value);
                        existing.setUpdatedAt(LocalDateTime.now());
                        settingMapper.updateById(existing);
                    } else {
                        SystemSetting newSetting = new SystemSetting();
                        newSetting.setSettingKey(key);
                        newSetting.setSettingValue(value);
                        settingMapper.insert(newSetting);
                    }
                }
            }
        }
        return Result.success(null);
    }

    @PutMapping("/{key}")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "setting", action = "update", detail = "更新设置")
    @Operation(summary = "更新单个设置")
    public Result<Void> update(@PathVariable String key, @RequestBody SettingUpdateDTO dto) {
        SystemSetting existing = settingMapper.selectOne(
                new LambdaQueryWrapper<SystemSetting>().eq(SystemSetting::getSettingKey, key));
        if (existing != null) {
            existing.setSettingValue(dto.getValue());
            existing.setUpdatedAt(LocalDateTime.now());
            settingMapper.updateById(existing);
        } else {
            SystemSetting newSetting = new SystemSetting();
            newSetting.setSettingKey(key);
            newSetting.setSettingValue(dto.getValue());
            settingMapper.insert(newSetting);
        }
        return Result.success(null);
    }
}

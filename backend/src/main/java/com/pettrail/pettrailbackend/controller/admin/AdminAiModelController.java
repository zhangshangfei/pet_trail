package com.pettrail.pettrailbackend.controller.admin;

import com.pettrail.pettrailbackend.annotation.OperationLog;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.*;
import com.pettrail.pettrailbackend.service.AiModelService;
import com.pettrail.pettrailbackend.service.HealthAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/ai-models")
@RequiredArgsConstructor
@Tag(name = "AI模型管理", description = "AI健康分析模型管理接口")
public class AdminAiModelController extends BaseAdminController {

    private final AiModelService aiModelService;
    private final HealthAnalysisService healthAnalysisService;

    @GetMapping
    @Operation(summary = "获取所有AI模型列表")
    @RequireRole("ADMIN")
    public Result<List<AiModelVO>> listModels() {
        return Result.success(aiModelService.listAllModels());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取AI模型仪表盘数据")
    @RequireRole("ADMIN")
    public Result<AiModelDashboardVO> getDashboard() {
        return Result.success(aiModelService.getDashboard());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取单个AI模型详情")
    @RequireRole("ADMIN")
    public Result<AiModelVO> getModel(@PathVariable Long id) {
        AiModelVO model = aiModelService.getModelById(id);
        if (model == null) {
            return Result.error(404, "模型不存在");
        }
        return Result.success(model);
    }

    @GetMapping("/{id}/parameters")
    @Operation(summary = "获取模型参数配置")
    @RequireRole("ADMIN")
    public Result<Map<String, Object>> getModelParameters(@PathVariable Long id) {
        return Result.success(aiModelService.getModelParameters(id));
    }

    @PostMapping
    @Operation(summary = "创建AI模型")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "ai_model", action = "create", detail = "创建AI模型")
    public Result<AiModelVO> createModel(@RequestBody AiModelCreateDTO dto) {
        if (dto.getModelName() == null || dto.getModelName().isEmpty()) {
            return Result.error(400, "模型名称不能为空");
        }
        if (dto.getDisplayName() == null || dto.getDisplayName().isEmpty()) {
            return Result.error(400, "显示名称不能为空");
        }
        try {
            return Result.success(aiModelService.createModel(dto));
        } catch (Exception e) {
            return Result.error(500, "创建模型失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新AI模型配置")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "ai_model", action = "update", detail = "更新AI模型配置")
    public Result<AiModelVO> updateModel(@PathVariable Long id, @RequestBody AiModelUpdateDTO dto) {
        try {
            AiModelVO updated = aiModelService.updateModel(id, dto);
            return Result.success(updated);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}/parameters")
    @Operation(summary = "更新模型参数配置")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "ai_model", action = "update_params", detail = "更新模型参数配置")
    public Result<Void> updateModelParameters(@PathVariable Long id, @RequestBody Map<String, Object> parameters) {
        try {
            aiModelService.updateModelParameters(id, parameters);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "切换模型启用/禁用状态")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "ai_model", action = "toggle_status", detail = "切换模型状态")
    public Result<AiModelVO> setModelStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return Result.error(400, "状态值无效，必须为0或1");
        }
        try {
            return Result.success(aiModelService.setModelStatus(id, status));
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/switch")
    @Operation(summary = "切换当前活动AI模型")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "ai_model", action = "switch", detail = "切换活动AI模型")
    public Result<AiModelVO> switchModel(@RequestBody Map<String, Object> body) {
        Object modelIdObj = body.get("modelId");
        if (modelIdObj == null) {
            return Result.error(400, "目标模型ID不能为空");
        }
        Long modelId;
        try {
            modelId = Long.valueOf(modelIdObj.toString());
        } catch (NumberFormatException e) {
            return Result.error(400, "模型ID格式无效");
        }
        String reason = (String) body.getOrDefault("reason", "管理员手动切换");
        try {
            AiModelVO result = aiModelService.switchModel(modelId, reason);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(500, "模型切换失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除AI模型")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "ai_model", action = "delete", detail = "删除AI模型")
    public Result<Void> deleteModel(@PathVariable Long id) {
        try {
            if (!aiModelService.deleteModel(id)) {
                return Result.error(404, "模型不存在");
            }
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/switch-logs")
    @Operation(summary = "获取模型切换日志")
    @RequireRole("ADMIN")
    public Result<List<AiModelSwitchLogVO>> getSwitchLogs(
            @RequestParam(defaultValue = "20") int limit) {
        return Result.success(aiModelService.getSwitchLogs(limit));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新当前活动模型缓存")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "ai_model", action = "refresh", detail = "刷新模型缓存")
    public Result<Void> refreshModel() {
        aiModelService.refreshCurrentModel();
        return Result.success(null);
    }

    @GetMapping("/cache/stats")
    @Operation(summary = "获取健康分析缓存统计")
    @RequireRole("ADMIN")
    public Result<Map<String, Object>> getCacheStats() {
        return Result.success(healthAnalysisService.getCacheStats());
    }

    @DeleteMapping("/cache")
    @Operation(summary = "清除所有健康分析缓存")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "ai_model", action = "clear_cache", detail = "清除健康分析缓存")
    public Result<Void> clearAllCache() {
        healthAnalysisService.invalidateAllCache();
        return Result.success(null);
    }

    @DeleteMapping("/cache/pet/{petId}")
    @Operation(summary = "清除指定宠物的健康分析缓存")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "ai_model", action = "clear_pet_cache", detail = "清除指定宠物缓存")
    public Result<Void> clearPetCache(@PathVariable Long petId) {
        healthAnalysisService.invalidateCacheByPetId(petId);
        return Result.success(null);
    }
}

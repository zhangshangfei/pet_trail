package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.AiModelVO;
import com.pettrail.pettrailbackend.dto.HealthAnalysisVO;
import com.pettrail.pettrailbackend.dto.Result;
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
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "健康分析", description = "AI宠物健康分析接口")
public class HealthAnalysisController extends BaseController {

    private final HealthAnalysisService healthAnalysisService;
    private final AiModelService aiModelService;

    @PostMapping("/analysis/{petId}")
    @Operation(summary = "获取宠物健康分析报告")
    public Result<HealthAnalysisVO> analyze(@PathVariable Long petId) {
        Long userId = requireLogin();
        HealthAnalysisVO result = healthAnalysisService.analyze(userId, petId);
        if (result == null) {
            return Result.error(404, "宠物不存在或无权限");
        }
        return Result.success(result);
    }

    @GetMapping("/ai-models")
    @Operation(summary = "获取可用的AI模型列表")
    public Result<List<AiModelVO>> listAvailableModels() {
        requireLogin();
        return Result.success(aiModelService.listAllModels());
    }

    @GetMapping("/ai-models/current")
    @Operation(summary = "获取当前使用的AI模型信息")
    public Result<AiModelVO> getCurrentModel() {
        requireLogin();
        com.pettrail.pettrailbackend.entity.AiModel current = aiModelService.getCurrentModel();
        if (current == null) {
            return Result.error(404, "当前无可用AI模型");
        }
        AiModelVO vo = new AiModelVO();
        vo.setId(current.getId());
        vo.setModelName(current.getModelName());
        vo.setDisplayName(current.getDisplayName());
        vo.setProvider(current.getProvider());
        vo.setModelVersion(current.getModelVersion());
        vo.setDescription(current.getDescription());
        vo.setIcon(current.getIcon());
        vo.setIsActive(true);
        return Result.success(vo);
    }

    @PostMapping("/ai-models/switch")
    @Operation(summary = "用户切换AI模型")
    public Result<AiModelVO> switchModel(@RequestBody Map<String, Object> body) {
        Long userId = requireLogin();
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
        String reason = "用户(ID:" + userId + ")手动切换";
        try {
            AiModelVO result = aiModelService.switchModel(modelId, reason);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(500, "模型切换失败: " + e.getMessage());
        }
    }
}

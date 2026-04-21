package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.HealthAnalysisVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.service.HealthAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "健康分析", description = "AI宠物健康分析接口")
public class HealthAnalysisController extends BaseController {

    private final HealthAnalysisService healthAnalysisService;

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
}

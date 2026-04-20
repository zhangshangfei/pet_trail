package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.ReportCreateDTO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Report;
import com.pettrail.pettrailbackend.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController extends BaseController {

    private final ReportService reportService;

    @PostMapping
    public Result<Map<String, Object>> createReport(@RequestBody ReportCreateDTO dto) {
        Long userId = requireLogin();

        Long targetId = dto.getTargetId();
        String targetType = dto.getTargetType();
        String reason = dto.getReason();
        String description = dto.getDescription();

        if (targetId == null || targetType == null || reason == null) {
            return Result.error(400, "缺少必要参数");
        }
        if (!"post".equals(targetType) && !"user".equals(targetType) && !"comment".equals(targetType)) {
            return Result.error(400, "无效的举报类型");
        }

        Report report = reportService.createReport(userId, targetId, targetType, reason, description);
        Map<String, Object> result = new HashMap<>();
        result.put("id", report.getId());
        result.put("status", report.getStatus());
        return Result.success(result);
    }
}

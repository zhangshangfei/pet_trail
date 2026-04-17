package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Report;
import com.pettrail.pettrailbackend.service.ReportService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public Result<Map<String, Object>> createReport(@RequestBody Map<String, Object> data) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        Long targetId = data.get("targetId") != null ? Long.valueOf(data.get("targetId").toString()) : null;
        String targetType = (String) data.get("targetType");
        String reason = (String) data.get("reason");
        String description = (String) data.get("description");

        if (targetId == null || targetType == null || reason == null) {
            return Result.error(400, "缺少必要参数");
        }

        if (!"post".equals(targetType) && !"user".equals(targetType) && !"comment".equals(targetType)) {
            return Result.error(400, "无效的举报类型");
        }

        try {
            Report report = reportService.createReport(userId, targetId, targetType, reason, description);
            Map<String, Object> result = new HashMap<>();
            result.put("id", report.getId());
            result.put("status", report.getStatus());
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(409, e.getMessage());
        } catch (Exception e) {
            log.error("举报失败：{}", e.getMessage(), e);
            return Result.error("举报失败：" + e.getMessage());
        }
    }
}

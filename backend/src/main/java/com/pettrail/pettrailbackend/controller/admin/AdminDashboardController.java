package com.pettrail.pettrailbackend.controller.admin;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin-仪表盘", description = "后台数据统计")
public class AdminDashboardController extends BaseAdminController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "获取总览统计数据")
    public Result<Map<String, Object>> getStats() {
        return Result.success(dashboardService.getStats());
    }

    @GetMapping("/today")
    @Operation(summary = "获取今日数据")
    public Result<Map<String, Object>> getTodayStats() {
        return Result.success(dashboardService.getTodayStats());
    }

    @GetMapping("/trend")
    @Operation(summary = "获取趋势数据")
    public Result<Map<String, Object>> getTrend(@RequestParam(defaultValue = "30") int days) {
        return Result.success(dashboardService.getTrend(days));
    }

    @GetMapping("/audit-stats")
    @Operation(summary = "获取审核统计")
    public Result<Map<String, Object>> getAuditStats() {
        return Result.success(dashboardService.getAuditStats());
    }
}

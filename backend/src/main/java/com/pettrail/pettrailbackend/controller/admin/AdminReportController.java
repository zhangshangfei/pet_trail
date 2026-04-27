package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.ReportHandleDTO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Report;
import com.pettrail.pettrailbackend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Admin-举报管理", description = "后台举报处理")
public class AdminReportController extends BaseAdminController {

    private final ReportService reportService;

    @GetMapping
    @Operation(summary = "分页查询举报列表")
    public Result<Page<Report>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String targetType) {
        return Result.success(reportService.adminListReports(page, size, status, targetType));
    }

    @PutMapping("/{id}/handle")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "report", action = "handle", detail = "处理举报")
    @Operation(summary = "处理举报")
    public Result<Void> handle(@PathVariable Long id, @RequestBody ReportHandleDTO dto) {
        reportService.adminHandleReport(id, dto.getStatus(), dto.getResult());
        return Result.success(null);
    }
}

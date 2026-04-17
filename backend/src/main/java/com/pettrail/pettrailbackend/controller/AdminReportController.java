package com.pettrail.pettrailbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Report;
import com.pettrail.pettrailbackend.mapper.ReportMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Admin-举报管理", description = "后台举报处理")
public class AdminReportController {

    private final ReportMapper reportMapper;

    @GetMapping
    @Operation(summary = "分页查询举报列表")
    public Result<Page<Report>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String targetType) {
        Page<Report> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }
        if (targetType != null && !targetType.isEmpty()) {
            wrapper.eq(Report::getTargetType, targetType);
        }
        wrapper.orderByDesc(Report::getCreatedAt);

        return Result.success(reportMapper.selectPage(pageParam, wrapper));
    }

    @PutMapping("/{id}/handle")
    @Operation(summary = "处理举报")
    public Result<Void> handle(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return Result.error(404, "举报不存在");
        }
        report.setStatus(Integer.valueOf(body.get("status").toString()));
        if (body.containsKey("result")) {
            report.setResult(body.get("result").toString());
        }
        reportMapper.updateById(report);
        return Result.success(null);
    }
}

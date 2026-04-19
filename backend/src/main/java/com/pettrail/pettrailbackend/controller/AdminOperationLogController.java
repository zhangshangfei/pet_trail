package com.pettrail.pettrailbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.AdminOperationLog;
import com.pettrail.pettrailbackend.mapper.AdminOperationLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/operation-logs")
@RequiredArgsConstructor
@Tag(name = "Admin-操作日志", description = "后台操作日志")
public class AdminOperationLogController {

    private final AdminOperationLogMapper logMapper;

    @GetMapping
    @Operation(summary = "分页查询操作日志")
    public Result<Page<AdminOperationLog>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String adminName,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Page<AdminOperationLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<>();

        if (adminName != null && !adminName.isEmpty()) {
            wrapper.like(AdminOperationLog::getAdminName, adminName);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(AdminOperationLog::getModule, module);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(AdminOperationLog::getCreatedAt, startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(AdminOperationLog::getCreatedAt, endDate + " 23:59:59");
        }
        wrapper.orderByDesc(AdminOperationLog::getCreatedAt);

        return Result.success(logMapper.selectPage(pageParam, wrapper));
    }
}

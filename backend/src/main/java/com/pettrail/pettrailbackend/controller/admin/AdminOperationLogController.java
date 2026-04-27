package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.AdminOperationLog;
import com.pettrail.pettrailbackend.service.AdminOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/operation-logs")
@RequiredArgsConstructor
@Tag(name = "Admin-操作日志", description = "后台操作日志")
public class AdminOperationLogController extends BaseAdminController {

    private final AdminOperationLogService operationLogService;

    @GetMapping
    @Operation(summary = "分页查询操作日志")
    public Result<Page<AdminOperationLog>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String adminName,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(operationLogService.adminListLogs(page, size, adminName, module, startDate, endDate));
    }
}

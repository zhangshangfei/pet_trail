package com.pettrail.pettrailbackend.controller.admin;

import com.pettrail.pettrailbackend.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
@Tag(name = "Admin-数据导出", description = "后台数据导出")
public class AdminExportController extends BaseAdminController {

    private final ExportService exportService;

    @GetMapping("/users")
    @Operation(summary = "导出用户数据")
    public void exportUsers(HttpServletResponse response,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) Integer status) throws IOException {
        exportService.exportUsers(response, keyword, status);
    }

    @GetMapping("/posts")
    @Operation(summary = "导出动态数据")
    public void exportPosts(HttpServletResponse response,
                            @RequestParam(required = false) Integer auditStatus) throws IOException {
        exportService.exportPosts(response, auditStatus);
    }

    @GetMapping("/reports")
    @Operation(summary = "导出举报数据")
    public void exportReports(HttpServletResponse response,
                              @RequestParam(required = false) Integer status) throws IOException {
        exportService.exportReports(response, status);
    }

    @GetMapping("/logs")
    @Operation(summary = "导出操作日志")
    public void exportLogs(HttpServletResponse response,
                           @RequestParam(required = false) String module) throws IOException {
        exportService.exportLogs(response, module);
    }
}

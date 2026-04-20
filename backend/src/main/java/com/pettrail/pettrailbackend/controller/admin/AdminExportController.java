package com.pettrail.pettrailbackend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
@Tag(name = "Admin-数据导出", description = "后台数据导出")
public class AdminExportController extends BaseAdminController {

    @GetMapping("/users")
    @Operation(summary = "导出用户数据")
    public void exportUsers(HttpServletResponse response,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) Integer status) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");
        response.getWriter().write("export placeholder - users");
    }

    @GetMapping("/posts")
    @Operation(summary = "导出动态数据")
    public void exportPosts(HttpServletResponse response,
                            @RequestParam(required = false) Integer auditStatus) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=posts.xlsx");
        response.getWriter().write("export placeholder - posts");
    }

    @GetMapping("/reports")
    @Operation(summary = "导出举报数据")
    public void exportReports(HttpServletResponse response,
                              @RequestParam(required = false) Integer status) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=reports.xlsx");
        response.getWriter().write("export placeholder - reports");
    }

    @GetMapping("/logs")
    @Operation(summary = "导出操作日志")
    public void exportLogs(HttpServletResponse response,
                           @RequestParam(required = false) String module) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=logs.xlsx");
        response.getWriter().write("export placeholder - logs");
    }
}

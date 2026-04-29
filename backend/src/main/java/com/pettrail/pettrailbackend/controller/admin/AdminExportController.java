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
        requireExportPermission(3L);
        exportService.exportUsers(response, keyword, status);
    }

    @GetMapping("/posts")
    @Operation(summary = "导出动态数据")
    public void exportPosts(HttpServletResponse response,
                            @RequestParam(required = false) Integer auditStatus) throws IOException {
        requireExportPermission(6L);
        exportService.exportPosts(response, auditStatus);
    }

    @GetMapping("/reports")
    @Operation(summary = "导出举报数据")
    public void exportReports(HttpServletResponse response,
                              @RequestParam(required = false) Integer status) throws IOException {
        requireExportPermission(9L);
        exportService.exportReports(response, status);
    }

    @GetMapping("/logs")
    @Operation(summary = "导出操作日志")
    public void exportLogs(HttpServletResponse response,
                           @RequestParam(required = false) String module) throws IOException {
        requireExportPermission(18L);
        exportService.exportLogs(response, module);
    }

    @GetMapping("/pets")
    @Operation(summary = "导出宠物数据")
    public void exportPets(HttpServletResponse response,
                           @RequestParam(required = false) Integer category) throws IOException {
        requireExportPermission(5L);
        exportService.exportPets(response, category);
    }

    @GetMapping("/challenges")
    @Operation(summary = "导出挑战赛数据")
    public void exportChallenges(HttpServletResponse response,
                                  @RequestParam(required = false) Integer status) throws IOException {
        requireExportPermission(11L);
        exportService.exportChallenges(response, status);
    }

    @GetMapping("/products")
    @Operation(summary = "导出商品数据")
    public void exportProducts(HttpServletResponse response,
                                @RequestParam(required = false) Integer status) throws IOException {
        requireExportPermission(12L);
        exportService.exportProducts(response, status);
    }
}

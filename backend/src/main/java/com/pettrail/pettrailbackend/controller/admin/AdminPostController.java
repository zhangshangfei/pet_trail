package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.annotation.OperationLog;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.PostAuditDTO;
import com.pettrail.pettrailbackend.dto.PostBatchAuditDTO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@Tag(name = "Admin-动态管理", description = "后台动态内容管理")
public class AdminPostController extends BaseAdminController {

    private final PostService postService;

    @GetMapping
    @Operation(summary = "分页查询动态列表")
    public Result<Page<Post>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(postService.adminListPosts(page, size, userId, auditStatus));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取动态详情")
    public Result<Post> getDetail(@PathVariable Long id) {
        return Result.success(postService.adminGetPostDetail(id));
    }

    @PutMapping("/{id}/audit")
    @Operation(summary = "审核动态（通过/拒绝）")
    @OperationLog(module = "post", action = "audit", detail = "审核动态")
    public Result<Void> audit(@PathVariable Long id, @RequestBody PostAuditDTO dto) {
        postService.adminAuditPost(id, dto.getAuditStatus(), dto.getAuditRemark());
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除动态")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "post", action = "delete", detail = "删除动态")
    public Result<Void> delete(@PathVariable Long id) {
        postService.adminDeletePost(id);
        return Result.success(null);
    }

    @PutMapping("/batch-audit")
    @Operation(summary = "批量审核动态")
    @OperationLog(module = "post", action = "batch_audit", detail = "批量审核动态")
    public Result<Void> batchAudit(@RequestBody PostBatchAuditDTO dto) {
        postService.adminBatchAudit(dto.getPostIds(), dto.getAuditStatus(), dto.getAuditRemark());
        return Result.success(null);
    }

    @GetMapping("/deleted")
    @Operation(summary = "查询已删除动态")
    public Result<Page<Post>> deletedList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(postService.adminListDeletedPosts(page, size));
    }

    @PutMapping("/{id}/restore")
    @Operation(summary = "恢复已删除动态")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "post", action = "restore", detail = "恢复动态")
    public Result<Void> restore(@PathVariable Long id) {
        postService.adminRestorePost(id);
        return Result.success(null);
    }
}

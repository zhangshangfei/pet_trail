package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.PostComment;
import com.pettrail.pettrailbackend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@Tag(name = "Admin-评论管理", description = "后台评论管理")
public class AdminCommentController extends BaseAdminController {

    private final CommentService commentService;

    @GetMapping
    @Operation(summary = "分页查询评论列表")
    public Result<Page<PostComment>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) Integer deleted) {
        return Result.success(commentService.adminListComments(page, size, keyword, postId, deleted));
    }

    @DeleteMapping("/{id}")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "comment", action = "delete", detail = "删除评论")
    @Operation(summary = "删除评论（逻辑删除）")
    @RequireRole("SUPER_ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.adminDeleteComment(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/restore")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "comment", action = "restore", detail = "恢复评论")
    @Operation(summary = "恢复评论")
    @RequireRole("SUPER_ADMIN")
    public Result<Void> restore(@PathVariable Long id) {
        commentService.adminRestoreComment(id);
        return Result.success(null);
    }
}

package com.pettrail.pettrailbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.PostComment;
import com.pettrail.pettrailbackend.mapper.PostCommentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@Tag(name = "Admin-评论管理", description = "后台评论管理")
public class AdminCommentController {

    private final PostCommentMapper commentMapper;

    @GetMapping
    @Operation(summary = "分页查询评论列表")
    public Result<Page<PostComment>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) Integer deleted) {
        Page<PostComment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(PostComment::getContent, keyword);
        }
        if (postId != null) {
            wrapper.eq(PostComment::getPostId, postId);
        }
        if (deleted != null) {
            wrapper.eq(PostComment::getStatus, deleted == 1 ? 0 : 1);
        }
        wrapper.orderByDesc(PostComment::getCreatedAt);

        return Result.success(commentMapper.selectPage(pageParam, wrapper));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论（逻辑删除）")
    @RequireRole("SUPER_ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        PostComment comment = commentMapper.selectById(id);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }
        comment.setStatus(0);
        commentMapper.updateById(comment);
        return Result.success(null);
    }

    @PutMapping("/{id}/restore")
    @Operation(summary = "恢复评论")
    @RequireRole("SUPER_ADMIN")
    public Result<Void> restore(@PathVariable Long id) {
        PostComment comment = commentMapper.selectById(id);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }
        comment.setStatus(1);
        commentMapper.updateById(comment);
        return Result.success(null);
    }
}

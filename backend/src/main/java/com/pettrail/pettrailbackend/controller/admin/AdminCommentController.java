package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.PostComment;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.PostCommentMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@Tag(name = "Admin-评论管理", description = "后台评论管理")
public class AdminCommentController extends BaseAdminController {

    private final PostCommentMapper commentMapper;
    private final UserMapper userMapper;

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

        Page<PostComment> result = commentMapper.selectPage(pageParam, wrapper);
        fillCommentUserNickname(result);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "comment", action = "delete", detail = "删除评论")
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
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "comment", action = "restore", detail = "恢复评论")
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

    private void fillCommentUserNickname(Page<PostComment> result) {
        Set<Long> userIds = result.getRecords().stream()
                .map(PostComment::getUserId)
                .filter(uid -> uid != null)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        Map<Long, String> nicknameMap = buildNicknameMap(userIds, userMapper);
        for (PostComment comment : result.getRecords()) {
            if (comment.getUserId() != null) {
                comment.setUserNickname(nicknameMap.getOrDefault(comment.getUserId(), "未知用户"));
            }
        }
    }
}

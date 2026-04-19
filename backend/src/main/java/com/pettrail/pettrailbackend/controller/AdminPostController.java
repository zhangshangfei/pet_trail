package com.pettrail.pettrailbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.annotation.OperationLog;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@Tag(name = "Admin-动态管理", description = "后台动态内容管理")
public class AdminPostController {

    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @GetMapping
    @Operation(summary = "分页查询动态列表")
    public Result<Page<Post>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer auditStatus) {
        Page<Post> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(Post::getUserId, userId);
        }
        if (auditStatus != null) {
            wrapper.eq(Post::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(Post::getCreatedAt);

        Page<Post> result = postMapper.selectPage(pageParam, wrapper);
        fillUserNickname(result);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取动态详情")
    public Result<Post> getDetail(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.error(404, "动态不存在");
        }
        if (post.getUserId() != null) {
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                post.setUserNickname(user.getNickname());
            }
        }
        return Result.success(post);
    }

    @PutMapping("/{id}/audit")
    @Operation(summary = "审核动态（通过/拒绝）")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "post", action = "audit", detail = "审核动态")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.error(404, "动态不存在");
        }
        post.setAuditStatus(Integer.valueOf(body.get("auditStatus").toString()));
        if (body.containsKey("auditRemark") && body.get("auditRemark") != null) {
            post.setAuditRemark(body.get("auditRemark").toString());
        }
        postMapper.updateById(post);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除动态")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "post", action = "delete", detail = "删除动态")
    public Result<Void> delete(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.error(404, "动态不存在");
        }
        post.setDeleted(1);
        postMapper.updateById(post);
        return Result.success(null);
    }

    @PutMapping("/batch-audit")
    @Operation(summary = "批量审核动态")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "post", action = "batch_audit", detail = "批量审核动态")
    public Result<Void> batchAudit(@RequestBody Map<String, Object> body) {
        List<Integer> postIds = (List<Integer>) body.get("postIds");
        Integer auditStatus = (Integer) body.get("auditStatus");
        String auditRemark = body.get("auditRemark") != null ? body.get("auditRemark").toString() : null;

        if (postIds == null || postIds.isEmpty() || auditStatus == null) {
            return Result.error(400, "参数不完整");
        }

        for (Integer postId : postIds) {
            Post post = postMapper.selectById(postId.longValue());
            if (post != null) {
                post.setAuditStatus(auditStatus);
                if (auditRemark != null) {
                    post.setAuditRemark(auditRemark);
                }
                postMapper.updateById(post);
            }
        }

        return Result.success(null);
    }

    @GetMapping("/deleted")
    @Operation(summary = "查询已删除动态")
    public Result<Page<Post>> deletedList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Post> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getDeleted, 1);
        wrapper.orderByDesc(Post::getCreatedAt);
        Page<Post> result = postMapper.selectPage(pageParam, wrapper);
        fillUserNickname(result);
        return Result.success(result);
    }

    @PutMapping("/{id}/restore")
    @Operation(summary = "恢复已删除动态")
    @RequireRole("SUPER_ADMIN")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "post", action = "restore", detail = "恢复动态")
    public Result<Void> restore(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.error(404, "动态不存在");
        }
        post.setDeleted(0);
        postMapper.updateById(post);
        return Result.success(null);
    }

    private void fillUserNickname(Page<Post> result) {
        Set<Long> userIds = result.getRecords().stream()
                .map(Post::getUserId)
                .filter(uid -> uid != null)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        Map<Long, String> nicknameMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u.getNickname() != null ? u.getNickname() : "用户" + u.getId(), (a, b) -> a));
        for (Post post : result.getRecords()) {
            if (post.getUserId() != null) {
                post.setUserNickname(nicknameMap.getOrDefault(post.getUserId(), "未知用户"));
            }
        }
    }
}

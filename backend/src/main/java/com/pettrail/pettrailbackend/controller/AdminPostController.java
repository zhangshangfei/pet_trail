package com.pettrail.pettrailbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@Tag(name = "Admin-动态管理", description = "后台动态内容管理")
public class AdminPostController {

    private final PostMapper postMapper;

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

        return Result.success(postMapper.selectPage(pageParam, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取动态详情")
    public Result<Post> getDetail(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.error(404, "动态不存在");
        }
        return Result.success(post);
    }

    @PutMapping("/{id}/audit")
    @Operation(summary = "审核动态（通过/拒绝）")
    public Result<Void> audit(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.error(404, "动态不存在");
        }
        post.setAuditStatus(body.get("auditStatus"));
        if (body.containsKey("auditRemark")) {
            post.setAuditRemark(body.get("auditRemark").toString());
        }
        postMapper.updateById(post);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除动态")
    public Result<Void> delete(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.error(404, "动态不存在");
        }
        post.setDeleted(1);
        postMapper.updateById(post);
        return Result.success(null);
    }
}

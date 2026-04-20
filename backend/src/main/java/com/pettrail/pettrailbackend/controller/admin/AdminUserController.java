package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.annotation.OperationLog;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.dto.StatusDTO;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin-用户管理", description = "后台用户管理")
public class AdminUserController extends BaseAdminController {

    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final FollowMapper followMapper;

    @GetMapping
    @Operation(summary = "分页查询用户列表")
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword).or().like(User::getOpenid, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        return Result.success(userMapper.selectPage(pageParam, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<User> getDetail(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态（启用/禁用）")
    @RequireRole("SUPER_ADMIN")
    @OperationLog(module = "user", action = "update_status", detail = "更新用户状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setStatus(dto.getStatus());
        userMapper.updateById(user);
        return Result.success(null);
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "获取用户统计信息")
    public Result<Map<String, Object>> getUserStats(@PathVariable Long id) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("petCount", petMapper.selectCount(new LambdaQueryWrapper<com.pettrail.pettrailbackend.entity.Pet>().eq(com.pettrail.pettrailbackend.entity.Pet::getUserId, id)));
        stats.put("postCount", postMapper.selectCount(new LambdaQueryWrapper<com.pettrail.pettrailbackend.entity.Post>().eq(com.pettrail.pettrailbackend.entity.Post::getUserId, id)));
        stats.put("likeCount", postLikeMapper.selectCount(new LambdaQueryWrapper<com.pettrail.pettrailbackend.entity.PostLike>().eq(com.pettrail.pettrailbackend.entity.PostLike::getUserId, id)));
        stats.put("followerCount", followMapper.selectCount(new LambdaQueryWrapper<com.pettrail.pettrailbackend.entity.Follow>().eq(com.pettrail.pettrailbackend.entity.Follow::getFolloweeId, id)));
        stats.put("followingCount", followMapper.selectCount(new LambdaQueryWrapper<com.pettrail.pettrailbackend.entity.Follow>().eq(com.pettrail.pettrailbackend.entity.Follow::getFollowerId, id)));
        return Result.success(stats);
    }
}

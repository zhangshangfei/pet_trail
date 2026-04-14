package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.service.FollowService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关注控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * 关注/取消关注
     */
    @PostMapping("/{followeeId}")
    public Result<Map<String, Object>> toggleFollow(@PathVariable Long followeeId) {
        Long followerId = UserContext.getCurrentUserId();
        if (followerId == null) {
            return Result.error(401, "用户未登录");
        }

        boolean isFollowing = followService.toggleFollow(followerId, followeeId);

        Map<String, Object> result = new HashMap<>();
        result.put("following", isFollowing);
        
        // 获取最新的粉丝数
        int followerCount = followService.getFollowerCount(followeeId);
        result.put("followerCount", followerCount);

        return Result.success(result);
    }

    /**
     * 检查是否已关注
     */
    @GetMapping("/check/{followeeId}")
    public Result<Map<String, Object>> checkFollow(@PathVariable Long followeeId) {
        Long followerId = UserContext.getCurrentUserId();
        if (followerId == null) {
            return Result.error(401, "用户未登录");
        }

        boolean isFollowing = followService.isFollowing(followerId, followeeId);
        int followerCount = followService.getFollowerCount(followeeId);
        int followeeCount = followService.getFolloweeCount(followerId);

        Map<String, Object> result = new HashMap<>();
        result.put("following", isFollowing);
        result.put("followerCount", followerCount);
        result.put("followeeCount", followeeCount);

        return Result.success(result);
    }

    /**
     * 获取关注列表
     */
    @GetMapping("/list")
    public Result<List<Long>> getFollowList() {
        Long followerId = UserContext.getCurrentUserId();
        if (followerId == null) {
            return Result.error(401, "用户未登录");
        }

        List<Long> followeeIds = followService.getFolloweeIds(followerId);
        return Result.success(followeeIds);
    }
}

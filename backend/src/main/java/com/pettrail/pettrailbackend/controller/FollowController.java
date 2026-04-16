package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.service.FollowService;
import com.pettrail.pettrailbackend.service.UserService;
import com.pettrail.pettrailbackend.service.PostService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    private final UserService userService;
    private final PostService postService;

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

    @GetMapping("/followees")
    public Result<List<Map<String, Object>>> getFolloweeList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<Long> followeeIds = followService.getFolloweeIds(userId);
            Long currentUserId = UserContext.getCurrentUserId();
            List<Long> currentFolloweeIds = currentUserId != null ? followService.getFolloweeIds(currentUserId) : new ArrayList<>();

            List<Map<String, Object>> result = new ArrayList<>();
            int start = (page - 1) * size;
            int end = Math.min(start + size, followeeIds.size());
            for (int i = start; i < end; i++) {
                Long followeeId = followeeIds.get(i);
                User user;
                try {
                    user = userService.getProfile(followeeId);
                } catch (Exception e) {
                    continue;
                }
                if (user.getStatus() != null && user.getStatus() == 0) continue;
                Map<String, Object> item = new HashMap<>();
                item.put("id", user.getId());
                item.put("nickname", user.getNickname());
                item.put("avatar", user.getAvatar());
                item.put("gender", user.getGender());
                item.put("followerCount", followService.getFollowerCount(user.getId()));
                item.put("postCount", postService.getUserPostCount(user.getId()));
                item.put("isFollowing", currentFolloweeIds.contains(user.getId()));
                result.add(item);
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取关注列表失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/followers")
    public Result<List<Map<String, Object>>> getFollowerList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<Long> followerIds = followService.getFollowerIds(userId);
            Long currentUserId = UserContext.getCurrentUserId();
            List<Long> currentFolloweeIds = currentUserId != null ? followService.getFolloweeIds(currentUserId) : new ArrayList<>();

            List<Map<String, Object>> result = new ArrayList<>();
            int start = (page - 1) * size;
            int end = Math.min(start + size, followerIds.size());
            for (int i = start; i < end; i++) {
                Long followerId = followerIds.get(i);
                User user;
                try {
                    user = userService.getProfile(followerId);
                } catch (Exception e) {
                    continue;
                }
                if (user.getStatus() != null && user.getStatus() == 0) continue;
                Map<String, Object> item = new HashMap<>();
                item.put("id", user.getId());
                item.put("nickname", user.getNickname());
                item.put("avatar", user.getAvatar());
                item.put("gender", user.getGender());
                item.put("followerCount", followService.getFollowerCount(user.getId()));
                item.put("postCount", postService.getUserPostCount(user.getId()));
                item.put("isFollowing", currentFolloweeIds.contains(user.getId()));
                result.add(item);
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取粉丝列表失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }
}

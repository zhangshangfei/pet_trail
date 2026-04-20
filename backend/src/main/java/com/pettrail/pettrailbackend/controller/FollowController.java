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

@Slf4j
@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController extends BaseController {

    private final FollowService followService;
    private final UserService userService;
    private final PostService postService;

    @PostMapping("/{followeeId}")
    public Result<Map<String, Object>> toggleFollow(@PathVariable Long followeeId) {
        Long followerId = requireLogin();
        boolean isFollowing = followService.toggleFollow(followerId, followeeId);
        Map<String, Object> result = new HashMap<>();
        result.put("following", isFollowing);
        result.put("followerCount", followService.getFollowerCount(followeeId));
        return Result.success(result);
    }

    @GetMapping("/check/{followeeId}")
    public Result<Map<String, Object>> checkFollow(@PathVariable Long followeeId) {
        Long followerId = requireLogin();
        Map<String, Object> result = new HashMap<>();
        result.put("following", followService.isFollowing(followerId, followeeId));
        result.put("followerCount", followService.getFollowerCount(followeeId));
        result.put("followeeCount", followService.getFolloweeCount(followerId));
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<List<Long>> getFollowList() {
        Long followerId = requireLogin();
        return Result.success(followService.getFolloweeIds(followerId));
    }

    @GetMapping("/followees")
    public Result<List<Map<String, Object>>> getFolloweeList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Long> followeeIds = followService.getFolloweeIds(userId);
        Long currentUserId = UserContext.getCurrentUserId();
        List<Long> currentFolloweeIds = currentUserId != null ? followService.getFolloweeIds(currentUserId) : new ArrayList<>();
        return Result.success(buildUserList(followeeIds, currentFolloweeIds, page, size));
    }

    @GetMapping("/followers")
    public Result<List<Map<String, Object>>> getFollowerList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Long> followerIds = followService.getFollowerIds(userId);
        Long currentUserId = UserContext.getCurrentUserId();
        List<Long> currentFolloweeIds = currentUserId != null ? followService.getFolloweeIds(currentUserId) : new ArrayList<>();
        return Result.success(buildUserList(followerIds, currentFolloweeIds, page, size));
    }

    private List<Map<String, Object>> buildUserList(List<Long> userIds, List<Long> currentFolloweeIds, int page, int size) {
        List<Map<String, Object>> result = new ArrayList<>();
        int start = (page - 1) * size;
        int end = Math.min(start + size, userIds.size());
        for (int i = start; i < end; i++) {
            Long uid = userIds.get(i);
            User user;
            try {
                user = userService.getProfile(uid);
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
        return result;
    }
}

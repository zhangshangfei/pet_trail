package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.FollowActionVO;
import com.pettrail.pettrailbackend.dto.FollowUserVO;
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
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController extends BaseController {

    private final FollowService followService;
    private final UserService userService;
    private final PostService postService;

    @PostMapping("/{followeeId}")
    public Result<FollowActionVO> toggleFollow(@PathVariable Long followeeId) {
        Long followerId = requireLogin();
        boolean isFollowing = followService.toggleFollow(followerId, followeeId);
        FollowActionVO vo = new FollowActionVO();
        vo.setFollowing(isFollowing);
        vo.setFollowerCount(followService.getFollowerCount(followeeId));
        return Result.success(vo);
    }

    @GetMapping("/check/{followeeId}")
    public Result<FollowActionVO> checkFollow(@PathVariable Long followeeId) {
        Long followerId = requireLogin();
        FollowActionVO vo = new FollowActionVO();
        vo.setFollowing(followService.isFollowing(followerId, followeeId));
        vo.setFollowerCount(followService.getFollowerCount(followeeId));
        vo.setFolloweeCount(followService.getFolloweeCount(followerId));
        return Result.success(vo);
    }

    @GetMapping("/list")
    public Result<List<Long>> getFollowList() {
        Long followerId = requireLogin();
        return Result.success(followService.getFolloweeIds(followerId));
    }

    @GetMapping("/followees")
    public Result<List<FollowUserVO>> getFolloweeList(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (userId == null) {
            userId = requireLogin();
        }
        List<Long> followeeIds = followService.getFolloweeIds(userId);
        Long currentUserId = UserContext.getCurrentUserId();
        List<Long> currentFolloweeIds = currentUserId != null ? followService.getFolloweeIds(currentUserId) : new ArrayList<>();
        return Result.success(buildUserList(followeeIds, currentFolloweeIds, page, size));
    }

    @GetMapping("/followers")
    public Result<List<FollowUserVO>> getFollowerList(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (userId == null) {
            userId = requireLogin();
        }
        List<Long> followerIds = followService.getFollowerIds(userId);
        Long currentUserId = UserContext.getCurrentUserId();
        List<Long> currentFolloweeIds = currentUserId != null ? followService.getFolloweeIds(currentUserId) : new ArrayList<>();
        return Result.success(buildUserList(followerIds, currentFolloweeIds, page, size));
    }

    private List<FollowUserVO> buildUserList(List<Long> userIds, List<Long> currentFolloweeIds, int page, int size) {
        List<FollowUserVO> result = new ArrayList<>();
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
            FollowUserVO vo = new FollowUserVO();
            vo.setId(user.getId());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setGender(user.getGender());
            vo.setFollowerCount(followService.getFollowerCount(user.getId()));
            vo.setFolloweeCount(followService.getFolloweeCount(user.getId()));
            vo.setPostCount(postService.getUserPostCount(user.getId()));
            vo.setIsFollowing(currentFolloweeIds.contains(user.getId()));
            result.add(vo);
        }
        return result;
    }
}

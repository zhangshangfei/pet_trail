package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.UserBehavior;
import com.pettrail.pettrailbackend.mapper.UserBehaviorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserBehaviorService {

    private final UserBehaviorMapper userBehaviorMapper;

    @Async
    public void recordBehavior(Long userId, String action, String targetType, Long targetId, Integer duration, String extra) {
        try {
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setAction(action);
            behavior.setTargetType(targetType);
            behavior.setTargetId(targetId);
            behavior.setDuration(duration != null ? duration : 0);
            behavior.setExtra(extra);
            userBehaviorMapper.insert(behavior);
        } catch (Exception e) {
            log.warn("记录用户行为失败: userId={}, action={}, error={}", userId, action, e.getMessage());
        }
    }

    public void recordView(Long userId, String targetType, Long targetId, Integer duration) {
        recordBehavior(userId, "view", targetType, targetId, duration, null);
    }

    public void recordLike(Long userId, Long postId) {
        recordBehavior(userId, "like", "post", postId, null, null);
    }

    public void recordComment(Long userId, Long postId) {
        recordBehavior(userId, "comment", "post", postId, null, null);
    }

    public void recordShare(Long userId, Long postId) {
        recordBehavior(userId, "share", "post", postId, null, null);
    }

    public void recordFollow(Long userId, Long targetUserId) {
        recordBehavior(userId, "follow", "user", targetUserId, null, null);
    }

    public void recordCollect(Long userId, Long postId) {
        recordBehavior(userId, "collect", "post", postId, null, null);
    }

    public void recordPublish(Long userId, Long postId) {
        recordBehavior(userId, "publish", "post", postId, null, null);
    }

    public List<Long> getRecentViewedPostIds(Long userId, int limit) {
        return userBehaviorMapper.selectRecentTargetIds(userId, "view", "post", limit);
    }

    public List<Long> getRecentLikedPostIds(Long userId, int limit) {
        return userBehaviorMapper.selectRecentTargetIds(userId, "like", "post", limit);
    }

    public List<Long> getRecentFollowedUserIds(Long userId, int limit) {
        return userBehaviorMapper.selectRecentTargetIds(userId, "follow", "user", limit);
    }

    public List<Long> getRecentCollectedPostIds(Long userId, int limit) {
        return userBehaviorMapper.selectRecentTargetIds(userId, "collect", "post", limit);
    }

    public List<Map<String, Object>> getActionCountsSince(Long userId, LocalDateTime since) {
        return userBehaviorMapper.countByActionSince(userId, since.toString());
    }

    public List<Map<String, Object>> getMostViewedPosts(Long userId, LocalDateTime since, int limit) {
        return userBehaviorMapper.selectMostViewedPosts(userId, since.toString(), limit);
    }

    public long getBehaviorCount(Long userId, String action, LocalDateTime since) {
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBehavior::getUserId, userId);
        wrapper.eq(UserBehavior::getAction, action);
        if (since != null) {
            wrapper.ge(UserBehavior::getCreatedAt, since);
        }
        return userBehaviorMapper.selectCount(wrapper);
    }
}

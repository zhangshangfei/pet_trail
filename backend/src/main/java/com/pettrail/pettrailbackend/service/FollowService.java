package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.entity.Follow;
import com.pettrail.pettrailbackend.mapper.FollowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowMapper followMapper;
    private final NotificationService notificationService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String FOLLOWER_COUNT_PREFIX = "follow:count:follower:";
    private static final String FOLLOWEE_COUNT_PREFIX = "follow:count:followee:";
    private static final String IS_FOLLOWING_PREFIX = "follow:is:";
    private static final long COUNT_CACHE_TTL_MINUTES = 10;
    private static final long IS_FOLLOWING_TTL_HOURS = 1;

    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFollow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("不能关注自己");
        }

        Follow existingFollow = followMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, followerId)
                        .eq(Follow::getFolloweeId, followeeId)
        );

        if (existingFollow != null) {
            followMapper.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
            log.info("取消关注: followerId={}, followeeId={}", followerId, followeeId);

            evictFollowCache(followerId, followeeId);
            return false;
        } else {
            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFolloweeId(followeeId);
            followMapper.insert(follow);
            log.info("关注成功: followerId={}, followeeId={}", followerId, followeeId);

            notificationService.createNotification(
                followeeId, followerId, "follow", followerId, "关注了你");

            evictFollowCache(followerId, followeeId);
            return true;
        }
    }

    public boolean isFollowing(Long followerId, Long followeeId) {
        String cacheKey = IS_FOLLOWING_PREFIX + followerId + ":" + followeeId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return "1".equals(cached.toString());
            }
        } catch (Exception e) {
            log.warn("关注状态缓存读取异常: {}", e.getMessage());
        }

        Follow follow = followMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, followerId)
                        .eq(Follow::getFolloweeId, followeeId)
        );
        boolean result = follow != null;

        try {
            redisTemplate.opsForValue().set(cacheKey, result ? "1" : "0", IS_FOLLOWING_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("关注状态缓存写入异常: {}", e.getMessage());
        }

        return result;
    }

    public int getFollowerCount(Long followeeId) {
        String cacheKey = FOLLOWER_COUNT_PREFIX + followeeId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return Integer.parseInt(cached.toString());
            }
        } catch (Exception e) {
            log.warn("粉丝数缓存读取异常: {}", e.getMessage());
        }

        int count = followMapper.countFollowers(followeeId);
        try {
            redisTemplate.opsForValue().set(cacheKey, count, COUNT_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("粉丝数缓存写入异常: {}", e.getMessage());
        }
        return count;
    }

    public int getFolloweeCount(Long followerId) {
        String cacheKey = FOLLOWEE_COUNT_PREFIX + followerId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return Integer.parseInt(cached.toString());
            }
        } catch (Exception e) {
            log.warn("关注数缓存读取异常: {}", e.getMessage());
        }

        int count = followMapper.countFollowees(followerId);
        try {
            redisTemplate.opsForValue().set(cacheKey, count, COUNT_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("关注数缓存写入异常: {}", e.getMessage());
        }
        return count;
    }

    public List<Long> getFolloweeIds(Long followerId) {
        return followMapper.selectFolloweeIds(followerId);
    }

    public List<Long> getFollowerIds(Long followeeId) {
        return followMapper.selectFollowerIds(followeeId);
    }

    private void evictFollowCache(Long followerId, Long followeeId) {
        try {
            redisTemplate.delete(FOLLOWER_COUNT_PREFIX + followeeId);
            redisTemplate.delete(FOLLOWEE_COUNT_PREFIX + followerId);
            redisTemplate.delete(IS_FOLLOWING_PREFIX + followerId + ":" + followeeId);
        } catch (Exception e) {
            log.warn("关注缓存清除异常: {}", e.getMessage());
        }
    }
}

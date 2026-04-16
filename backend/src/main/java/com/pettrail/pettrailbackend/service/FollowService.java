package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.entity.Follow;
import com.pettrail.pettrailbackend.mapper.FollowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 关注服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowMapper followMapper;
    private final NotificationService notificationService;

    /**
     * 关注/取消关注
     *
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @return true-关注成功, false-取消关注成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFollow(Long followerId, Long followeeId) {
        // 不能关注自己
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("不能关注自己");
        }

        // 检查是否已关注
        Follow existingFollow = followMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, followerId)
                        .eq(Follow::getFolloweeId, followeeId)
        );

        if (existingFollow != null) {
            // 取消关注
            followMapper.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
            log.info("取消关注: followerId={}, followeeId={}", followerId, followeeId);
            return false;
        } else {
            // 关注
            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFolloweeId(followeeId);
            followMapper.insert(follow);
            log.info("关注成功: followerId={}, followeeId={}", followerId, followeeId);

            notificationService.createNotification(
                followeeId, followerId, "follow", followerId, "关注了你");
            return true;
        }
    }

    /**
     * 检查是否已关注
     *
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @return true-已关注, false-未关注
     */
    public boolean isFollowing(Long followerId, Long followeeId) {
        Follow follow = followMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, followerId)
                        .eq(Follow::getFolloweeId, followeeId)
        );
        return follow != null;
    }

    /**
     * 获取用户的粉丝数
     *
     * @param followeeId 被关注者ID
     * @return 粉丝数
     */
    public int getFollowerCount(Long followeeId) {
        return followMapper.countFollowers(followeeId);
    }

    /**
     * 获取用户的关注数
     *
     * @param followerId 关注者ID
     * @return 关注数
     */
    public int getFolloweeCount(Long followerId) {
        return followMapper.countFollowees(followerId);
    }

    /**
     * 获取用户关注的用户ID列表
     *
     * @param followerId 关注者ID
     * @return 关注的用户ID列表
     */
    public List<Long> getFolloweeIds(Long followerId) {
        return followMapper.selectFolloweeIds(followerId);
    }

    public List<Long> getFollowerIds(Long followeeId) {
        return followMapper.selectFollowerIds(followeeId);
    }
}

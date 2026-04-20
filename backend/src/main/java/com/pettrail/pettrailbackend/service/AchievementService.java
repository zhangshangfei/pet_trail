package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.AchievementVO;
import com.pettrail.pettrailbackend.entity.Achievement;
import com.pettrail.pettrailbackend.entity.CheckinStats;
import com.pettrail.pettrailbackend.entity.UserAchievement;
import com.pettrail.pettrailbackend.mapper.AchievementMapper;
import com.pettrail.pettrailbackend.mapper.CheckinStatsMapper;
import com.pettrail.pettrailbackend.mapper.PetMapper;
import com.pettrail.pettrailbackend.mapper.PostLikeMapper;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import com.pettrail.pettrailbackend.mapper.UserAchievementMapper;
import com.pettrail.pettrailbackend.mapper.WeightRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final CheckinStatsMapper checkinStatsMapper;
    private final PetMapper petMapper;
    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final WeightRecordMapper weightRecordMapper;
    private final NotificationService notificationService;

    private static final Map<Integer, String> TYPE_NAMES = Map.of(
            1, "打卡", 2, "健康", 3, "社交", 4, "成长"
    );

    public List<AchievementVO> getUserAchievements(Long userId) {
        List<Achievement> allAchievements = achievementMapper.selectList(
                new LambdaQueryWrapper<Achievement>()
                        .eq(Achievement::getIsEnabled, 1)
                        .orderByAsc(Achievement::getSortOrder));

        Set<Long> unlockedIds = userAchievementMapper.selectUnlockedAchievementIds(userId)
                .stream().collect(Collectors.toSet());

        Map<Long, UserAchievement> userAchievementMap = userAchievementMapper.selectList(
                new LambdaQueryWrapper<UserAchievement>().eq(UserAchievement::getUserId, userId))
                .stream().collect(Collectors.toMap(UserAchievement::getAchievementId, ua -> ua, (a, b) -> a));

        List<AchievementVO> result = new ArrayList<>();
        for (Achievement a : allAchievements) {
            AchievementVO vo = convertToVO(a, userId, unlockedIds, userAchievementMap);
            result.add(vo);
        }
        return result;
    }

    public AchievementVO getAchievementDetail(Long userId, Long achievementId) {
        Achievement achievement = achievementMapper.selectById(achievementId);
        if (achievement == null) {
            return null;
        }

        Set<Long> unlockedIds = userAchievementMapper.selectUnlockedAchievementIds(userId)
                .stream().collect(Collectors.toSet());

        UserAchievement ua = userAchievementMapper.selectByUserIdAndAchievementId(userId, achievementId);
        Map<Long, UserAchievement> userAchievementMap = ua != null
                ? Map.of(achievementId, ua)
                : Map.of();

        return convertToVO(achievement, userId, unlockedIds, userAchievementMap);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndUnlock(Long userId, String conditionType) {
        List<Achievement> achievements = achievementMapper.selectList(
                new LambdaQueryWrapper<Achievement>()
                        .eq(Achievement::getIsEnabled, 1)
                        .eq(Achievement::getConditionType, conditionType)
                        .orderByAsc(Achievement::getConditionValue));

        Set<Long> unlockedIds = userAchievementMapper.selectUnlockedAchievementIds(userId)
                .stream().collect(Collectors.toSet());

        boolean anyUnlocked = false;
        for (Achievement a : achievements) {
            if (unlockedIds.contains(a.getId())) continue;

            int currentProgress = getCurrentProgress(userId, a.getConditionType());
            if (currentProgress >= a.getConditionValue()) {
                unlockAchievement(userId, a);
                anyUnlocked = true;
            }
        }
        return anyUnlocked;
    }

    @Transactional(rollbackFor = Exception.class)
    public UserAchievement claimAchievement(Long userId, Long achievementId) {
        UserAchievement ua = userAchievementMapper.selectByUserIdAndAchievementId(userId, achievementId);
        if (ua == null) {
            throw new com.pettrail.pettrailbackend.exception.NotFoundException("成就记录不存在");
        }
        if (ua.getStatus() != 2) {
            throw new com.pettrail.pettrailbackend.exception.BusinessException("成就未完成或已领取");
        }
        ua.setStatus(3);
        userAchievementMapper.updateById(ua);
        return ua;
    }

    public int getUnlockedCount(Long userId) {
        return userAchievementMapper.selectUnlockedAchievementIds(userId).size();
    }

    public int getTotalCount() {
        Long count = achievementMapper.selectCount(
                new LambdaQueryWrapper<Achievement>().eq(Achievement::getIsEnabled, 1));
        return count != null ? count.intValue() : 0;
    }

    private void unlockAchievement(Long userId, Achievement achievement) {
        UserAchievement existing = userAchievementMapper.selectByUserIdAndAchievementId(userId, achievement.getId());
        if (existing != null) {
            if (existing.getStatus() < 2) {
                existing.setStatus(2);
                existing.setUnlockedAt(LocalDateTime.now());
                userAchievementMapper.updateById(existing);
            }
            return;
        }

        UserAchievement ua = new UserAchievement();
        ua.setUserId(userId);
        ua.setAchievementId(achievement.getId());
        ua.setStatus(2);
        ua.setUnlockedAt(LocalDateTime.now());
        ua.setCreatedAt(LocalDateTime.now());
        userAchievementMapper.insert(ua);

        notificationService.sendCheckinAchievementNotification(userId, achievement.getName());
        log.info("成就解锁: userId={}, achievementId={}, name={}", userId, achievement.getId(), achievement.getName());
    }

    int getCurrentProgress(Long userId, String conditionType) {
        return switch (conditionType) {
            case "checkin_count" -> getCheckinCount(userId);
            case "checkin_streak" -> getCheckinStreak(userId);
            case "health_record_count" -> getHealthRecordCount(userId);
            case "post_count" -> getPostCount(userId);
            case "like_received" -> getLikeReceivedCount(userId);
            default -> 0;
        };
    }

    private int getCheckinCount(Long userId) {
        List<CheckinStats> stats = checkinStatsMapper.selectList(
                new LambdaQueryWrapper<CheckinStats>().eq(CheckinStats::getUserId, userId));
        return stats.stream().mapToInt(s -> s.getTotalCount() != null ? s.getTotalCount() : 0).sum();
    }

    private int getCheckinStreak(Long userId) {
        List<CheckinStats> stats = checkinStatsMapper.selectList(
                new LambdaQueryWrapper<CheckinStats>().eq(CheckinStats::getUserId, userId));
        return stats.stream()
                .mapToInt(s -> s.getCurrentStreak() != null ? s.getCurrentStreak() : 0)
                .max().orElse(0);
    }

    private int getHealthRecordCount(Long userId) {
        List<com.pettrail.pettrailbackend.entity.Pet> pets = petMapper.selectList(
                new LambdaQueryWrapper<com.pettrail.pettrailbackend.entity.Pet>()
                        .eq(com.pettrail.pettrailbackend.entity.Pet::getUserId, userId));
        int count = 0;
        for (com.pettrail.pettrailbackend.entity.Pet pet : pets) {
            Long petCount = weightRecordMapper.selectCount(
                    new LambdaQueryWrapper<com.pettrail.pettrailbackend.entity.WeightRecord>()
                            .eq(com.pettrail.pettrailbackend.entity.WeightRecord::getPetId, pet.getId()));
            count += petCount != null ? petCount.intValue() : 0;
        }
        return count;
    }

    private int getPostCount(Long userId) {
        Long count = postMapper.selectCount(
                new LambdaQueryWrapper<com.pettrail.pettrailbackend.entity.Post>()
                        .eq(com.pettrail.pettrailbackend.entity.Post::getUserId, userId)
                        .eq(com.pettrail.pettrailbackend.entity.Post::getDeleted, 0));
        return count != null ? count.intValue() : 0;
    }

    private int getLikeReceivedCount(Long userId) {
        List<com.pettrail.pettrailbackend.entity.Post> posts = postMapper.selectList(
                new LambdaQueryWrapper<com.pettrail.pettrailbackend.entity.Post>()
                        .eq(com.pettrail.pettrailbackend.entity.Post::getUserId, userId)
                        .eq(com.pettrail.pettrailbackend.entity.Post::getDeleted, 0));
        return posts.stream()
                .mapToInt(p -> p.getLikeCount() != null ? p.getLikeCount() : 0)
                .sum();
    }

    private AchievementVO convertToVO(Achievement a, Long userId, Set<Long> unlockedIds,
                                       Map<Long, UserAchievement> userAchievementMap) {
        AchievementVO vo = new AchievementVO();
        vo.setId(a.getId());
        vo.setName(a.getName());
        vo.setDescription(a.getDescription());
        vo.setIcon(a.getIcon());
        vo.setType(a.getType());
        vo.setTypeName(TYPE_NAMES.getOrDefault(a.getType(), "其他"));
        vo.setConditionType(a.getConditionType());
        vo.setConditionValue(a.getConditionValue());
        vo.setSortOrder(a.getSortOrder());

        boolean unlocked = unlockedIds.contains(a.getId());
        vo.setUnlocked(unlocked);

        UserAchievement ua = userAchievementMap.get(a.getId());
        if (ua != null) {
            vo.setUnlockedAt(ua.getUnlockedAt());
            vo.setStatus(ua.getStatus());
        } else {
            vo.setStatus(1);
        }

        vo.setCurrentProgress(getCurrentProgress(userId, a.getConditionType()));
        return vo;
    }
}

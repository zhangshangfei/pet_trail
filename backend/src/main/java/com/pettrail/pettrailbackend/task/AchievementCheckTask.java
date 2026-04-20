package com.pettrail.pettrailbackend.task;

import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import com.pettrail.pettrailbackend.service.AchievementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementCheckTask {

    private final AchievementService achievementService;
    private final UserMapper userMapper;

    private static final List<String> CONDITION_TYPES = List.of(
            "checkin_count", "checkin_streak", "health_record_count",
            "post_count", "like_received"
    );

    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyAchievementCheck() {
        log.info("开始每日成就检查任务");
        int totalChecked = 0;
        int totalUnlocked = 0;

        List<User> activeUsers = userMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getStatus, 1));

        for (User user : activeUsers) {
            try {
                for (String conditionType : CONDITION_TYPES) {
                    boolean unlocked = achievementService.checkAndUnlock(user.getId(), conditionType);
                    if (unlocked) totalUnlocked++;
                }
                totalChecked++;
            } catch (Exception e) {
                log.error("成就检查失败: userId={}, error={}", user.getId(), e.getMessage());
            }
        }

        log.info("每日成就检查完成: 检查用户数={}, 新解锁数={}", totalChecked, totalUnlocked);
    }
}

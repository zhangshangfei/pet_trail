package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.CheckinReminder;
import com.pettrail.pettrailbackend.mapper.CheckinReminderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinReminderService {

    private final CheckinReminderMapper checkinReminderMapper;
    private final NotificationService notificationService;

    public List<CheckinReminder> getUserReminders(Long userId) {
        LambdaQueryWrapper<CheckinReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinReminder::getUserId, userId);
        wrapper.orderByAsc(CheckinReminder::getRemindTime);
        return checkinReminderMapper.selectList(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public CheckinReminder createReminder(Long userId, Long itemId, LocalTime remindTime) {
        CheckinReminder reminder = new CheckinReminder();
        reminder.setUserId(userId);
        reminder.setItemId(itemId);
        reminder.setRemindTime(remindTime);
        reminder.setIsEnabled(true);
        checkinReminderMapper.insert(reminder);
        return reminder;
    }

    @Transactional(rollbackFor = Exception.class)
    public CheckinReminder updateReminder(Long id, Long userId, Long itemId, LocalTime remindTime, Boolean isEnabled) {
        CheckinReminder reminder = checkinReminderMapper.selectById(id);
        if (reminder == null || !reminder.getUserId().equals(userId)) {
            return null;
        }
        if (itemId != null) reminder.setItemId(itemId);
        if (remindTime != null) reminder.setRemindTime(remindTime);
        if (isEnabled != null) reminder.setIsEnabled(isEnabled);
        checkinReminderMapper.updateById(reminder);
        return reminder;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteReminder(Long id, Long userId) {
        CheckinReminder reminder = checkinReminderMapper.selectById(id);
        if (reminder != null && reminder.getUserId().equals(userId)) {
            checkinReminderMapper.deleteById(id);
        }
    }

    public void sendCheckinReminders() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        List<CheckinReminder> reminders = checkinReminderMapper.selectEnabledByUserId(null);
        LambdaQueryWrapper<CheckinReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinReminder::getIsEnabled, true);
        wrapper.eq(CheckinReminder::getRemindTime, now);
        reminders = checkinReminderMapper.selectList(wrapper);

        for (CheckinReminder reminder : reminders) {
            try {
                String content = reminder.getItemId() != null
                    ? "该给你的宠物打卡啦！别忘了今天的打卡任务 🐾"
                    : "该给你的宠物打卡啦！别忘了今天的打卡任务 🐾";
                notificationService.createNotification(
                    reminder.getUserId(), 0L, "system", null, content);
                log.info("发送打卡提醒: userId={}", reminder.getUserId());
            } catch (Exception e) {
                log.warn("发送打卡提醒失败: userId={}, error={}", reminder.getUserId(), e.getMessage());
            }
        }
    }
}

package com.pettrail.pettrailbackend.service;

import com.alibaba.fastjson.JSONObject;
import com.pettrail.pettrailbackend.dto.ReminderMessage;
import com.pettrail.pettrailbackend.entity.CheckinReminder;
import com.pettrail.pettrailbackend.mapper.CheckinReminderMapper;
import com.pettrail.pettrailbackend.mq.ReminderMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinReminderService {

    private final CheckinReminderMapper checkinReminderMapper;
    private final ReminderMessageProducer reminderMessageProducer;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public List<CheckinReminder> getUserReminders(Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckinReminder> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
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

        scheduleCheckinReminder(reminder);
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

        if (reminder.getIsEnabled()) {
            scheduleCheckinReminder(reminder);
        } else {
            reminderMessageProducer.cancelCheckinReminder(id);
        }
        return reminder;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteReminder(Long id, Long userId) {
        CheckinReminder reminder = checkinReminderMapper.selectById(id);
        if (reminder != null && reminder.getUserId().equals(userId)) {
            checkinReminderMapper.deleteById(id);
            reminderMessageProducer.cancelCheckinReminder(id);
        }
    }

    private void scheduleCheckinReminder(CheckinReminder reminder) {
        try {
            long delay = calculateNextDelay(reminder.getRemindTime());
            if (delay > 0) {
                JSONObject payload = new JSONObject();
                payload.put("itemId", reminder.getItemId());
                payload.put("remindTime", reminder.getRemindTime().format(TIME_FMT));

                ReminderMessage msg = ReminderMessage.checkin(
                    reminder.getId(), reminder.getUserId(), 0L, payload.toJSONString());
                reminderMessageProducer.sendDelayedCheckinReminder(msg, delay);
            }
        } catch (Exception e) {
            log.warn("调度打卡提醒失败: reminderId={}, error={}", reminder.getId(), e.getMessage());
        }
    }

    private long calculateNextDelay(LocalTime remindTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextTarget = now.toLocalDate().atTime(remindTime);

        if (!nextTarget.isAfter(now)) {
            nextTarget = nextTarget.plusDays(1);
        }

        return java.time.Duration.between(now, nextTarget).toMillis();
    }
}

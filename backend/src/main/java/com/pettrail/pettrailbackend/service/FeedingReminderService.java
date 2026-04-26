package com.pettrail.pettrailbackend.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pettrail.pettrailbackend.dto.ReminderMessage;
import com.pettrail.pettrailbackend.entity.FeedingReminder;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.FeedingReminderMapper;
import com.pettrail.pettrailbackend.mq.ReminderMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedingReminderService {

    private final FeedingReminderMapper feedingReminderMapper;
    private final ReminderMessageProducer reminderMessageProducer;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public List<FeedingReminder> getUserReminders(Long userId) {
        return feedingReminderMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FeedingReminder>()
                .eq(FeedingReminder::getUserId, userId)
                .orderByAsc(FeedingReminder::getTime)
        );
    }

    public List<FeedingReminder> getPetReminders(Long userId, Long petId) {
        return feedingReminderMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FeedingReminder>()
                .eq(FeedingReminder::getUserId, userId)
                .eq(FeedingReminder::getPetId, petId)
                .orderByAsc(FeedingReminder::getTime)
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public FeedingReminder createReminder(Long userId, Long petId, String mealType,
                                           String time, String repeat, String note) {
        FeedingReminder reminder = new FeedingReminder();
        reminder.setUserId(userId);
        reminder.setPetId(petId);
        reminder.setMealType(mealType);
        reminder.setTime(time);
        reminder.setRepeatType(repeat != null ? repeat : "daily");
        reminder.setNote(note);
        reminder.setEnabled(true);
        reminder.setCreatedAt(LocalDateTime.now());
        reminder.setUpdatedAt(LocalDateTime.now());
        feedingReminderMapper.insert(reminder);

        scheduleFeedingReminder(reminder);
        return reminder;
    }

    @Transactional(rollbackFor = Exception.class)
    public FeedingReminder updateReminder(Long userId, Long reminderId, String mealType,
                                           String time, String repeat, String note) {
        FeedingReminder reminder = feedingReminderMapper.selectById(reminderId);
        if (reminder == null || !reminder.getUserId().equals(userId)) {
            throw new BusinessException("提醒不存在或无权修改");
        }

        if (mealType != null) reminder.setMealType(mealType);
        if (time != null) reminder.setTime(time);
        if (repeat != null) reminder.setRepeatType(repeat);
        if (note != null) reminder.setNote(note);
        reminder.setUpdatedAt(LocalDateTime.now());
        feedingReminderMapper.updateById(reminder);

        if (reminder.getEnabled()) {
            scheduleFeedingReminder(reminder);
        }
        return reminder;
    }

    @Transactional(rollbackFor = Exception.class)
    public FeedingReminder toggleReminder(Long userId, Long reminderId) {
        FeedingReminder reminder = feedingReminderMapper.selectById(reminderId);
        if (reminder == null || !reminder.getUserId().equals(userId)) {
            throw new BusinessException("提醒不存在或无权操作");
        }
        reminder.setEnabled(!reminder.getEnabled());
        reminder.setUpdatedAt(LocalDateTime.now());
        feedingReminderMapper.updateById(reminder);

        if (reminder.getEnabled()) {
            scheduleFeedingReminder(reminder);
        } else {
            reminderMessageProducer.cancelFeedingReminder(reminderId);
        }
        return reminder;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteReminder(Long userId, Long reminderId) {
        FeedingReminder reminder = feedingReminderMapper.selectById(reminderId);
        if (reminder == null || !reminder.getUserId().equals(userId)) {
            throw new BusinessException("提醒不存在或无权删除");
        }
        feedingReminderMapper.deleteById(reminderId);
        reminderMessageProducer.cancelFeedingReminder(reminderId);
    }

    private void scheduleFeedingReminder(FeedingReminder reminder) {
        try {
            long delay = calculateNextDelay(reminder.getTime(), reminder.getRepeatType());
            if (delay > 0) {
                JSONObject payload = new JSONObject();
                payload.put("mealType", reminder.getMealType());
                payload.put("time", reminder.getTime());
                payload.put("repeatType", reminder.getRepeatType());
                payload.put("note", reminder.getNote());

                ReminderMessage msg = ReminderMessage.feeding(
                    reminder.getId(), reminder.getUserId(), reminder.getPetId(), payload.toJSONString());
                reminderMessageProducer.sendDelayedFeedingReminder(msg, delay);
            }
        } catch (Exception e) {
            log.warn("调度喂食提醒失败: reminderId={}, error={}", reminder.getId(), e.getMessage());
        }
    }

    private long calculateNextDelay(String timeStr, String repeatType) {
        try {
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextTarget = now.toLocalDate().atTime(hour, minute);

            if (!nextTarget.isAfter(now)) {
                nextTarget = nextTarget.plusDays(1);
            }

            if ("weekday".equals(repeatType) && isWeekend(nextTarget.toLocalDate())) {
                while (isWeekend(nextTarget.toLocalDate())) {
                    nextTarget = nextTarget.plusDays(1);
                }
            } else if ("weekend".equals(repeatType) && !isWeekend(nextTarget.toLocalDate())) {
                while (!isWeekend(nextTarget.toLocalDate())) {
                    nextTarget = nextTarget.plusDays(1);
                }
            }

            return java.time.Duration.between(now, nextTarget).toMillis();
        } catch (Exception e) {
            return 24 * 60 * 60 * 1000L;
        }
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }
}

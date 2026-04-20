package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.FeedingReminder;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.FeedingReminderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedingReminderService {

    private final FeedingReminderMapper feedingReminderMapper;

    public List<FeedingReminder> getUserReminders(Long userId) {
        return feedingReminderMapper.selectList(
            new LambdaQueryWrapper<FeedingReminder>()
                .eq(FeedingReminder::getUserId, userId)
                .orderByAsc(FeedingReminder::getTime)
        );
    }

    public List<FeedingReminder> getPetReminders(Long userId, Long petId) {
        return feedingReminderMapper.selectList(
            new LambdaQueryWrapper<FeedingReminder>()
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
        return reminder;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteReminder(Long userId, Long reminderId) {
        FeedingReminder reminder = feedingReminderMapper.selectById(reminderId);
        if (reminder == null || !reminder.getUserId().equals(userId)) {
            throw new BusinessException("提醒不存在或无权删除");
        }
        feedingReminderMapper.deleteById(reminderId);
    }
}

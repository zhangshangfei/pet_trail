package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.CheckinItem;
import com.pettrail.pettrailbackend.entity.CheckinReminder;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.CheckinItemMapper;
import com.pettrail.pettrailbackend.mapper.CheckinReminderMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class CheckinReminderService {

    private final CheckinReminderMapper checkinReminderMapper;
    private final CheckinItemMapper checkinItemMapper;
    private final NotificationService notificationService;
    private final WxSubscribeMessageService wxSubscribeMessageService;
    private final UserMapper userMapper;

    public CheckinReminderService(CheckinReminderMapper checkinReminderMapper,
                                  CheckinItemMapper checkinItemMapper,
                                  NotificationService notificationService,
                                  WxSubscribeMessageService wxSubscribeMessageService,
                                  UserMapper userMapper) {
        this.checkinReminderMapper = checkinReminderMapper;
        this.checkinItemMapper = checkinItemMapper;
        this.notificationService = notificationService;
        this.wxSubscribeMessageService = wxSubscribeMessageService;
        this.userMapper = userMapper;
    }

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
        LambdaQueryWrapper<CheckinReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinReminder::getIsEnabled, true);
        wrapper.eq(CheckinReminder::getRemindTime, now);
        List<CheckinReminder> reminders = checkinReminderMapper.selectList(wrapper);

        for (CheckinReminder reminder : reminders) {
            try {
                String itemName = "全部打卡项";
                if (reminder.getItemId() != null) {
                    CheckinItem item = checkinItemMapper.selectById(reminder.getItemId());
                    if (item != null) {
                        itemName = item.getName();
                    }
                }
                String content = "该给你的宠物打卡啦！别忘了「" + itemName + "」任务 🐾";
                notificationService.createNotification(
                    reminder.getUserId(), 0L, "system", null, content);
                log.info("发送打卡提醒(站内信): userId={}", reminder.getUserId());

                sendWxSubscribeMessage(reminder, itemName);
            } catch (Exception e) {
                log.warn("发送打卡提醒失败: userId={}, error={}", reminder.getUserId(), e.getMessage());
            }
        }
    }

    private void sendWxSubscribeMessage(CheckinReminder reminder, String itemName) {
        try {
            User user = userMapper.selectById(reminder.getUserId());
            if (user == null || user.getOpenid() == null || user.getOpenid().isEmpty()) {
                log.debug("用户无openid，跳过微信订阅消息: userId={}", reminder.getUserId());
                return;
            }

            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年M月d日"));
            String timeStr = reminder.getRemindTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            String remindTimeStr = today + " " + timeStr;

            boolean sent = wxSubscribeMessageService.sendCheckinReminder(
                user.getOpenid(), itemName, remindTimeStr, "pages/checkin/index");
            if (sent) {
                log.info("发送打卡提醒(微信订阅消息): userId={}, openid={}", reminder.getUserId(), user.getOpenid());
            }
        } catch (Exception e) {
            log.warn("发送微信订阅消息异常: userId={}, error={}", reminder.getUserId(), e.getMessage());
        }
    }
}

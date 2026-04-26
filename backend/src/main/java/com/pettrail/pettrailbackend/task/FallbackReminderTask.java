package com.pettrail.pettrailbackend.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.CheckinReminder;
import com.pettrail.pettrailbackend.entity.FeedingReminder;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.CheckinItemMapper;
import com.pettrail.pettrailbackend.mapper.CheckinReminderMapper;
import com.pettrail.pettrailbackend.mapper.FeedingReminderMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import com.pettrail.pettrailbackend.service.NotificationService;
import com.pettrail.pettrailbackend.service.WxSubscribeMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "false", matchIfMissing = true)
public class FallbackReminderTask {

    private final FeedingReminderMapper feedingReminderMapper;
    private final CheckinReminderMapper checkinReminderMapper;
    private final CheckinItemMapper checkinItemMapper;
    private final NotificationService notificationService;
    private final WxSubscribeMessageService wxSubscribeMessageService;
    private final UserMapper userMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy年M月d日");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final Map<String, String> MEAL_NAME_MAP = Map.of(
        "breakfast", "早餐", "lunch", "午餐", "dinner", "晚餐", "snack", "加餐"
    );

    @Scheduled(cron = "0 */1 * * * ?")
    public void sendFeedingReminders() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        String currentTime = now.format(TIME_FMT);
        LocalDate today = LocalDate.now();
        boolean isWeekday = today.getDayOfWeek() != DayOfWeek.SATURDAY && today.getDayOfWeek() != DayOfWeek.SUNDAY;
        boolean isWeekend = today.getDayOfWeek() == DayOfWeek.SATURDAY || today.getDayOfWeek() == DayOfWeek.SUNDAY;

        List<FeedingReminder> reminders = feedingReminderMapper.selectList(
            new LambdaQueryWrapper<FeedingReminder>()
                .eq(FeedingReminder::getEnabled, true)
                .eq(FeedingReminder::getTime, currentTime));

        for (FeedingReminder reminder : reminders) {
            try {
                if (!shouldSendToday(reminder.getRepeatType(), isWeekday, isWeekend)) continue;

                String mealName = MEAL_NAME_MAP.getOrDefault(reminder.getMealType(), "喂食");
                String content = "该给宠物喂食啦！「" + mealName + "」时间到了 🍽️";
                if (reminder.getNote() != null && !reminder.getNote().isEmpty()) {
                    content += " 备注：" + reminder.getNote();
                }

                notificationService.createNotification(
                    reminder.getUserId(), reminder.getPetId(), "feeding", null, content);

                sendWxFeedingReminder(reminder, mealName);
            } catch (Exception e) {
                log.warn("发送喂食提醒失败: userId={}, error={}", reminder.getUserId(), e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void sendCheckinReminders() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        List<CheckinReminder> reminders = checkinReminderMapper.selectList(
            new LambdaQueryWrapper<CheckinReminder>()
                .eq(CheckinReminder::getIsEnabled, true)
                .eq(CheckinReminder::getRemindTime, now));

        for (CheckinReminder reminder : reminders) {
            try {
                String itemName = "全部打卡项";
                if (reminder.getItemId() != null) {
                    var item = checkinItemMapper.selectById(reminder.getItemId());
                    if (item != null) itemName = item.getName();
                }

                String content = "该给你的宠物打卡啦！别忘了「" + itemName + "」任务 🐾";
                notificationService.createNotification(
                    reminder.getUserId(), 0L, "system", null, content);

                sendWxCheckinReminder(reminder, itemName);
            } catch (Exception e) {
                log.warn("发送打卡提醒失败: userId={}, error={}", reminder.getUserId(), e.getMessage());
            }
        }
    }

    private void sendWxFeedingReminder(FeedingReminder reminder, String mealName) {
        try {
            User user = userMapper.selectById(reminder.getUserId());
            if (user == null || user.getOpenid() == null || user.getOpenid().isEmpty()) return;
            String today = LocalDate.now().format(DATE_FMT);
            String feedTime = today + " " + reminder.getTime();
            wxSubscribeMessageService.sendFeedingReminder(
                reminder.getUserId(), user.getOpenid(), mealName, feedTime, reminder.getNote(),
                "pages/me/feeding-reminder");
        } catch (Exception e) {
            log.warn("发送喂食微信订阅消息异常: userId={}, error={}", reminder.getUserId(), e.getMessage());
        }
    }

    private void sendWxCheckinReminder(CheckinReminder reminder, String itemName) {
        try {
            User user = userMapper.selectById(reminder.getUserId());
            if (user == null || user.getOpenid() == null || user.getOpenid().isEmpty()) return;
            String today = LocalDate.now().format(DATE_FMT);
            String timeStr = reminder.getRemindTime().format(TIME_FMT);
            String remindTimeStr = today + " " + timeStr;
            wxSubscribeMessageService.sendCheckinReminder(
                reminder.getUserId(), user.getOpenid(), itemName, remindTimeStr, "pages/checkin/index");
        } catch (Exception e) {
            log.warn("发送打卡微信订阅消息异常: userId={}, error={}", reminder.getUserId(), e.getMessage());
        }
    }

    private boolean shouldSendToday(String repeatType, boolean isWeekday, boolean isWeekend) {
        if (repeatType == null || "daily".equals(repeatType)) return true;
        if ("weekday".equals(repeatType)) return isWeekday;
        if ("weekend".equals(repeatType)) return isWeekend;
        return true;
    }
}

package com.pettrail.pettrailbackend.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pettrail.pettrailbackend.config.RabbitMQConfig;
import com.pettrail.pettrailbackend.dto.ReminderMessage;
import com.pettrail.pettrailbackend.entity.CheckinItem;
import com.pettrail.pettrailbackend.entity.CheckinReminder;
import com.pettrail.pettrailbackend.entity.FeedingReminder;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.CheckinItemMapper;
import com.pettrail.pettrailbackend.mapper.CheckinReminderMapper;
import com.pettrail.pettrailbackend.mapper.FeedingReminderMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import com.pettrail.pettrailbackend.service.NotificationService;
import com.pettrail.pettrailbackend.service.WxSubscribeMessageService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class ReminderMessageConsumer {

    private final FeedingReminderMapper feedingReminderMapper;
    private final CheckinReminderMapper checkinReminderMapper;
    private final CheckinItemMapper checkinItemMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final WxSubscribeMessageService wxSubscribeMessageService;
    private final ReminderMessageProducer reminderMessageProducer;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy年M月d日");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final Map<String, String> MEAL_NAME_MAP = Map.of(
        "breakfast", "早餐", "lunch", "午餐", "dinner", "晚餐", "snack", "加餐"
    );

    @RabbitListener(queues = RabbitMQConfig.FEEDING_QUEUE_NAME)
    public void onFeedingReminder(ReminderMessage msg, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            processFeedingReminder(msg);
            channel.basicAck(deliveryTag, false);

            scheduleNextFeedingReminder(msg);
        } catch (Exception e) {
            log.error("消费喂食提醒消息失败: reminderId={}, error={}", msg.getReminderId(), e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.CHECKIN_QUEUE_NAME)
    public void onCheckinReminder(ReminderMessage msg, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            processCheckinReminder(msg);
            channel.basicAck(deliveryTag, false);

            scheduleNextCheckinReminder(msg);
        } catch (Exception e) {
            log.error("消费打卡提醒消息失败: reminderId={}, error={}", msg.getReminderId(), e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void processFeedingReminder(ReminderMessage msg) {
        FeedingReminder reminder = feedingReminderMapper.selectById(msg.getReminderId());
        if (reminder == null || !reminder.getEnabled()) {
            log.debug("喂食提醒已删除或已禁用，跳过: reminderId={}", msg.getReminderId());
            return;
        }

        LocalDate today = LocalDate.now();
        if (!shouldSendToday(reminder.getRepeatType(), today)) {
            log.debug("喂食提醒今日不发送: reminderId={}, repeatType={}", msg.getReminderId(), reminder.getRepeatType());
            return;
        }

        String mealName = MEAL_NAME_MAP.getOrDefault(reminder.getMealType(), "喂食");
        String content = "该给宠物喂食啦！「" + mealName + "」时间到了 🍽️";
        if (reminder.getNote() != null && !reminder.getNote().isEmpty()) {
            content += " 备注：" + reminder.getNote();
        }

        notificationService.createNotification(
            reminder.getUserId(), reminder.getPetId(), "feeding", null, content);
        log.info("发送喂食提醒(站内信): userId={}, petId={}, meal={}", reminder.getUserId(), reminder.getPetId(), mealName);

        sendWxFeedingReminder(reminder, mealName);
    }

    private void processCheckinReminder(ReminderMessage msg) {
        CheckinReminder reminder = checkinReminderMapper.selectById(msg.getReminderId());
        if (reminder == null || !reminder.getIsEnabled()) {
            log.debug("打卡提醒已删除或已禁用，跳过: reminderId={}", msg.getReminderId());
            return;
        }

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

        sendWxCheckinReminder(reminder, itemName);
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

    private void scheduleNextFeedingReminder(ReminderMessage msg) {
        try {
            FeedingReminder reminder = feedingReminderMapper.selectById(msg.getReminderId());
            if (reminder == null || !reminder.getEnabled()) return;

            long delay = calculateNextDelay(reminder.getTime(), reminder.getRepeatType());
            if (delay > 0) {
                reminderMessageProducer.sendDelayedFeedingReminder(msg, delay);
            }
        } catch (Exception e) {
            log.warn("调度下次喂食提醒失败: reminderId={}, error={}", msg.getReminderId(), e.getMessage());
        }
    }

    private void scheduleNextCheckinReminder(ReminderMessage msg) {
        try {
            CheckinReminder reminder = checkinReminderMapper.selectById(msg.getReminderId());
            if (reminder == null || !reminder.getIsEnabled()) return;

            long delay = calculateNextDelayForTime(reminder.getRemindTime());
            if (delay > 0) {
                reminderMessageProducer.sendDelayedCheckinReminder(msg, delay);
            }
        } catch (Exception e) {
            log.warn("调度下次打卡提醒失败: reminderId={}, error={}", msg.getReminderId(), e.getMessage());
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
            log.warn("计算下次延迟时间异常: timeStr={}, error={}", timeStr, e.getMessage());
            return 24 * 60 * 60 * 1000L;
        }
    }

    private long calculateNextDelayForTime(LocalTime remindTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextTarget = now.toLocalDate().atTime(remindTime);

        if (!nextTarget.isAfter(now)) {
            nextTarget = nextTarget.plusDays(1);
        }

        return java.time.Duration.between(now, nextTarget).toMillis();
    }

    private boolean shouldSendToday(String repeatType, LocalDate date) {
        if (repeatType == null || "daily".equals(repeatType)) return true;
        if ("weekday".equals(repeatType)) return !isWeekend(date);
        if ("weekend".equals(repeatType)) return isWeekend(date);
        return true;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }
}

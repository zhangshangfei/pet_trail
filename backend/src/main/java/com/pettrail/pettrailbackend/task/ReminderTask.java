package com.pettrail.pettrailbackend.task;

import com.pettrail.pettrailbackend.entity.ParasiteReminder;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.entity.VaccineReminder;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import com.pettrail.pettrailbackend.service.NotificationService;
import com.pettrail.pettrailbackend.service.ReminderService;
import com.pettrail.pettrailbackend.service.WxSubscribeMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderTask {

    private final ReminderService reminderService;
    private final NotificationService notificationService;
    private final WxSubscribeMessageService wxSubscribeMessageService;
    private final UserMapper userMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy年M月d日");

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkDueReminders() {
        log.info("开始检查即将到期的疫苗/驱虫提醒");

        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        List<VaccineReminder> vaccineReminders =
            reminderService.getDueVaccineReminders(null, today, nextWeek);

        for (VaccineReminder reminder : vaccineReminders) {
            if (reminder.getIsNotified() == 0) {
                try {
                    notificationService.sendVaccineReminder(
                        reminder.getUserId(),
                        reminder.getPetId(),
                        reminder.getVaccineName(),
                        reminder.getNextDate()
                    );
                    sendWxVaccineReminder(reminder);
                    reminder.setIsNotified(1);
                } catch (Exception e) {
                    log.error("发送疫苗提醒失败：userId={}, petId={}",
                        reminder.getUserId(), reminder.getPetId(), e);
                }
            }
        }

        List<ParasiteReminder> parasiteReminders =
            reminderService.getDueParasiteReminders(null, today, nextWeek);

        for (ParasiteReminder reminder : parasiteReminders) {
            if (reminder.getIsNotified() == 0) {
                try {
                    notificationService.sendParasiteReminder(
                        reminder.getUserId(),
                        reminder.getPetId(),
                        reminder.getType(),
                        reminder.getNextDate()
                    );
                    sendWxParasiteReminder(reminder);
                    reminder.setIsNotified(1);
                } catch (Exception e) {
                    log.error("发送驱虫提醒失败：userId={}, petId={}",
                        reminder.getUserId(), reminder.getPetId(), e);
                }
            }
        }

        log.info("疫苗/驱虫提醒检查完成");
    }

    private void sendWxVaccineReminder(VaccineReminder reminder) {
        try {
            User user = userMapper.selectById(reminder.getUserId());
            if (user == null || user.getOpenid() == null || user.getOpenid().isEmpty()) return;

            String vaccineName = reminder.getVaccineName() != null ? reminder.getVaccineName() : "疫苗";
            String nextDate = reminder.getNextDate() != null ? reminder.getNextDate().format(DATE_FMT) : "";
            wxSubscribeMessageService.sendVaccineReminder(
                reminder.getUserId(), user.getOpenid(), vaccineName, nextDate, "pages/health/index");
        } catch (Exception e) {
            log.warn("发送疫苗微信订阅消息异常: userId={}, error={}", reminder.getUserId(), e.getMessage());
        }
    }

    private void sendWxParasiteReminder(ParasiteReminder reminder) {
        try {
            User user = userMapper.selectById(reminder.getUserId());
            if (user == null || user.getOpenid() == null || user.getOpenid().isEmpty()) return;

            String typeMap = reminder.getType() == 1 ? "体内驱虫" : reminder.getType() == 2 ? "体外驱虫" : "内外同驱";
            String nextDate = reminder.getNextDate() != null ? reminder.getNextDate().format(DATE_FMT) : "";
            wxSubscribeMessageService.sendParasiteReminder(
                reminder.getUserId(), user.getOpenid(), typeMap, nextDate, "pages/health/index");
        } catch (Exception e) {
            log.warn("发送驱虫微信订阅消息异常: userId={}, error={}", reminder.getUserId(), e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanExpiredReminders() {
        log.info("开始清理过期提醒");
        LocalDate expiredDate = LocalDate.now().minusDays(30);
        reminderService.cleanExpiredReminders(expiredDate);
    }
}

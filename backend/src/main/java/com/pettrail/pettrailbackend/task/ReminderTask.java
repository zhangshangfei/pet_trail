package com.pettrail.pettrailbackend.task;

import com.pettrail.pettrailbackend.entity.ParasiteReminder;
import com.pettrail.pettrailbackend.entity.VaccineReminder;
import com.pettrail.pettrailbackend.service.CheckinReminderService;
import com.pettrail.pettrailbackend.service.NotificationService;
import com.pettrail.pettrailbackend.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 提醒定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderTask {

    private final ReminderService reminderService;
    private final NotificationService notificationService;
    private final CheckinReminderService checkinReminderService;

    /**
     * 每日早上 9 点检查即将到期的提醒
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkDueReminders() {
        log.info("开始检查即将到期的提醒");
        
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        // 1. 查询即将到期的疫苗提醒
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
                    reminder.setIsNotified(1);
                } catch (Exception e) {
                    log.error("发送疫苗提醒失败：userId={}, petId={}", 
                        reminder.getUserId(), reminder.getPetId(), e);
                }
            }
        }

        // 2. 查询即将到期的驱虫提醒
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
                    reminder.setIsNotified(1);
                } catch (Exception e) {
                    log.error("发送驱虫提醒失败：userId={}, petId={}", 
                        reminder.getUserId(), reminder.getPetId(), e);
                }
            }
        }

        log.info("提醒检查完成");
    }

    /**
     * 每日凌晨 1 点清理过期的提醒
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanExpiredReminders() {
        log.info("开始清理过期提醒");
        LocalDate expiredDate = LocalDate.now().minusDays(30);
        reminderService.cleanExpiredReminders(expiredDate);
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void sendCheckinReminders() {
        try {
            checkinReminderService.sendCheckinReminders();
        } catch (Exception e) {
            log.warn("发送打卡提醒失败: {}", e.getMessage());
        }
    }
}

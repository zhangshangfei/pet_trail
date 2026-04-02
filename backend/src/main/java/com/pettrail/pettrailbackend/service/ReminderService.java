package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.entity.ParasiteReminder;
import com.pettrail.pettrailbackend.entity.VaccineReminder;
import com.pettrail.pettrailbackend.mapper.ParasiteReminderMapper;
import com.pettrail.pettrailbackend.mapper.VaccineReminderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 提醒服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final VaccineReminderMapper vaccineReminderMapper;
    private final ParasiteReminderMapper parasiteReminderMapper;

    /**
     * 创建疫苗提醒
     */
    @Transactional(rollbackFor = Exception.class)
    public VaccineReminder createVaccineReminder(Long petId, Long userId, String vaccineName, 
                                                  Integer vaccineType, LocalDate nextDate, Integer reminderDays) {
        VaccineReminder reminder = new VaccineReminder();
        reminder.setPetId(petId);
        reminder.setUserId(userId);
        reminder.setVaccineName(vaccineName);
        reminder.setVaccineType(vaccineType);
        reminder.setNextDate(nextDate);
        reminder.setReminderDays(reminderDays != null ? reminderDays : 7);
        reminder.setStatus(1);
        reminder.setIsNotified(0);
        vaccineReminderMapper.insert(reminder);
        return reminder;
    }

    /**
     * 创建驱虫提醒
     */
    @Transactional(rollbackFor = Exception.class)
    public ParasiteReminder createParasiteReminder(Long petId, Long userId, Integer type, 
                                                    String productName, LocalDate nextDate, Integer intervalDays) {
        ParasiteReminder reminder = new ParasiteReminder();
        reminder.setPetId(petId);
        reminder.setUserId(userId);
        reminder.setType(type);
        reminder.setProductName(productName);
        reminder.setNextDate(nextDate);
        reminder.setIntervalDays(intervalDays != null ? intervalDays : 30);
        reminder.setStatus(1);
        reminder.setIsNotified(0);
        parasiteReminderMapper.insert(reminder);
        return reminder;
    }

    /**
     * 获取即将到期的疫苗提醒
     */
    public List<VaccineReminder> getDueVaccineReminders(Long userId, LocalDate startDate, LocalDate endDate) {
        return vaccineReminderMapper.selectDueReminders(userId, startDate, endDate);
    }

    /**
     * 获取即将到期的驱虫提醒
     */
    public List<ParasiteReminder> getDueParasiteReminders(Long userId, LocalDate startDate, LocalDate endDate) {
        return parasiteReminderMapper.selectDueReminders(userId, startDate, endDate);
    }

    /**
     * 标记疫苗提醒为已完成
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeVaccineReminder(Long reminderId) {
        VaccineReminder reminder = vaccineReminderMapper.selectById(reminderId);
        if (reminder != null) {
            reminder.setStatus(2); // 2-已接种
            vaccineReminderMapper.updateById(reminder);
        }
    }

    /**
     * 标记驱虫提醒为已完成
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeParasiteReminder(Long reminderId) {
        ParasiteReminder reminder = parasiteReminderMapper.selectById(reminderId);
        if (reminder != null) {
            reminder.setStatus(2); // 2-已完成
            parasiteReminderMapper.updateById(reminder);
        }
    }

    /**
     * 清理过期的提醒
     */
    public void cleanExpiredReminders(LocalDate expiredDate) {
        // TODO: 实现清理逻辑
        log.info("清理过期提醒：{}", expiredDate);
    }
}

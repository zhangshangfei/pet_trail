package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pettrail.pettrailbackend.entity.VaccineReminder;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.VaccineReminderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaccineReminderService extends ServiceImpl<VaccineReminderMapper, VaccineReminder> {

    public List<VaccineReminder> listByPetId(Long petId) {
        LambdaQueryWrapper<VaccineReminder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VaccineReminder::getPetId, petId);
        queryWrapper.orderByAsc(VaccineReminder::getNextDate);
        return this.list(queryWrapper);
    }

    public List<VaccineReminder> listUpcoming(Long petId) {
        LambdaQueryWrapper<VaccineReminder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VaccineReminder::getPetId, petId);
        queryWrapper.eq(VaccineReminder::getStatus, 0);
        queryWrapper.le(VaccineReminder::getNextDate, LocalDate.now().plusDays(7));
        queryWrapper.orderByAsc(VaccineReminder::getNextDate);
        return this.list(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public VaccineReminder createReminder(Long petId, Long userId, String vaccineName, LocalDate nextDate, String note) {
        VaccineReminder reminder = new VaccineReminder();
        reminder.setPetId(petId);
        reminder.setUserId(userId);
        reminder.setVaccineName(vaccineName);
        reminder.setNextDate(nextDate);
        reminder.setNote(note);
        reminder.setStatus(0);
        reminder.setCreatedAt(LocalDateTime.now());
        reminder.setUpdatedAt(LocalDateTime.now());

        this.save(reminder);
        log.info("创建疫苗提醒成功: id={}, petId={}, userId={}, vaccineName={}, note={}", reminder.getId(), petId, userId, vaccineName, note);
        return reminder;
    }

    public VaccineReminder getReminderById(Long reminderId) {
        VaccineReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new BusinessException(404, "提醒不存在");
        }
        return reminder;
    }

    @Transactional(rollbackFor = Exception.class)
    public VaccineReminder updateReminder(Long reminderId, String vaccineName, LocalDate nextDate, String note) {
        VaccineReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new BusinessException(404, "提醒不存在");
        }
        if (vaccineName != null) {
            reminder.setVaccineName(vaccineName);
        }
        if (nextDate != null) {
            reminder.setNextDate(nextDate);
        }
        if (note != null) {
            reminder.setNote(note);
        }
        reminder.setUpdatedAt(LocalDateTime.now());
        this.updateById(reminder);
        log.info("更新提醒信息成功: reminderId={}, vaccineName={}, nextDate={}, note={}", reminderId, vaccineName, nextDate, note);
        return reminder;
    }

    @Transactional(rollbackFor = Exception.class)
    public VaccineReminder updateStatus(Long reminderId, Integer status) {
        VaccineReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new BusinessException(404, "提醒不存在");
        }
        reminder.setStatus(status);
        reminder.setUpdatedAt(LocalDateTime.now());
        this.updateById(reminder);
        log.info("更新提醒状态成功: reminderId={}, status={}", reminderId, status);
        return reminder;
    }

    @Transactional(rollbackFor = Exception.class)
    public VaccineReminder updateNextDate(Long reminderId, LocalDate nextDate) {
        VaccineReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new BusinessException(404, "提醒不存在");
        }
        reminder.setNextDate(nextDate);
        reminder.setUpdatedAt(LocalDateTime.now());
        this.updateById(reminder);
        log.info("更新提醒日期成功: reminderId={}, nextDate={}", reminderId, nextDate);
        return reminder;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteReminder(Long reminderId) {
        VaccineReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new BusinessException(404, "提醒不存在");
        }
        this.removeById(reminderId);
        log.info("删除提醒成功: reminderId={}", reminderId);
    }
}

package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pettrail.pettrailbackend.entity.VaccineReminder;
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

    /**
     * 获取宠物的疫苗提醒列表
     */
    public List<VaccineReminder> listByPetId(Long petId) {
        LambdaQueryWrapper<VaccineReminder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VaccineReminder::getPetId, petId);
        queryWrapper.orderByAsc(VaccineReminder::getNextDate);
        return this.list(queryWrapper);
    }

    /**
     * 获取即将到期的疫苗提醒（未来7天内）
     */
    public List<VaccineReminder> listUpcoming(Long petId) {
        LambdaQueryWrapper<VaccineReminder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VaccineReminder::getPetId, petId);
        queryWrapper.eq(VaccineReminder::getStatus, 0); // 未接种
        queryWrapper.le(VaccineReminder::getNextDate, LocalDate.now().plusDays(7));
        queryWrapper.orderByAsc(VaccineReminder::getNextDate);
        return this.list(queryWrapper);
    }

    /**
     * 创建疫苗提醒
     */
    @Transactional(rollbackFor = Exception.class)
    public VaccineReminder createReminder(Long petId, String vaccineName, LocalDate nextDate) {
        VaccineReminder reminder = new VaccineReminder();
        reminder.setPetId(petId);
        reminder.setVaccineName(vaccineName);
        reminder.setNextDate(nextDate);
        reminder.setStatus(0); // 未接种
        reminder.setCreatedAt(LocalDateTime.now());
        reminder.setUpdatedAt(LocalDateTime.now());

        this.save(reminder);
        log.info("创建疫苗提醒成功: id={}, petId={}, vaccineName={}", reminder.getId(), petId, vaccineName);
        return reminder;
    }

    /**
     * 获取提醒详情
     */
    public VaccineReminder getReminderById(Long reminderId) {
        VaccineReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new RuntimeException("提醒不存在");
        }
        return reminder;
    }

    /**
     * 更新提醒信息（通用编辑接口）
     */
    @Transactional(rollbackFor = Exception.class)
    public VaccineReminder updateReminder(Long reminderId, String vaccineName, LocalDate nextDate) {
        VaccineReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new RuntimeException("提醒不存在");
        }
        if (vaccineName != null) {
            reminder.setVaccineName(vaccineName);
        }
        if (nextDate != null) {
            reminder.setNextDate(nextDate);
        }
        reminder.setUpdatedAt(LocalDateTime.now());
        this.updateById(reminder);
        log.info("更新提醒信息成功: reminderId={}, vaccineName={}, nextDate={}", reminderId, vaccineName, nextDate);
        return reminder;
    }

    /**
     * 更新提醒状态
     */
    @Transactional(rollbackFor = Exception.class)
    public VaccineReminder updateStatus(Long reminderId, Integer status) {
        VaccineReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new RuntimeException("提醒不存在");
        }
        reminder.setStatus(status);
        reminder.setUpdatedAt(LocalDateTime.now());
        this.updateById(reminder);
        log.info("更新提醒状态成功: reminderId={}, status={}", reminderId, status);
        return reminder;
    }

    /**
     * 更新提醒日期
     */
    @Transactional(rollbackFor = Exception.class)
    public VaccineReminder updateNextDate(Long reminderId, LocalDate nextDate) {
        VaccineReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new RuntimeException("提醒不存在");
        }
        reminder.setNextDate(nextDate);
        reminder.setUpdatedAt(LocalDateTime.now());
        this.updateById(reminder);
        log.info("更新提醒日期成功: reminderId={}, nextDate={}", reminderId, nextDate);
        return reminder;
    }

    /**
     * 删除提醒
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteReminder(Long reminderId) {
        VaccineReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new RuntimeException("提醒不存在");
        }
        this.removeById(reminderId);
        log.info("删除提醒成功: reminderId={}", reminderId);
    }
}

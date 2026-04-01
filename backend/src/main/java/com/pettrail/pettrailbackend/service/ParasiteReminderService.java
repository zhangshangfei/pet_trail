package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pettrail.pettrailbackend.entity.ParasiteReminder;
import com.pettrail.pettrailbackend.mapper.ParasiteReminderMapper;
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
public class ParasiteReminderService extends ServiceImpl<ParasiteReminderMapper, ParasiteReminder> {

    /**
     * 获取宠物的寄生虫提醒列表
     */
    public List<ParasiteReminder> listByPetId(Long petId) {
        LambdaQueryWrapper<ParasiteReminder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParasiteReminder::getPetId, petId);
        queryWrapper.orderByAsc(ParasiteReminder::getNextDate);
        return this.list(queryWrapper);
    }

    /**
     * 获取即将到期的寄生虫提醒（未来7天内）
     */
    public List<ParasiteReminder> listUpcoming(Long petId) {
        LambdaQueryWrapper<ParasiteReminder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParasiteReminder::getPetId, petId);
        queryWrapper.eq(ParasiteReminder::getStatus, 0); // 未完成
        queryWrapper.le(ParasiteReminder::getNextDate, LocalDate.now().plusDays(7));
        queryWrapper.orderByAsc(ParasiteReminder::getNextDate);
        return this.list(queryWrapper);
    }

    /**
     * 创建寄生虫提醒
     */
    @Transactional(rollbackFor = Exception.class)
    public ParasiteReminder createReminder(Long petId, String type, LocalDate nextDate) {
        ParasiteReminder reminder = new ParasiteReminder();
        reminder.setPetId(petId);
        reminder.setType(type);
        reminder.setNextDate(nextDate);
        reminder.setStatus(0); // 未完成
        reminder.setCreatedAt(LocalDateTime.now());
        reminder.setUpdatedAt(LocalDateTime.now());

        this.save(reminder);
        log.info("创建寄生虫提醒成功: id={}, petId={}, type={}", reminder.getId(), petId, type);
        return reminder;
    }

    /**
     * 获取提醒详情
     */
    public ParasiteReminder getReminderById(Long reminderId) {
        ParasiteReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new RuntimeException("提醒不存在");
        }
        return reminder;
    }

    /**
     * 更新提醒状态
     */
    @Transactional(rollbackFor = Exception.class)
    public ParasiteReminder updateStatus(Long reminderId, Integer status) {
        ParasiteReminder reminder = this.getById(reminderId);
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
    public ParasiteReminder updateNextDate(Long reminderId, LocalDate nextDate) {
        ParasiteReminder reminder = this.getById(reminderId);
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
        ParasiteReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new RuntimeException("提醒不存在");
        }
        this.removeById(reminderId);
        log.info("删除提醒成功: reminderId={}", reminderId);
    }
}

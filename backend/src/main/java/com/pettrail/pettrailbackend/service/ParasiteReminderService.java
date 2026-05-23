package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pettrail.pettrailbackend.dto.ParasiteReminderVO;
import com.pettrail.pettrailbackend.entity.ParasiteReminder;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.ParasiteReminderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParasiteReminderService extends ServiceImpl<ParasiteReminderMapper, ParasiteReminder> {

    private final HealthAnalysisCacheService healthAnalysisCacheService;

    private static final Map<Integer, String> TYPE_NAME_MAP = Map.of(
            1, "体内驱虫",
            2, "体外驱虫",
            3, "体内外驱虫"
    );

    public List<ParasiteReminderVO> listByPetId(Long petId) {
        LambdaQueryWrapper<ParasiteReminder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParasiteReminder::getPetId, petId);
        queryWrapper.orderByAsc(ParasiteReminder::getNextDate);
        return this.list(queryWrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    public List<ParasiteReminderVO> listUpcoming(Long petId) {
        LambdaQueryWrapper<ParasiteReminder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParasiteReminder::getPetId, petId);
        queryWrapper.eq(ParasiteReminder::getStatus, 0);
        queryWrapper.le(ParasiteReminder::getNextDate, LocalDate.now().plusDays(7));
        queryWrapper.orderByAsc(ParasiteReminder::getNextDate);
        return this.list(queryWrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public ParasiteReminder createReminder(Long petId, Long userId, Integer type, LocalDate nextDate, String productName, String note) {
        ParasiteReminder reminder = new ParasiteReminder();
        reminder.setPetId(petId);
        reminder.setUserId(userId);
        reminder.setType(type);
        reminder.setProductName(productName);
        reminder.setNextDate(nextDate);
        reminder.setNote(note);
        reminder.setStatus(0);
        reminder.setCreatedAt(LocalDateTime.now());
        reminder.setUpdatedAt(LocalDateTime.now());

        this.save(reminder);
        log.info("创建寄生虫提醒成功: id={}, petId={}, userId={}, type={}, productName={}, note={}", reminder.getId(), petId, userId, type, productName, note);
        return reminder;
    }

    public ParasiteReminder getReminderById(Long reminderId) {
        ParasiteReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new BusinessException(404, "提醒不存在");
        }
        return reminder;
    }

    @Transactional(rollbackFor = Exception.class)
    public ParasiteReminder updateReminder(Long reminderId, Integer type, LocalDate nextDate, String note) {
        ParasiteReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new BusinessException(404, "提醒不存在");
        }
        if (type != null) {
            reminder.setType(type);
        }
        if (nextDate != null) {
            reminder.setNextDate(nextDate);
        }
        if (note != null) {
            reminder.setNote(note);
        }
        reminder.setUpdatedAt(LocalDateTime.now());
        this.updateById(reminder);
        log.info("更新提醒信息成功: reminderId={}, type={}, nextDate={}, note={}", reminderId, type, nextDate, note);
        return reminder;
    }

    @Transactional(rollbackFor = Exception.class)
    public ParasiteReminder updateStatus(Long reminderId, Integer status) {
        ParasiteReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new BusinessException(404, "提醒不存在");
        }
        Integer oldStatus = reminder.getStatus();
        reminder.setStatus(status);
        reminder.setUpdatedAt(LocalDateTime.now());
        this.updateById(reminder);
        log.info("更新提醒状态成功: reminderId={}, status={}", reminderId, status);

        if (status == 1 && oldStatus != null && oldStatus != 1) {
            healthAnalysisCacheService.invalidateByPetId(reminder.getPetId());
            log.info("驱虫确认完成，清除宠物AI健康分析缓存: petId={}", reminder.getPetId());
        }
        return reminder;
    }

    @Transactional(rollbackFor = Exception.class)
    public ParasiteReminder updateNextDate(Long reminderId, LocalDate nextDate) {
        ParasiteReminder reminder = this.getById(reminderId);
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
        ParasiteReminder reminder = this.getById(reminderId);
        if (reminder == null) {
            throw new BusinessException(404, "提醒不存在");
        }
        this.removeById(reminderId);
        log.info("删除提醒成功: reminderId={}", reminderId);
    }

    private ParasiteReminderVO toVO(ParasiteReminder reminder) {
        ParasiteReminderVO vo = new ParasiteReminderVO();
        vo.setId(reminder.getId());
        vo.setPetId(reminder.getPetId());
        vo.setType(reminder.getType());
        vo.setTypeName(TYPE_NAME_MAP.getOrDefault(reminder.getType(), "驱虫"));
        vo.setProductName(reminder.getProductName());
        vo.setNextDate(reminder.getNextDate());
        vo.setIntervalDays(reminder.getIntervalDays());
        vo.setStatus(reminder.getStatus());
        vo.setNote(reminder.getNote());
        vo.setCreatedAt(reminder.getCreatedAt());
        return vo;
    }
}

package com.pettrail.pettrailbackend.service;

import com.alibaba.fastjson2.JSON;
import com.pettrail.pettrailbackend.entity.CheckinItem;
import com.pettrail.pettrailbackend.entity.CheckinRecord;
import com.pettrail.pettrailbackend.entity.CheckinStats;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.CheckinItemMapper;
import com.pettrail.pettrailbackend.mapper.CheckinRecordMapper;
import com.pettrail.pettrailbackend.mapper.CheckinStatsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 打卡服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinItemMapper checkinItemMapper;
    private final CheckinRecordMapper checkinRecordMapper;
    private final CheckinStatsMapper checkinStatsMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取打卡项列表
     */
    public List<CheckinItem> getCheckinItems() {
        List<CheckinItem> defaultItems = checkinItemMapper.selectDefaultItems();
        return defaultItems;
    }

    /**
     * 打卡（幂等性保证）
     */
    @Transactional(rollbackFor = Exception.class)
    public CheckinRecord checkin(Long userId, Long petId, Long itemId, String note, List<String> images) {
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ISO_DATE);
        log.info("开始打卡: userId={}, petId={}, itemId={}, date={}", userId, petId, itemId, todayStr);

        // // 1. Redis 防重（原子操作）
        // String redisKey = String.format("checkin:%s:user:%d", todayStr, userId);
        // String fieldKey = String.valueOf(itemId);
        //
        // Boolean isNew = redisTemplate.opsForHash()
        //     .putIfAbsent(redisKey, fieldKey, todayStr);
        //
        // if (Boolean.FALSE.equals(isNew)) {
        //     throw new BusinessException("今日已完成该打卡项");
        // }
        // redisTemplate.expire(redisKey, 2, TimeUnit.DAYS);

        // 2. 检查是否已打卡
        log.info("检查是否已打卡: userId={}, petId={}, itemId={}, date={}", userId, petId, itemId, todayStr);
        CheckinRecord existing = checkinRecordMapper.selectByUserIdItemIdAndDate(userId, petId, itemId, today);
        if (existing != null) {
            log.warn("重复打卡: userId={}, petId={}, itemId={}, date={}, existingId={}", userId, petId, itemId, todayStr, existing.getId());
            throw new BusinessException("今日已完成该打卡项");
        }

        // 3. 创建打卡记录
        log.info("创建打卡记录: userId={}, petId={}, itemId={}", userId, petId, itemId);
        CheckinRecord record = new CheckinRecord();
        record.setUserId(userId);
        record.setPetId(petId);
        record.setItemId(itemId);
        record.setRecordDate(today);
        record.setStatus(1);
        record.setNote(note);
        record.setImages(images != null ? JSON.toJSONString(images) : null);
        
        int insertResult = checkinRecordMapper.insert(record);
        log.info("打卡记录插入结果: rows={}, recordId={}", insertResult, record.getId());
        
        if (insertResult <= 0) {
            log.error("打卡记录插入失败: userId={}, itemId={}", userId, itemId);
            throw new BusinessException("打卡记录保存失败");
        }

        // 4. 更新统计（连续打卡计算）
        log.info("更新打卡统计: userId={}, itemId={}", userId, itemId);
        updateCheckinStats(userId, itemId, today);

        log.info("打卡完成: userId={}, itemId={}, recordId={}", userId, itemId, record.getId());
        return record;
    }

    /**
     * 更新打卡统计
     */
    private void updateCheckinStats(Long userId, Long itemId, LocalDate today) {
        log.info("查询打卡统计: userId={}, itemId={}", userId, itemId);
        CheckinStats stats = checkinStatsMapper.selectByUserIdAndItemId(userId, itemId);

        if (stats == null) {
            log.info("初始化打卡统计: userId={}, itemId={}", userId, itemId);
            stats = new CheckinStats();
            stats.setUserId(userId);
            stats.setItemId(itemId);
            stats.setTotalCount(1);
            stats.setCurrentStreak(1);
            stats.setMaxStreak(1);
            stats.setLastCheckinDate(today);
            int insertResult = checkinStatsMapper.insert(stats);
            log.info("插入打卡统计结果: rows={}, statsId={}", insertResult, stats.getId());
        } else {
            log.info("更新打卡统计: userId={}, itemId={}, oldTotal={}, oldStreak={}", userId, itemId, stats.getTotalCount(), stats.getCurrentStreak());
            stats.setTotalCount(stats.getTotalCount() + 1);

            // 计算连续打卡
            LocalDate lastDate = stats.getLastCheckinDate();
            if (lastDate != null) {
                long daysBetween = ChronoUnit.DAYS.between(lastDate, today);
                log.info("计算连续打卡: lastDate={}, today={}, daysBetween={}", lastDate, today, daysBetween);
                if (daysBetween == 1) {
                    stats.setCurrentStreak(stats.getCurrentStreak() + 1);
                } else if (daysBetween > 1) {
                    stats.setCurrentStreak(1); // 中断，重新开始
                }
            }

            stats.setMaxStreak(Math.max(stats.getMaxStreak(), stats.getCurrentStreak()));
            stats.setLastCheckinDate(today);
            int updateResult = checkinStatsMapper.updateById(stats);
            log.info("更新打卡统计结果: rows={}, newTotal={}, newStreak={}", updateResult, stats.getTotalCount(), stats.getCurrentStreak());
        }
    }

    /**
     * 获取打卡日历
     */
    public List<CheckinRecord> getCalendar(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return checkinRecordMapper.selectByUserIdAndDateRange(userId, startDate, endDate);
    }

    /**
     * 取消打卡
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelCheckin(Long userId, Long recordId) {
        CheckinRecord record = checkinRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("打卡记录不存在");
        }

        record.setStatus(2); // 2-取消
        checkinRecordMapper.updateById(record);

        // 更新统计
        CheckinStats stats = checkinStatsMapper.selectByUserIdAndItemId(userId, record.getItemId());
        if (stats != null && stats.getCurrentStreak() > 0) {
            stats.setCurrentStreak(stats.getCurrentStreak() - 1);
            stats.setTotalCount(Math.max(0, stats.getTotalCount() - 1));
            checkinStatsMapper.updateById(stats);
        }
    }
}

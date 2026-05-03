package com.pettrail.pettrailbackend.service;

import com.alibaba.fastjson2.JSON;
import com.pettrail.pettrailbackend.entity.CheckinItem;
import com.pettrail.pettrailbackend.entity.CheckinRecord;
import com.pettrail.pettrailbackend.entity.CheckinStats;
import com.pettrail.pettrailbackend.entity.UserHiddenItem;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.CheckinItemMapper;
import com.pettrail.pettrailbackend.mapper.CheckinRecordMapper;
import com.pettrail.pettrailbackend.mapper.CheckinStatsMapper;
import com.pettrail.pettrailbackend.mapper.UserHiddenItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private final UserHiddenItemMapper userHiddenItemMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AchievementService achievementService;

    /**
     * 获取打卡项列表
     */
    public List<CheckinItem> getCheckinItems() {
        List<CheckinItem> defaultItems = checkinItemMapper.selectDefaultItems();
        return defaultItems;
    }

    public List<CheckinItem> getUserCheckinItems(Long userId) {
        List<CheckinItem> defaultItems = checkinItemMapper.selectDefaultItems();
        if (userId != null) {
            List<CheckinItem> customItems = checkinItemMapper.selectByUserId(userId);
            defaultItems.addAll(customItems);

            Set<Long> hiddenIds = userHiddenItemMapper.selectHiddenItemIdsByUserId(userId)
                    .stream().collect(Collectors.toSet());
            for (CheckinItem item : defaultItems) {
                item.setHidden(hiddenIds.contains(item.getId()));
            }
        } else {
            for (CheckinItem item : defaultItems) {
                item.setHidden(false);
            }
        }
        return defaultItems;
    }

    @Transactional(rollbackFor = Exception.class)
    public void hideItem(Long userId, Long itemId) {
        if (userHiddenItemMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserHiddenItem>()
                .eq(UserHiddenItem::getUserId, userId)
                .eq(UserHiddenItem::getItemId, itemId)).isEmpty()) {
            UserHiddenItem hidden = new UserHiddenItem();
            hidden.setUserId(userId);
            hidden.setItemId(itemId);
            userHiddenItemMapper.insert(hidden);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void showItem(Long userId, Long itemId) {
        userHiddenItemMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserHiddenItem>()
                .eq(UserHiddenItem::getUserId, userId)
                .eq(UserHiddenItem::getItemId, itemId));
    }

    @Transactional(rollbackFor = Exception.class)
    public CheckinItem createCustomItem(Long userId, String name, String icon, Integer type, String description) {
        CheckinItem item = new CheckinItem();
        item.setUserId(userId);
        item.setName(name);
        item.setIcon(icon);
        item.setType(type != null ? type : 1);
        item.setDescription(description);
        item.setSortOrder(100);
        item.setIsDefault(0);
        item.setIsEnabled(1);
        checkinItemMapper.insert(item);
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public CheckinItem updateCustomItem(Long userId, Long itemId, String name, String icon, Integer type, String description) {
        CheckinItem item = checkinItemMapper.selectById(itemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("打卡项不存在或无权修改");
        }
        if (name != null) item.setName(name);
        if (icon != null) item.setIcon(icon);
        if (type != null) item.setType(type);
        if (description != null) item.setDescription(description);
        checkinItemMapper.updateById(item);
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCustomItem(Long userId, Long itemId) {
        CheckinItem item = checkinItemMapper.selectById(itemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("打卡项不存在或无权删除");
        }
        if (item.getIsDefault() != null && item.getIsDefault() == 1) {
            throw new BusinessException("默认打卡项不可删除");
        }
        item.setIsEnabled(0);
        checkinItemMapper.updateById(item);
    }

    /**
     * 打卡（幂等性保证）
     */
    @Transactional(rollbackFor = Exception.class)
    public CheckinRecord checkin(Long userId, Long petId, Long itemId, String note, List<String> images) {
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ISO_DATE);
        log.info("开始打卡: userId={}, petId={}, itemId={}, date={}", userId, petId, itemId, todayStr);

        String redisKey = String.format("checkin:%s:user:%d:pet:%d", todayStr, userId, petId);
        String fieldKey = String.valueOf(itemId);

        Boolean isNew = redisTemplate.opsForHash()
            .putIfAbsent(redisKey, fieldKey, todayStr);

        if (Boolean.FALSE.equals(isNew)) {
            throw new BusinessException("今日已完成该打卡项");
        }
        redisTemplate.expire(redisKey, 2, TimeUnit.DAYS);

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

        try {
            achievementService.checkAndUnlock(userId, "checkin_count");
            achievementService.checkAndUnlock(userId, "checkin_streak");
        } catch (Exception e) {
            log.warn("成就检查失败: userId={}, error={}", userId, e.getMessage());
        }

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

    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();

        List<CheckinStats> allStats = checkinStatsMapper.selectList(
            new LambdaQueryWrapper<CheckinStats>().eq(CheckinStats::getUserId, userId));

        int totalCheckins = 0;
        int maxStreak = 0;
        int currentStreak = 0;

        for (CheckinStats s : allStats) {
            totalCheckins += s.getTotalCount() != null ? s.getTotalCount() : 0;
            if (s.getMaxStreak() != null && s.getMaxStreak() > maxStreak) {
                maxStreak = s.getMaxStreak();
            }
            if (s.getLastCheckinDate() != null) {
                long daysDiff = ChronoUnit.DAYS.between(s.getLastCheckinDate(), today);
                if (daysDiff <= 1 && s.getCurrentStreak() != null) {
                    if (s.getCurrentStreak() > currentStreak) {
                        currentStreak = s.getCurrentStreak();
                    }
                }
            }
        }

        result.put("totalCount", totalCheckins);
        result.put("currentStreak", currentStreak);
        result.put("maxStreak", maxStreak);

        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        List<CheckinRecord> weekRecords = checkinRecordMapper.selectByUserIdAndDateRange(userId, weekStart, today);
        long weekDays = weekRecords.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .map(CheckinRecord::getRecordDate)
                .distinct()
                .count();
        result.put("weekDays", weekDays);

        LocalDate monthStart = today.withDayOfMonth(1);
        List<CheckinRecord> monthRecords = checkinRecordMapper.selectByUserIdAndDateRange(userId, monthStart, today);
        long monthDays = monthRecords.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .map(CheckinRecord::getRecordDate)
                .distinct()
                .count();
        result.put("monthDays", monthDays);

        List<Map<String, Object>> itemStats = new java.util.ArrayList<>();
        for (CheckinStats s : allStats) {
            CheckinItem item = checkinItemMapper.selectById(s.getItemId());
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("itemId", s.getItemId());
            itemMap.put("itemName", item != null ? item.getName() : "未知");
            itemMap.put("itemIcon", item != null ? item.getIcon() : "📋");
            itemMap.put("totalCount", s.getTotalCount());
            itemMap.put("currentStreak", s.getCurrentStreak());
            itemMap.put("maxStreak", s.getMaxStreak());
            itemMap.put("lastCheckinDate", s.getLastCheckinDate());
            itemStats.add(itemMap);
        }
        result.put("itemStats", itemStats);

        return result;
    }
}

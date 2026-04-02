package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.entity.StepRecord;
import com.pettrail.pettrailbackend.entity.WaterRecord;
import com.pettrail.pettrailbackend.mapper.StepRecordMapper;
import com.pettrail.pettrailbackend.mapper.WaterRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 健康记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthRecordService {

    private final StepRecordMapper stepRecordMapper;
    private final WaterRecordMapper waterRecordMapper;

    /**
     * 记录步数
     */
    @Transactional(rollbackFor = Exception.class)
    public StepRecord recordStep(Long userId, Long petId, Integer steps, BigDecimal distance, LocalDate recordDate) {
        // 检查是否已存在
        StepRecord existing = stepRecordMapper.selectByUserIdPetIdDate(userId, petId, recordDate);

        if (existing == null) {
            StepRecord record = new StepRecord();
            record.setUserId(userId);
            record.setPetId(petId);
            record.setSteps(steps);
            record.setDistance(distance);
            record.setRecordDate(recordDate);
            record.setSource(1); // 1-手动
            stepRecordMapper.insert(record);
            return record;
        } else {
            existing.setSteps(steps);
            existing.setDistance(distance);
            stepRecordMapper.updateById(existing);
            return existing;
        }
    }

    /**
     * 记录饮水量
     */
    @Transactional(rollbackFor = Exception.class)
    public WaterRecord recordWater(Long userId, Long petId, BigDecimal amount, 
                                   LocalDate recordDate, LocalTime recordTime) {
        WaterRecord record = new WaterRecord();
        record.setUserId(userId);
        record.setPetId(petId);
        record.setAmount(amount);
        record.setRecordDate(recordDate);
        record.setRecordTime(recordTime != null ? recordTime : LocalTime.now());
        waterRecordMapper.insert(record);
        return record;
    }

    /**
     * 获取健康数据看板
     */
    public Map<String, Object> getDashboard(Long userId, Long petId) {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        // 今日步数
        StepRecord stepRecord = stepRecordMapper.selectByUserIdPetIdDate(userId, petId, today);
        result.put("todaySteps", stepRecord != null ? stepRecord.getSteps() : 0);

        // 今日饮水
        BigDecimal totalWater = waterRecordMapper.sumByUserIdPetIdDate(userId, petId, today);
        result.put("todayWater", totalWater != null ? totalWater.doubleValue() : 0);

        // 7 天步数趋势
        List<StepRecord> weekStepsTrend = stepRecordMapper.selectTrend(userId, petId, weekStart, today);
        result.put("weekStepsTrend", weekStepsTrend);

        // 7 天饮水趋势
        List<WaterRecord> weekWaterTrend = waterRecordMapper.selectDailySum(userId, petId, weekStart, today);
        result.put("weekWaterTrend", weekWaterTrend);

        return result;
    }
}

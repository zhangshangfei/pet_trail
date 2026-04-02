package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.StepRecord;
import com.pettrail.pettrailbackend.entity.WaterRecord;
import com.pettrail.pettrailbackend.service.HealthRecordService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

/**
 * 健康记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    /**
     * 记录步数
     */
    @PostMapping("/steps")
    public Result<StepRecord> recordStep(
            @RequestParam Integer steps,
            @RequestParam(required = false) BigDecimal distance,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate recordDate,
            @RequestParam(required = false) Long petId) {
        
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        LocalDate date = recordDate != null ? recordDate : LocalDate.now();
        StepRecord record = healthRecordService.recordStep(userId, petId, steps, distance, date);
        return Result.success(record);
    }

    /**
     * 记录饮水量
     */
    @PostMapping("/water")
    public Result<WaterRecord> recordWater(
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate recordDate,
            @RequestParam(required = false) String recordTime,
            @RequestParam(required = false) Long petId) {
        
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        LocalDate date = recordDate != null ? recordDate : LocalDate.now();
        LocalTime time = null;
        if (recordTime != null) {
            time = LocalTime.parse(recordTime);
        }

        WaterRecord record = healthRecordService.recordWater(userId, petId, amount, date, time);
        return Result.success(record);
    }

    /**
     * 获取健康数据看板
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(@RequestParam(required = false) Long petId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        Map<String, Object> data = healthRecordService.getDashboard(userId, petId);
        return Result.success(data);
    }
}

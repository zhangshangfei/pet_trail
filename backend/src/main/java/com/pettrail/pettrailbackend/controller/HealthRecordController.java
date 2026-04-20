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

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    @GetMapping("/score")
    public Result<Map<String, Object>> getHealthScore(@RequestParam Long petId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        try {
            Map<String, Object> scoreData = healthRecordService.calculateHealthScore(userId, petId);
            return Result.success(scoreData);
        } catch (Exception e) {
            log.error("计算健康评分失败: {}", e.getMessage(), e);
            return Result.error("计算失败：" + e.getMessage());
        }
    }

    /**
     * 记录步数
     */
    @PostMapping("/steps")
    public Result<StepRecord> recordStep(@RequestBody java.util.Map<String, Object> requestBody) {
        Integer steps = requestBody.get("steps") != null ? Integer.parseInt(requestBody.get("steps").toString()) : null;
        
        BigDecimal distance = null;
        if (requestBody.get("distance") != null) {
            distance = new BigDecimal(requestBody.get("distance").toString());
        }
        
        LocalDate recordDate = null;
        if (requestBody.get("recordDate") != null) {
            recordDate = LocalDate.parse(requestBody.get("recordDate").toString());
        }
        
        Long petId = null;
        if (requestBody.get("petId") != null) {
            petId = Long.parseLong(requestBody.get("petId").toString());
        }

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        if (steps == null) {
            return Result.error(400, "步数参数不能为空");
        }

        LocalDate date = recordDate != null ? recordDate : LocalDate.now();
        StepRecord record = healthRecordService.recordStep(userId, petId, steps, distance, date);
        return Result.success(record);
    }

    /**
     * 记录饮水量
     */
    @PostMapping("/water")
    public Result<WaterRecord> recordWater(@RequestBody java.util.Map<String, Object> requestBody) {
        BigDecimal amount = null;
        if (requestBody.get("amount") != null) {
            amount = new BigDecimal(requestBody.get("amount").toString());
        }
        
        LocalDate recordDate = null;
        if (requestBody.get("recordDate") != null) {
            recordDate = LocalDate.parse(requestBody.get("recordDate").toString());
        }
        
        LocalTime recordTime = null;
        if (requestBody.get("recordTime") != null) {
            recordTime = LocalTime.parse(requestBody.get("recordTime").toString());
        }
        
        Long petId = null;
        if (requestBody.get("petId") != null) {
            petId = Long.parseLong(requestBody.get("petId").toString());
        }

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        if (amount == null) {
            return Result.error(400, "水量参数不能为空");
        }

        LocalDate date = recordDate != null ? recordDate : LocalDate.now();
        WaterRecord record = healthRecordService.recordWater(userId, petId, amount, date, recordTime);
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

package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.StepRecord;
import com.pettrail.pettrailbackend.entity.WaterRecord;
import com.pettrail.pettrailbackend.service.HealthRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthRecordController extends BaseController {

    private final HealthRecordService healthRecordService;

    @GetMapping("/score")
    public Result<Map<String, Object>> getHealthScore(@RequestParam Long petId) {
        Long userId = requireLogin();
        return Result.success(healthRecordService.calculateHealthScore(userId, petId));
    }

    @PostMapping("/steps")
    public Result<StepRecord> recordStep(@RequestBody java.util.Map<String, Object> requestBody) {
        Long userId = requireLogin();
        Integer steps = requestBody.get("steps") != null ? Integer.parseInt(requestBody.get("steps").toString()) : null;
        if (steps == null) {
            return Result.error(400, "步数参数不能为空");
        }

        BigDecimal distance = requestBody.get("distance") != null ? new BigDecimal(requestBody.get("distance").toString()) : null;
        LocalDate recordDate = requestBody.get("recordDate") != null ? LocalDate.parse(requestBody.get("recordDate").toString()) : null;
        Long petId = requestBody.get("petId") != null ? Long.parseLong(requestBody.get("petId").toString()) : null;
        LocalDate date = recordDate != null ? recordDate : LocalDate.now();

        return Result.success(healthRecordService.recordStep(userId, petId, steps, distance, date));
    }

    @PostMapping("/water")
    public Result<WaterRecord> recordWater(@RequestBody java.util.Map<String, Object> requestBody) {
        Long userId = requireLogin();
        BigDecimal amount = requestBody.get("amount") != null ? new BigDecimal(requestBody.get("amount").toString()) : null;
        if (amount == null) {
            return Result.error(400, "水量参数不能为空");
        }

        LocalDate recordDate = requestBody.get("recordDate") != null ? LocalDate.parse(requestBody.get("recordDate").toString()) : null;
        LocalTime recordTime = requestBody.get("recordTime") != null ? LocalTime.parse(requestBody.get("recordTime").toString()) : null;
        Long petId = requestBody.get("petId") != null ? Long.parseLong(requestBody.get("petId").toString()) : null;
        LocalDate date = recordDate != null ? recordDate : LocalDate.now();

        return Result.success(healthRecordService.recordWater(userId, petId, amount, date, recordTime));
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(@RequestParam(required = false) Long petId) {
        Long userId = requireLogin();
        return Result.success(healthRecordService.getDashboard(userId, petId));
    }
}

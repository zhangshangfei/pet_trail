package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.service.HealthRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(@RequestParam(required = false) Long petId) {
        Long userId = requireLogin();
        return Result.success(healthRecordService.getDashboard(userId, petId));
    }
}

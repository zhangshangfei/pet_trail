package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.entity.WeightRecord;
import com.pettrail.pettrailbackend.service.PetService;
import com.pettrail.pettrailbackend.service.WeightRecordService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pets/{petId}/weight-records")
@RequiredArgsConstructor
public class WeightRecordController {

    private final PetService petService;
    private final WeightRecordService weightRecordService;

    /**
     * 获取宠物的体重记录
     */
    @GetMapping
    public Map<String, Object> listRecords(@PathVariable Long petId) {
        log.info("获取体重记录: petId={}", petId);
        Map<String, Object> result = new HashMap<>();
        try {
            validatePetOwnership(petId);
            List<WeightRecord> records = weightRecordService.listByPetId(petId);
            result.put("success", true);
            result.put("data", records);
            result.put("message", "获取成功");
        } catch (Exception e) {
            log.error("获取体重记录失败: petId={}, error={}", petId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取指定时间范围的体重记录
     */
    @GetMapping("/range")
    public Map<String, Object> listByDateRange(
            @PathVariable Long petId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("获取时间范围内的体重记录: petId={}, startDate={}, endDate={}", petId, startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        try {
            validatePetOwnership(petId);
            List<WeightRecord> records = weightRecordService.listByPetIdAndDateRange(petId, startDate, endDate);
            result.put("success", true);
            result.put("data", records);
            result.put("message", "获取成功");
        } catch (Exception e) {
            log.error("获取时间范围内的体重记录失败: petId={}, error={}", petId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取最后一次体重记录
     */
    @GetMapping("/last")
    public Map<String, Object> getLastRecord(@PathVariable Long petId) {
        log.info("获取最后一次体重记录: petId={}", petId);
        Map<String, Object> result = new HashMap<>();
        try {
            validatePetOwnership(petId);
            WeightRecord record = weightRecordService.getLastRecord(petId);
            result.put("success", true);
            result.put("data", record);
            result.put("message", "获取成功");
        } catch (Exception e) {
            log.error("获取最后一次体重记录失败: petId={}, error={}", petId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取体重趋势
     */
    @GetMapping("/trend")
    public Map<String, Object> getTrend(
            @PathVariable Long petId,
            @RequestParam(defaultValue = "7") int days) {
        log.info("获取体重趋势: petId={}, days={}", petId, days);
        Map<String, Object> result = new HashMap<>();
        try {
            validatePetOwnership(petId);
            List<WeightRecord> records = weightRecordService.getTrend(petId, days);
            result.put("success", true);
            result.put("data", records);
            result.put("message", "获取成功");
        } catch (Exception e) {
            log.error("获取体重趋势失败: petId={}, error={}", petId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 创建体重记录
     */
    @PostMapping
    public Map<String, Object> createRecord(
            @PathVariable Long petId,
            @RequestParam BigDecimal weight,
            @RequestParam(required = false) LocalDate recordDate) {
        log.info("创建体重记录: petId={}, weight={}, recordDate={}", petId, weight, recordDate);
        Map<String, Object> result = new HashMap<>();
        try {
            validatePetOwnership(petId);
            LocalDate date = recordDate != null ? recordDate : LocalDate.now();
            WeightRecord record = weightRecordService.createRecord(petId, weight, date);
            // 同步更新 pets 表中的当前体重
            petService.updatePetWeight(petId, weight);
            result.put("success", true);
            result.put("data", record);
            result.put("message", "创建成功");
        } catch (Exception e) {
            log.error("创建体重记录失败: petId={}, error={}", petId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除体重记录
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteRecord(
            @PathVariable Long petId,
            @PathVariable Long id) {
        log.info("删除体重记录: id={}", id);
        Map<String, Object> result = new HashMap<>();
        try {
            validatePetOwnership(petId);
            weightRecordService.deleteRecord(id);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            log.error("删除体重记录失败: id={}, error={}", id, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 校验当前登录用户是否拥有该宠物
     */
    private void validatePetOwnership(Long petId) {
        Long currentUserId = UserContext.getCurrentUserId();
        Pet pet = petService.getPetDetail(petId);
        if (!currentUserId.equals(pet.getUserId())) {
            throw new RuntimeException("无权限操作该宠物");
        }
    }
}

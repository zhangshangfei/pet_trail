package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.entity.WeightRecord;
import com.pettrail.pettrailbackend.exception.ForbiddenException;
import com.pettrail.pettrailbackend.exception.NotFoundException;
import com.pettrail.pettrailbackend.service.PetService;
import com.pettrail.pettrailbackend.service.WeightRecordService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    public Result<List<WeightRecord>> listRecords(@PathVariable Long petId) {
        log.info("获取体重记录：petId={}", petId);
        validatePetOwnership(petId);
        List<WeightRecord> records = weightRecordService.listByPetId(petId);
        return Result.success(records);
    }

    /**
     * 获取指定时间范围的体重记录
     */
    @GetMapping("/range")
    public Result<List<WeightRecord>> listByDateRange(
            @PathVariable Long petId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("获取时间范围内的体重记录：petId={}, startDate={}, endDate={}", petId, startDate, endDate);
        validatePetOwnership(petId);
        List<WeightRecord> records = weightRecordService.listByPetIdAndDateRange(petId, startDate, endDate);
        return Result.success(records);
    }

    /**
     * 获取最后一次体重记录
     */
    @GetMapping("/last")
    public Result<WeightRecord> getLastRecord(@PathVariable Long petId) {
        log.info("获取最后一次体重记录：petId={}", petId);
        validatePetOwnership(petId);
        WeightRecord record = weightRecordService.getLastRecord(petId);
        return record != null ? Result.success(record) : Result.error(404, "暂无记录");
    }

    /**
     * 获取体重趋势
     */
    @GetMapping("/trend")
    public Result<List<WeightRecord>> getTrend(
            @PathVariable Long petId,
            @RequestParam(defaultValue = "7") int days) {
        log.info("获取体重趋势：petId={}, days={}", petId, days);
        validatePetOwnership(petId);
        List<WeightRecord> records = weightRecordService.getTrend(petId, days);
        return Result.success(records);
    }

    /**
     * 创建体重记录
     */
    @PostMapping
    public Result<WeightRecord> createRecord(
            @PathVariable Long petId,
            @RequestBody java.util.Map<String, Object> requestBody) {
        BigDecimal weight = new BigDecimal(requestBody.get("weight").toString());
        LocalDate recordDate = null;
        if (requestBody.containsKey("recordDate") && requestBody.get("recordDate") != null) {
            recordDate = LocalDate.parse(requestBody.get("recordDate").toString());
        }
        log.info("创建体重记录：petId={}, weight={}, recordDate={}", petId, weight, recordDate);
        validatePetOwnership(petId);
        LocalDate date = recordDate != null ? recordDate : LocalDate.now();
        WeightRecord record = weightRecordService.createRecord(petId, weight, date);
        // 同步更新 pets 表中的当前体重
        petService.updatePetWeight(petId, weight);
        return Result.success(record);
    }

    /**
     * 更新体重记录
     */
    @PutMapping("/{id}")
    public Result<WeightRecord> updateRecord(
            @PathVariable Long petId,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> requestBody) {
        BigDecimal weight = new BigDecimal(requestBody.get("weight").toString());
        LocalDate recordDate = null;
        if (requestBody.containsKey("recordDate") && requestBody.get("recordDate") != null) {
            recordDate = LocalDate.parse(requestBody.get("recordDate").toString());
        }
        log.info("更新体重记录：petId={}, id={}, weight={}, recordDate={}", petId, id, weight, recordDate);
        validatePetOwnership(petId);
        LocalDate date = recordDate != null ? recordDate : LocalDate.now();
        WeightRecord record = weightRecordService.updateRecord(id, weight, date);
        // 同步更新 pets 表中的当前体重
        petService.updatePetWeight(petId, weight);
        return Result.success(record);
    }

    /**
     * 删除体重记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(
            @PathVariable Long petId,
            @PathVariable Long id) {
        log.info("删除体重记录：id={}", id);
        validatePetOwnership(petId);
        weightRecordService.deleteRecord(id);
        return Result.success();
    }

    /**
     * 校验当前登录用户是否拥有该宠物
     */
    private void validatePetOwnership(Long petId) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("用户未登录");
        }
        Pet pet = petService.getPetDetail(petId);
        if (pet == null) {
            throw new NotFoundException("宠物不存在");
        }
        if (!currentUserId.equals(pet.getUserId())) {
            throw new ForbiddenException("无权限操作该宠物");
        }
    }
}

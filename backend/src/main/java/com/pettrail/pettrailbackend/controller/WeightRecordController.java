package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.entity.WeightRecord;
import com.pettrail.pettrailbackend.exception.ForbiddenException;
import com.pettrail.pettrailbackend.exception.NotFoundException;
import com.pettrail.pettrailbackend.service.PetService;
import com.pettrail.pettrailbackend.service.WeightRecordService;
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
public class WeightRecordController extends BaseController {

    private final PetService petService;
    private final WeightRecordService weightRecordService;

    @GetMapping
    public Result<List<WeightRecord>> listRecords(@PathVariable Long petId) {
        validatePetOwnership(petId);
        return Result.success(weightRecordService.listByPetId(petId));
    }

    @GetMapping("/range")
    public Result<List<WeightRecord>> listByDateRange(
            @PathVariable Long petId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        validatePetOwnership(petId);
        return Result.success(weightRecordService.listByPetIdAndDateRange(petId, startDate, endDate));
    }

    @GetMapping("/last")
    public Result<WeightRecord> getLastRecord(@PathVariable Long petId) {
        validatePetOwnership(petId);
        WeightRecord record = weightRecordService.getLastRecord(petId);
        return record != null ? Result.success(record) : Result.error(404, "暂无记录");
    }

    @GetMapping("/trend")
    public Result<List<WeightRecord>> getTrend(@PathVariable Long petId, @RequestParam(defaultValue = "7") int days) {
        validatePetOwnership(petId);
        return Result.success(weightRecordService.getTrend(petId, days));
    }

    @PostMapping
    public Result<WeightRecord> createRecord(@PathVariable Long petId, @RequestBody java.util.Map<String, Object> requestBody) {
        validatePetOwnership(petId);
        BigDecimal weight = new BigDecimal(requestBody.get("weight").toString());
        LocalDate recordDate = requestBody.containsKey("recordDate") && requestBody.get("recordDate") != null
                ? LocalDate.parse(requestBody.get("recordDate").toString()) : null;
        String note = requestBody.get("note") != null ? requestBody.get("note").toString() : null;
        LocalDate date = recordDate != null ? recordDate : LocalDate.now();
        WeightRecord record = weightRecordService.createRecord(petId, weight, date, note);
        petService.updatePetWeight(petId, weight);
        return Result.success(record);
    }

    @PutMapping("/{id}")
    public Result<WeightRecord> updateRecord(
            @PathVariable Long petId, @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> requestBody) {
        validatePetOwnership(petId);
        BigDecimal weight = new BigDecimal(requestBody.get("weight").toString());
        LocalDate recordDate = requestBody.containsKey("recordDate") && requestBody.get("recordDate") != null
                ? LocalDate.parse(requestBody.get("recordDate").toString()) : null;
        String note = requestBody.get("note") != null ? requestBody.get("note").toString() : null;
        LocalDate date = recordDate != null ? recordDate : LocalDate.now();
        WeightRecord record = weightRecordService.updateRecord(id, weight, date, note);
        petService.updatePetWeight(petId, weight);
        return Result.success(record);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(@PathVariable Long petId, @PathVariable Long id) {
        validatePetOwnership(petId);
        weightRecordService.deleteRecord(id);
        return Result.success();
    }

    private void validatePetOwnership(Long petId) {
        Long currentUserId = requireLogin();
        Pet pet = petService.getPetDetail(petId);
        if (pet == null) {
            throw new NotFoundException("宠物不存在");
        }
        if (!currentUserId.equals(pet.getUserId())) {
            throw new ForbiddenException("无权限操作该宠物");
        }
    }
}

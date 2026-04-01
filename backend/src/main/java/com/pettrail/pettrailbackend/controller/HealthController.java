package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.entity.WeightRecord;
import com.pettrail.pettrailbackend.entity.VaccineReminder;
import com.pettrail.pettrailbackend.entity.ParasiteReminder;
import com.pettrail.pettrailbackend.service.WeightRecordService;
import com.pettrail.pettrailbackend.service.VaccineReminderService;
import com.pettrail.pettrailbackend.service.ParasiteReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final WeightRecordService weightRecordService;
    private final VaccineReminderService vaccineReminderService;
    private final ParasiteReminderService parasiteReminderService;

    /**
     * 获取宠物的体重记录
     */
    @GetMapping("/weight-records")
    public Map<String, Object> getWeightRecords(@RequestParam Long petId) {
        log.info("获取体重记录: petId={}", petId);
        Map<String, Object> result = new HashMap<>();
        try {
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
     * 获取宠物的疫苗提醒列表
     */
    @GetMapping("/vaccine-reminders")
    public Map<String, Object> getVaccineReminders(@RequestParam Long petId) {
        log.info("获取疫苗提醒: petId={}", petId);
        Map<String, Object> result = new HashMap<>();
        try {
            List<VaccineReminder> reminders = vaccineReminderService.listByPetId(petId);
            result.put("success", true);
            result.put("data", reminders);
            result.put("message", "获取成功");
        } catch (Exception e) {
            log.error("获取疫苗提醒失败: petId={}, error={}", petId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取即将到期的疫苗提醒
     */
    @GetMapping("/vaccine-reminders/upcoming")
    public Map<String, Object> getUpcomingVaccineReminders(@RequestParam Long petId) {
        log.info("获取即将到期的疫苗提醒: petId={}", petId);
        Map<String, Object> result = new HashMap<>();
        try {
            List<VaccineReminder> reminders = vaccineReminderService.listUpcoming(petId);
            result.put("success", true);
            result.put("data", reminders);
            result.put("message", "获取成功");
        } catch (Exception e) {
            log.error("获取即将到期的疫苗提醒失败: petId={}, error={}", petId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取宠物的驱虫提醒列表
     */
    @GetMapping("/parasite-reminders")
    public Map<String, Object> getParasiteReminders(@RequestParam Long petId) {
        log.info("获取驱虫提醒: petId={}", petId);
        Map<String, Object> result = new HashMap<>();
        try {
            List<ParasiteReminder> reminders = parasiteReminderService.listByPetId(petId);
            result.put("success", true);
            result.put("data", reminders);
            result.put("message", "获取成功");
        } catch (Exception e) {
            log.error("获取驱虫提醒失败: petId={}, error={}", petId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取即将到期的驱虫提醒
     */
    @GetMapping("/parasite-reminders/upcoming")
    public Map<String, Object> getUpcomingParasiteReminders(@RequestParam Long petId) {
        log.info("获取即将到期的驱虫提醒: petId={}", petId);
        Map<String, Object> result = new HashMap<>();
        try {
            List<ParasiteReminder> reminders = parasiteReminderService.listUpcoming(petId);
            result.put("success", true);
            result.put("data", reminders);
            result.put("message", "获取成功");
        } catch (Exception e) {
            log.error("获取即将到期的驱虫提醒失败: petId={}, error={}", petId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 创建体重记录
     */
    @PostMapping("/weight-records")
    public Map<String, Object> createWeightRecord(
            @RequestParam Long petId,
            @RequestParam BigDecimal weight,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate recordDate) {
        log.info("创建体重记录: petId={}, weight={}, recordDate={}", petId, weight, recordDate);
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDate date = recordDate != null ? recordDate : LocalDate.now();
            WeightRecord record = weightRecordService.createRecord(petId, weight, date);
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
     * 更新提醒状态
     */
    @PutMapping("/vaccine-reminders/{id}/status")
    public Map<String, Object> updateVaccineStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        log.info("更新疫苗提醒状态: id={}, status={}", id, status);
        Map<String, Object> result = new HashMap<>();
        try {
            VaccineReminder reminder = vaccineReminderService.updateStatus(id, status);
            result.put("success", true);
            result.put("data", reminder);
            result.put("message", "更新成功");
        } catch (Exception e) {
            log.error("更新疫苗提醒状态失败: id={}, error={}", id, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新驱虫提醒状态
     */
    @PutMapping("/parasite-reminders/{id}/status")
    public Map<String, Object> updateParasiteStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        log.info("更新驱虫提醒状态: id={}, status={}", id, status);
        Map<String, Object> result = new HashMap<>();
        try {
            ParasiteReminder reminder = parasiteReminderService.updateStatus(id, status);
            result.put("success", true);
            result.put("data", reminder);
            result.put("message", "更新成功");
        } catch (Exception e) {
            log.error("更新驱虫提醒状态失败: id={}, error={}", id, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }
}

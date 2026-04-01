package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.entity.VaccineReminder;
import com.pettrail.pettrailbackend.service.VaccineReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pets/{petId}/vaccine-reminders")
@RequiredArgsConstructor
public class VaccineReminderController {

    private final VaccineReminderService vaccineReminderService;

    /**
     * 获取宠物的疫苗提醒列表
     */
    @GetMapping
    public Map<String, Object> listReminders(@PathVariable Long petId) {
        log.info("获取疫苗提醒列表: petId={}", petId);
        Map<String, Object> result = new HashMap<>();
        try {
            List<VaccineReminder> reminders = vaccineReminderService.listByPetId(petId);
            result.put("success", true);
            result.put("data", reminders);
            result.put("message", "获取成功");
        } catch (Exception e) {
            log.error("获取疫苗提醒列表失败: petId={}, error={}", petId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取即将到期的疫苗提醒
     */
    @GetMapping("/upcoming")
    public Map<String, Object> listUpcoming(@PathVariable Long petId) {
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
     * 创建疫苗提醒
     */
    @PostMapping
    public Map<String, Object> createReminder(
            @PathVariable Long petId,
            @RequestParam String vaccineName,
            @RequestParam LocalDate nextDate) {
        log.info("创建疫苗提醒: petId={}, vaccineName={}, nextDate={}", petId, vaccineName, nextDate);
        Map<String, Object> result = new HashMap<>();
        try {
            VaccineReminder reminder = vaccineReminderService.createReminder(petId, vaccineName, nextDate);
            result.put("success", true);
            result.put("data", reminder);
            result.put("message", "创建成功");
        } catch (Exception e) {
            log.error("创建疫苗提醒失败: petId={}, error={}", petId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取提醒详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getReminder(
            @PathVariable Long petId,
            @PathVariable Long id) {
        log.info("获取疫苗提醒详情: petId={}, id={}", petId, id);
        Map<String, Object> result = new HashMap<>();
        try {
            VaccineReminder reminder = vaccineReminderService.getReminderById(id);
            result.put("success", true);
            result.put("data", reminder);
            result.put("message", "获取成功");
        } catch (Exception e) {
            log.error("获取疫苗提醒详情失败: id={}, error={}", id, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新提醒状态
     */
    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(
            @PathVariable Long petId,
            @PathVariable Long id,
            @RequestParam Integer status) {
        log.info("更新提醒状态: id={}, status={}", id, status);
        Map<String, Object> result = new HashMap<>();
        try {
            VaccineReminder reminder = vaccineReminderService.updateStatus(id, status);
            result.put("success", true);
            result.put("data", reminder);
            result.put("message", "更新成功");
        } catch (Exception e) {
            log.error("更新提醒状态失败: id={}, error={}", id, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新提醒日期
     */
    @PutMapping("/{id}/next-date")
    public Map<String, Object> updateNextDate(
            @PathVariable Long petId,
            @PathVariable Long id,
            @RequestParam LocalDate nextDate) {
        log.info("更新提醒日期: id={}, nextDate={}", id, nextDate);
        Map<String, Object> result = new HashMap<>();
        try {
            VaccineReminder reminder = vaccineReminderService.updateNextDate(id, nextDate);
            result.put("success", true);
            result.put("data", reminder);
            result.put("message", "更新成功");
        } catch (Exception e) {
            log.error("更新提醒日期失败: id={}, error={}", id, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除提醒
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteReminder(
            @PathVariable Long petId,
            @PathVariable Long id) {
        log.info("删除疫苗提醒: id={}", id);
        Map<String, Object> result = new HashMap<>();
        try {
            vaccineReminderService.deleteReminder(id);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            log.error("删除疫苗提醒失败: id={}, error={}", id, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }
}

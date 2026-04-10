package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.VaccineReminder;
import com.pettrail.pettrailbackend.service.VaccineReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
    public Result<List<VaccineReminder>> listReminders(@PathVariable Long petId) {
        log.info("获取疫苗提醒列表：petId={}", petId);
        List<VaccineReminder> reminders = vaccineReminderService.listByPetId(petId);
        return Result.success(reminders);
    }

    /**
     * 获取即将到期的疫苗提醒
     */
    @GetMapping("/upcoming")
    public Result<List<VaccineReminder>> listUpcoming(@PathVariable Long petId) {
        log.info("获取即将到期的疫苗提醒：petId={}", petId);
        List<VaccineReminder> reminders = vaccineReminderService.listUpcoming(petId);
        return Result.success(reminders);
    }

    /**
     * 创建疫苗提醒
     */
    @PostMapping
    public Result<VaccineReminder> createReminder(
            @PathVariable Long petId,
            @RequestBody java.util.Map<String, Object> requestBody) {
        String vaccineName = (String) requestBody.get("vaccineName");
        String nextDateStr = (String) requestBody.get("nextDate");
        LocalDate nextDate = nextDateStr != null ? LocalDate.parse(nextDateStr) : null;
        String note = requestBody.get("note") != null ? requestBody.get("note").toString() : null;

        Long userId = com.pettrail.pettrailbackend.util.UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        log.info("创建疫苗提醒：petId={}, userId={}, vaccineName={}, nextDate={}, note={}", petId, userId, vaccineName, nextDate, note);
        VaccineReminder reminder = vaccineReminderService.createReminder(petId, userId, vaccineName, nextDate, note);
        return Result.success(reminder);
    }

    /**
     * 获取提醒详情
     */
    @GetMapping("/{id}")
    public Result<VaccineReminder> getReminder(
            @PathVariable Long petId,
            @PathVariable Long id) {
        log.info("获取疫苗提醒详情：petId={}, id={}", petId, id);
        VaccineReminder reminder = vaccineReminderService.getReminderById(id);
        return Result.success(reminder);
    }

    /**
     * 更新提醒信息（通用编辑接口）
     */
    @PutMapping("/{id}")
    public Result<VaccineReminder> updateReminder(
            @PathVariable Long petId,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> requestBody) {
        String vaccineName = (String) requestBody.get("vaccineName");
        String nextDateStr = (String) requestBody.get("nextDate");
        LocalDate nextDate = nextDateStr != null ? LocalDate.parse(nextDateStr) : null;
        String note = requestBody.get("note") != null ? requestBody.get("note").toString() : null;

        log.info("更新疫苗提醒：petId={}, id={}, vaccineName={}, nextDate={}, note={}", petId, id, vaccineName, nextDate, note);
        VaccineReminder reminder = vaccineReminderService.updateReminder(id, vaccineName, nextDate, note);
        return Result.success(reminder);
    }

    /**
     * 更新提醒状态
     */
    @PutMapping("/{id}/status")
    public Result<VaccineReminder> updateStatus(
            @PathVariable Long petId,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Integer> requestBody) {
        Integer status = requestBody.get("status");
        log.info("更新提醒状态：id={}, status={}", id, status);
        VaccineReminder reminder = vaccineReminderService.updateStatus(id, status);
        return Result.success(reminder);
    }

    /**
     * 更新提醒日期
     */
    @PutMapping("/{id}/next-date")
    public Result<VaccineReminder> updateNextDate(
            @PathVariable Long petId,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> requestBody) {
        String nextDateStr = requestBody.get("nextDate");
        LocalDate nextDate = nextDateStr != null ? LocalDate.parse(nextDateStr) : null;
        log.info("更新提醒日期：id={}, nextDate={}", id, nextDate);
        VaccineReminder reminder = vaccineReminderService.updateNextDate(id, nextDate);
        return Result.success(reminder);
    }

    /**
     * 删除提醒
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteReminder(
            @PathVariable Long petId,
            @PathVariable Long id) {
        log.info("删除疫苗提醒：id={}", id);
        vaccineReminderService.deleteReminder(id);
        return Result.success();
    }
}

package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.ParasiteReminder;
import com.pettrail.pettrailbackend.service.ParasiteReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pets/{petId}/parasite-reminders")
@RequiredArgsConstructor
public class ParasiteReminderController {

    private final ParasiteReminderService parasiteReminderService;

    /**
     * 获取宠物的寄生虫提醒列表
     */
    @GetMapping
    public Result<List<ParasiteReminder>> listReminders(@PathVariable Long petId) {
        log.info("获取寄生虫提醒列表：petId={}", petId);
        List<ParasiteReminder> reminders = parasiteReminderService.listByPetId(petId);
        return Result.success(reminders);
    }

    /**
     * 获取即将到期的寄生虫提醒
     */
    @GetMapping("/upcoming")
    public Result<List<ParasiteReminder>> listUpcoming(@PathVariable Long petId) {
        log.info("获取即将到期的寄生虫提醒：petId={}", petId);
        List<ParasiteReminder> reminders = parasiteReminderService.listUpcoming(petId);
        return Result.success(reminders);
    }

    /**
     * 创建寄生虫提醒
     */
    @PostMapping
    public Result<ParasiteReminder> createReminder(
            @PathVariable Long petId,
            @RequestBody java.util.Map<String, Object> requestBody) {
        Integer type = (Integer) requestBody.get("type");
        String nextDateStr = (String) requestBody.get("nextDate");
        LocalDate nextDate = nextDateStr != null ? LocalDate.parse(nextDateStr) : null;
        
        log.info("创建寄生虫提醒：petId={}, type={}, nextDate={}", petId, type, nextDate);
        ParasiteReminder reminder = parasiteReminderService.createReminder(petId, type, nextDate);
        return Result.success(reminder);
    }

    /**
     * 获取提醒详情
     */
    @GetMapping("/{id}")
    public Result<ParasiteReminder> getReminder(
            @PathVariable Long petId,
            @PathVariable Long id) {
        log.info("获取寄生虫提醒详情：petId={}, id={}", petId, id);
        ParasiteReminder reminder = parasiteReminderService.getReminderById(id);
        return Result.success(reminder);
    }

    /**
     * 更新提醒状态
     */
    @PutMapping("/{id}/status")
    public Result<ParasiteReminder> updateStatus(
            @PathVariable Long petId,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Integer> requestBody) {
        Integer status = requestBody.get("status");
        log.info("更新提醒状态：id={}, status={}", id, status);
        ParasiteReminder reminder = parasiteReminderService.updateStatus(id, status);
        return Result.success(reminder);
    }

    /**
     * 更新提醒日期
     */
    @PutMapping("/{id}/next-date")
    public Result<ParasiteReminder> updateNextDate(
            @PathVariable Long petId,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> requestBody) {
        String nextDateStr = requestBody.get("nextDate");
        LocalDate nextDate = nextDateStr != null ? LocalDate.parse(nextDateStr) : null;
        log.info("更新提醒日期：id={}, nextDate={}", id, nextDate);
        ParasiteReminder reminder = parasiteReminderService.updateNextDate(id, nextDate);
        return Result.success(reminder);
    }

    /**
     * 删除提醒
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteReminder(
            @PathVariable Long petId,
            @PathVariable Long id) {
        log.info("删除寄生虫提醒：id={}", id);
        parasiteReminderService.deleteReminder(id);
        return Result.success();
    }
}

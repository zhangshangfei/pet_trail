package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.ParasiteReminder;
import com.pettrail.pettrailbackend.entity.VaccineReminder;
import com.pettrail.pettrailbackend.service.ReminderService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 提醒控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    /**
     * 创建疫苗提醒
     */
    @PostMapping("/vaccine")
    public Result<VaccineReminder> createVaccineReminder(
            @RequestParam Long petId,
            @RequestParam String vaccineName,
            @RequestParam(required = false) Integer vaccineType,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate nextDate,
            @RequestParam(required = false) Integer reminderDays) {
        
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        VaccineReminder reminder = reminderService.createVaccineReminder(
            petId, userId, vaccineName, vaccineType, nextDate, reminderDays);
        return Result.success(reminder);
    }

    /**
     * 创建驱虫提醒
     */
    @PostMapping("/parasite")
    public Result<ParasiteReminder> createParasiteReminder(
            @RequestParam Long petId,
            @RequestParam Integer type,
            @RequestParam(required = false) String productName,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate nextDate,
            @RequestParam(required = false) Integer intervalDays) {
        
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        ParasiteReminder reminder = reminderService.createParasiteReminder(
            petId, userId, type, productName, nextDate, intervalDays);
        return Result.success(reminder);
    }

    /**
     * 完成疫苗提醒
     */
    @PostMapping("/vaccine/{id}/complete")
    public Result<Void> completeVaccineReminder(@PathVariable Long id) {
        reminderService.completeVaccineReminder(id);
        return Result.success();
    }

    /**
     * 完成驱虫提醒
     */
    @PostMapping("/parasite/{id}/complete")
    public Result<Void> completeParasiteReminder(@PathVariable Long id) {
        reminderService.completeParasiteReminder(id);
        return Result.success();
    }

    /**
     * 获取疫苗提醒列表
     */
    @GetMapping("/vaccine")
    public Result<List<VaccineReminder>> getVaccineReminders(
            @RequestParam(required = false) Long petId,
            @RequestParam(required = false) Integer status) {
        
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        // TODO: 实现查询逻辑
        return Result.success(null);
    }

    /**
     * 获取驱虫提醒列表
     */
    @GetMapping("/parasite")
    public Result<List<ParasiteReminder>> getParasiteReminders(
            @RequestParam(required = false) Long petId,
            @RequestParam(required = false) Integer status) {
        
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        // TODO: 实现查询逻辑
        return Result.success(null);
    }
}

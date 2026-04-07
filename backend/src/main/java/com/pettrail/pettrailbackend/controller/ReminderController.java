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
    public Result<VaccineReminder> createVaccineReminder(@RequestBody java.util.Map<String, Object> requestBody) {
        Long petId = requestBody.get("petId") != null ? Long.parseLong(requestBody.get("petId").toString()) : null;
        String vaccineName = (String) requestBody.get("vaccineName");
        
        Integer vaccineType = null;
        if (requestBody.get("vaccineType") != null) {
            vaccineType = Integer.parseInt(requestBody.get("vaccineType").toString());
        }
        
        LocalDate nextDate = null;
        if (requestBody.get("nextDate") != null) {
            nextDate = LocalDate.parse(requestBody.get("nextDate").toString());
        }
        
        Integer reminderDays = null;
        if (requestBody.get("reminderDays") != null) {
            reminderDays = Integer.parseInt(requestBody.get("reminderDays").toString());
        }

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        if (petId == null || vaccineName == null || nextDate == null) {
            return Result.error(400, "宠物ID、疫苗名称和下次接种日期不能为空");
        }

        VaccineReminder reminder = reminderService.createVaccineReminder(
            petId, userId, vaccineName, vaccineType, nextDate, reminderDays);
        return Result.success(reminder);
    }

    /**
     * 创建驱虫提醒
     */
    @PostMapping("/parasite")
    public Result<ParasiteReminder> createParasiteReminder(@RequestBody java.util.Map<String, Object> requestBody) {
        Long petId = requestBody.get("petId") != null ? Long.parseLong(requestBody.get("petId").toString()) : null;
        Integer type = requestBody.get("type") != null ? Integer.parseInt(requestBody.get("type").toString()) : null;
        
        String productName = (String) requestBody.get("productName");
        
        LocalDate nextDate = null;
        if (requestBody.get("nextDate") != null) {
            nextDate = LocalDate.parse(requestBody.get("nextDate").toString());
        }
        
        Integer intervalDays = null;
        if (requestBody.get("intervalDays") != null) {
            intervalDays = Integer.parseInt(requestBody.get("intervalDays").toString());
        }

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        if (petId == null || type == null || nextDate == null) {
            return Result.error(400, "宠物ID、类型和下次驱虫日期不能为空");
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

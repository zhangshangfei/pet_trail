package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.ParasiteReminder;
import com.pettrail.pettrailbackend.entity.VaccineReminder;
import com.pettrail.pettrailbackend.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController extends BaseController {

    private final ReminderService reminderService;

    @PostMapping("/vaccine")
    public Result<VaccineReminder> createVaccineReminder(@RequestBody java.util.Map<String, Object> requestBody) {
        Long userId = requireLogin();
        Long petId = requestBody.get("petId") != null ? Long.parseLong(requestBody.get("petId").toString()) : null;
        String vaccineName = (String) requestBody.get("vaccineName");
        Integer vaccineType = requestBody.get("vaccineType") != null ? Integer.parseInt(requestBody.get("vaccineType").toString()) : null;
        LocalDate nextDate = requestBody.get("nextDate") != null ? LocalDate.parse(requestBody.get("nextDate").toString()) : null;
        Integer reminderDays = requestBody.get("reminderDays") != null ? Integer.parseInt(requestBody.get("reminderDays").toString()) : null;

        if (petId == null || vaccineName == null || nextDate == null) {
            return Result.error(400, "宠物ID、疫苗名称和下次接种日期不能为空");
        }

        return Result.success(reminderService.createVaccineReminder(petId, userId, vaccineName, vaccineType, nextDate, reminderDays));
    }

    @PostMapping("/parasite")
    public Result<ParasiteReminder> createParasiteReminder(@RequestBody java.util.Map<String, Object> requestBody) {
        Long userId = requireLogin();
        Long petId = requestBody.get("petId") != null ? Long.parseLong(requestBody.get("petId").toString()) : null;
        Integer type = requestBody.get("type") != null ? Integer.parseInt(requestBody.get("type").toString()) : null;
        String productName = (String) requestBody.get("productName");
        LocalDate nextDate = requestBody.get("nextDate") != null ? LocalDate.parse(requestBody.get("nextDate").toString()) : null;
        Integer intervalDays = requestBody.get("intervalDays") != null ? Integer.parseInt(requestBody.get("intervalDays").toString()) : null;

        if (petId == null || type == null || nextDate == null) {
            return Result.error(400, "宠物ID、类型和下次驱虫日期不能为空");
        }

        return Result.success(reminderService.createParasiteReminder(petId, userId, type, productName, nextDate, intervalDays));
    }

    @PostMapping("/vaccine/{id}/complete")
    public Result<Void> completeVaccineReminder(@PathVariable Long id) {
        reminderService.completeVaccineReminder(id);
        return Result.success();
    }

    @PostMapping("/parasite/{id}/complete")
    public Result<Void> completeParasiteReminder(@PathVariable Long id) {
        reminderService.completeParasiteReminder(id);
        return Result.success();
    }

    @GetMapping("/vaccine")
    public Result<List<VaccineReminder>> getVaccineReminders(
            @RequestParam(required = false) Long petId,
            @RequestParam(required = false) Integer status) {
        requireLogin();
        return Result.success(null);
    }

    @GetMapping("/parasite")
    public Result<List<ParasiteReminder>> getParasiteReminders(
            @RequestParam(required = false) Long petId,
            @RequestParam(required = false) Integer status) {
        requireLogin();
        return Result.success(null);
    }
}

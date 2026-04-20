package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.ParasiteReminderDTO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.dto.VaccineReminderDTO;
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
    public Result<VaccineReminder> createVaccineReminder(@RequestBody VaccineReminderDTO dto) {
        Long userId = requireLogin();
        Long petId = dto.getPetId();
        String vaccineName = dto.getVaccineName();
        Integer vaccineType = dto.getVaccineType();
        LocalDate nextDate = dto.getNextDate() != null ? LocalDate.parse(dto.getNextDate()) : null;
        Integer reminderDays = dto.getReminderDays();

        if (petId == null || vaccineName == null || nextDate == null) {
            return Result.error(400, "宠物ID、疫苗名称和下次接种日期不能为空");
        }

        return Result.success(reminderService.createVaccineReminder(petId, userId, vaccineName, vaccineType, nextDate, reminderDays));
    }

    @PostMapping("/parasite")
    public Result<ParasiteReminder> createParasiteReminder(@RequestBody ParasiteReminderDTO dto) {
        Long userId = requireLogin();
        Long petId = dto.getPetId();
        Integer type = dto.getType();
        String productName = dto.getProductName();
        LocalDate nextDate = dto.getNextDate() != null ? LocalDate.parse(dto.getNextDate()) : null;
        Integer intervalDays = dto.getIntervalDays();

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

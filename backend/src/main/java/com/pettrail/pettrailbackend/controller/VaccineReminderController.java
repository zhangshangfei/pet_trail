package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.ReminderNextDateDTO;
import com.pettrail.pettrailbackend.dto.ReminderStatusDTO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.dto.VaccineReminderDTO;
import com.pettrail.pettrailbackend.entity.VaccineReminder;
import com.pettrail.pettrailbackend.service.VaccineReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pets/{petId}/vaccine-reminders")
@RequiredArgsConstructor
public class VaccineReminderController extends BaseController {

    private final VaccineReminderService vaccineReminderService;

    @GetMapping
    public Result<List<VaccineReminder>> listReminders(@PathVariable Long petId) {
        return Result.success(vaccineReminderService.listByPetId(petId));
    }

    @GetMapping("/upcoming")
    public Result<List<VaccineReminder>> listUpcoming(@PathVariable Long petId) {
        return Result.success(vaccineReminderService.listUpcoming(petId));
    }

    @PostMapping
    public Result<VaccineReminder> createReminder(
            @PathVariable Long petId,
            @RequestBody VaccineReminderDTO dto) {
        Long userId = requireLogin();
        String vaccineName = dto.getVaccineName();
        String nextDateStr = dto.getNextDate();
        LocalDate nextDate = nextDateStr != null ? LocalDate.parse(nextDateStr) : null;
        String note = dto.getNote();
        return Result.success(vaccineReminderService.createReminder(petId, userId, vaccineName, nextDate, note));
    }

    @GetMapping("/{id}")
    public Result<VaccineReminder> getReminder(@PathVariable Long petId, @PathVariable Long id) {
        return Result.success(vaccineReminderService.getReminderById(id));
    }

    @PutMapping("/{id}")
    public Result<VaccineReminder> updateReminder(
            @PathVariable Long petId, @PathVariable Long id,
            @RequestBody VaccineReminderDTO dto) {
        String vaccineName = dto.getVaccineName();
        String nextDateStr = dto.getNextDate();
        LocalDate nextDate = nextDateStr != null ? LocalDate.parse(nextDateStr) : null;
        String note = dto.getNote();
        return Result.success(vaccineReminderService.updateReminder(id, vaccineName, nextDate, note));
    }

    @PutMapping("/{id}/status")
    public Result<VaccineReminder> updateStatus(
            @PathVariable Long petId, @PathVariable Long id,
            @RequestBody ReminderStatusDTO dto) {
        return Result.success(vaccineReminderService.updateStatus(id, dto.getStatus()));
    }

    @PutMapping("/{id}/next-date")
    public Result<VaccineReminder> updateNextDate(
            @PathVariable Long petId, @PathVariable Long id,
            @RequestBody ReminderNextDateDTO dto) {
        String nextDateStr = dto.getNextDate();
        LocalDate nextDate = nextDateStr != null ? LocalDate.parse(nextDateStr) : null;
        return Result.success(vaccineReminderService.updateNextDate(id, nextDate));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReminder(@PathVariable Long petId, @PathVariable Long id) {
        vaccineReminderService.deleteReminder(id);
        return Result.success();
    }
}

package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.ParasiteReminderDTO;
import com.pettrail.pettrailbackend.dto.ReminderNextDateDTO;
import com.pettrail.pettrailbackend.dto.ReminderStatusDTO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.ParasiteReminder;
import com.pettrail.pettrailbackend.service.ParasiteReminderService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pets/{petId}/parasite-reminders")
@RequiredArgsConstructor
public class ParasiteReminderController extends BaseController {

    private final ParasiteReminderService parasiteReminderService;

    @GetMapping
    public Result<List<ParasiteReminder>> listReminders(@PathVariable Long petId) {
        return Result.success(parasiteReminderService.listByPetId(petId));
    }

    @GetMapping("/upcoming")
    public Result<List<ParasiteReminder>> listUpcoming(@PathVariable Long petId) {
        return Result.success(parasiteReminderService.listUpcoming(petId));
    }

    @PostMapping
    public Result<ParasiteReminder> createReminder(
            @PathVariable Long petId,
            @RequestBody ParasiteReminderDTO dto) {
        Long userId = requireLogin();
        Integer type = dto.getType();
        String nextDateStr = dto.getNextDate();
        LocalDate nextDate = nextDateStr != null ? LocalDate.parse(nextDateStr) : null;
        String productName = dto.getProductName();
        String note = dto.getNote();
        return Result.success(parasiteReminderService.createReminder(petId, userId, type, nextDate, productName, note));
    }

    @GetMapping("/{id}")
    public Result<ParasiteReminder> getReminder(@PathVariable Long petId, @PathVariable Long id) {
        return Result.success(parasiteReminderService.getReminderById(id));
    }

    @PutMapping("/{id}")
    public Result<ParasiteReminder> updateReminder(
            @PathVariable Long petId, @PathVariable Long id,
            @RequestBody ParasiteReminderDTO dto) {
        Integer type = dto.getType();
        String nextDateStr = dto.getNextDate();
        LocalDate nextDate = nextDateStr != null ? LocalDate.parse(nextDateStr) : null;
        String note = dto.getNote();
        return Result.success(parasiteReminderService.updateReminder(id, type, nextDate, note));
    }

    @PutMapping("/{id}/status")
    public Result<ParasiteReminder> updateStatus(
            @PathVariable Long petId, @PathVariable Long id,
            @RequestBody ReminderStatusDTO dto) {
        return Result.success(parasiteReminderService.updateStatus(id, dto.getStatus()));
    }

    @PutMapping("/{id}/next-date")
    public Result<ParasiteReminder> updateNextDate(
            @PathVariable Long petId, @PathVariable Long id,
            @RequestBody ReminderNextDateDTO dto) {
        String nextDateStr = dto.getNextDate();
        LocalDate nextDate = nextDateStr != null ? LocalDate.parse(nextDateStr) : null;
        return Result.success(parasiteReminderService.updateNextDate(id, nextDate));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReminder(@PathVariable Long petId, @PathVariable Long id) {
        parasiteReminderService.deleteReminder(id);
        return Result.success();
    }
}

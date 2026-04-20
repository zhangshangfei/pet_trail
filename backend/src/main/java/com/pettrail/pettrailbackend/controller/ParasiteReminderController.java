package com.pettrail.pettrailbackend.controller;

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
            @RequestBody java.util.Map<String, Object> requestBody) {
        Long userId = requireLogin();
        Integer type = requestBody.get("type") != null ? ((Number) requestBody.get("type")).intValue() : null;
        String nextDateStr = (String) requestBody.get("nextDate");
        LocalDate nextDate = nextDateStr != null ? LocalDate.parse(nextDateStr) : null;
        String note = requestBody.get("note") != null ? requestBody.get("note").toString() : null;
        return Result.success(parasiteReminderService.createReminder(petId, userId, type, nextDate, note));
    }

    @GetMapping("/{id}")
    public Result<ParasiteReminder> getReminder(@PathVariable Long petId, @PathVariable Long id) {
        return Result.success(parasiteReminderService.getReminderById(id));
    }

    @PutMapping("/{id}")
    public Result<ParasiteReminder> updateReminder(
            @PathVariable Long petId, @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> requestBody) {
        Integer type = requestBody.get("type") != null ? ((Number) requestBody.get("type")).intValue() : null;
        String nextDateStr = (String) requestBody.get("nextDate");
        LocalDate nextDate = nextDateStr != null ? LocalDate.parse(nextDateStr) : null;
        String note = requestBody.get("note") != null ? requestBody.get("note").toString() : null;
        return Result.success(parasiteReminderService.updateReminder(id, type, nextDate, note));
    }

    @PutMapping("/{id}/status")
    public Result<ParasiteReminder> updateStatus(
            @PathVariable Long petId, @PathVariable Long id,
            @RequestBody java.util.Map<String, Integer> requestBody) {
        return Result.success(parasiteReminderService.updateStatus(id, requestBody.get("status")));
    }

    @PutMapping("/{id}/next-date")
    public Result<ParasiteReminder> updateNextDate(
            @PathVariable Long petId, @PathVariable Long id,
            @RequestBody java.util.Map<String, String> requestBody) {
        String nextDateStr = requestBody.get("nextDate");
        LocalDate nextDate = nextDateStr != null ? LocalDate.parse(nextDateStr) : null;
        return Result.success(parasiteReminderService.updateNextDate(id, nextDate));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReminder(@PathVariable Long petId, @PathVariable Long id) {
        parasiteReminderService.deleteReminder(id);
        return Result.success();
    }
}

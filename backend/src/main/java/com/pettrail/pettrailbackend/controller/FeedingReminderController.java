package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.FeedingReminderDTO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.FeedingReminder;
import com.pettrail.pettrailbackend.service.FeedingReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/feeding-reminders")
@RequiredArgsConstructor
public class FeedingReminderController extends BaseController {

    private final FeedingReminderService feedingReminderService;

    @GetMapping
    public Result<List<FeedingReminder>> getReminders(@RequestParam(required = false) Long petId) {
        Long userId = requireLogin();
        List<FeedingReminder> reminders = petId != null
                ? feedingReminderService.getPetReminders(userId, petId)
                : feedingReminderService.getUserReminders(userId);
        return Result.success(reminders);
    }

    @PostMapping
    public Result<FeedingReminder> createReminder(@RequestBody FeedingReminderDTO dto) {
        Long userId = requireLogin();
        Long petId = dto.getPetId();
        String mealType = dto.getMealType() != null ? dto.getMealType() : "breakfast";
        String time = dto.getTime();
        String repeat = dto.getRepeat() != null ? dto.getRepeat() : "daily";
        String note = dto.getNote();

        if (time == null || time.isEmpty()) {
            return Result.error(400, "请选择提醒时间");
        }
        if (petId == null) {
            return Result.error(400, "请选择宠物");
        }

        return Result.success(feedingReminderService.createReminder(userId, petId, mealType, time, repeat, note));
    }

    @PutMapping("/{id}")
    public Result<FeedingReminder> updateReminder(@PathVariable Long id, @RequestBody FeedingReminderDTO dto) {
        Long userId = requireLogin();
        String mealType = dto.getMealType();
        String time = dto.getTime();
        String repeat = dto.getRepeat();
        String note = dto.getNote();
        return Result.success(feedingReminderService.updateReminder(userId, id, mealType, time, repeat, note));
    }

    @PutMapping("/{id}/toggle")
    public Result<FeedingReminder> toggleReminder(@PathVariable Long id) {
        Long userId = requireLogin();
        return Result.success(feedingReminderService.toggleReminder(userId, id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReminder(@PathVariable Long id) {
        Long userId = requireLogin();
        feedingReminderService.deleteReminder(userId, id);
        return Result.success();
    }
}

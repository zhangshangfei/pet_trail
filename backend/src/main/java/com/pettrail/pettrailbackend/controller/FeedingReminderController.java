package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.FeedingReminder;
import com.pettrail.pettrailbackend.service.FeedingReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public Result<FeedingReminder> createReminder(@RequestBody Map<String, Object> body) {
        Long userId = requireLogin();
        Long petId = body.get("petId") != null ? Long.parseLong(body.get("petId").toString()) : null;
        String mealType = body.get("mealType") != null ? body.get("mealType").toString() : "breakfast";
        String time = body.get("time") != null ? body.get("time").toString() : null;
        String repeat = body.get("repeat") != null ? body.get("repeat").toString() : "daily";
        String note = body.get("note") != null ? body.get("note").toString() : null;

        if (time == null || time.isEmpty()) {
            return Result.error(400, "请选择提醒时间");
        }
        if (petId == null) {
            return Result.error(400, "请选择宠物");
        }

        return Result.success(feedingReminderService.createReminder(userId, petId, mealType, time, repeat, note));
    }

    @PutMapping("/{id}")
    public Result<FeedingReminder> updateReminder(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = requireLogin();
        String mealType = body.get("mealType") != null ? body.get("mealType").toString() : null;
        String time = body.get("time") != null ? body.get("time").toString() : null;
        String repeat = body.get("repeat") != null ? body.get("repeat").toString() : null;
        String note = body.get("note") != null ? body.get("note").toString() : null;
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

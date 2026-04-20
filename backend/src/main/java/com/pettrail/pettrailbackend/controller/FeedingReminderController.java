package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.FeedingReminder;
import com.pettrail.pettrailbackend.service.FeedingReminderService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/feeding-reminders")
@RequiredArgsConstructor
public class FeedingReminderController {

    private final FeedingReminderService feedingReminderService;

    @GetMapping
    public Result<List<FeedingReminder>> getReminders(
            @RequestParam(required = false) Long petId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        List<FeedingReminder> reminders;
        if (petId != null) {
            reminders = feedingReminderService.getPetReminders(userId, petId);
        } else {
            reminders = feedingReminderService.getUserReminders(userId);
        }
        return Result.success(reminders);
    }

    @PostMapping
    public Result<FeedingReminder> createReminder(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
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

        try {
            FeedingReminder reminder = feedingReminderService.createReminder(
                userId, petId, mealType, time, repeat, note);
            return Result.success(reminder);
        } catch (Exception e) {
            log.error("创建喂食提醒失败: {}", e.getMessage(), e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<FeedingReminder> updateReminder(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        String mealType = body.get("mealType") != null ? body.get("mealType").toString() : null;
        String time = body.get("time") != null ? body.get("time").toString() : null;
        String repeat = body.get("repeat") != null ? body.get("repeat").toString() : null;
        String note = body.get("note") != null ? body.get("note").toString() : null;

        try {
            FeedingReminder reminder = feedingReminderService.updateReminder(
                userId, id, mealType, time, repeat, note);
            return Result.success(reminder);
        } catch (Exception e) {
            log.error("更新喂食提醒失败: {}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle")
    public Result<FeedingReminder> toggleReminder(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        try {
            FeedingReminder reminder = feedingReminderService.toggleReminder(userId, id);
            return Result.success(reminder);
        } catch (Exception e) {
            log.error("切换喂食提醒状态失败: {}", e.getMessage(), e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReminder(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        try {
            feedingReminderService.deleteReminder(userId, id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除喂食提醒失败: {}", e.getMessage(), e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}

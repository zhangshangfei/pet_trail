package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.CheckinReportVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.CheckinReminder;
import com.pettrail.pettrailbackend.service.CheckinReminderService;
import com.pettrail.pettrailbackend.service.CheckinService;
import com.pettrail.pettrailbackend.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
@Tag(name = "打卡", description = "打卡相关接口")
public class CheckinExtController {

    private final CheckinReminderService checkinReminderService;
    private final CheckinService checkinService;

    @GetMapping("/report")
    @Operation(summary = "获取打卡报表", description = "获取周报或月报数据")
    public Result<CheckinReportVO> getReport(@RequestParam(defaultValue = "week") String period) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        Map<String, Object> stats = checkinService.getUserStats(userId);
        CheckinReportVO report = new CheckinReportVO();
        report.setPeriod(period);
        report.setTotalCheckins(stats.get("totalCount") != null ? Integer.parseInt(stats.get("totalCount").toString()) : 0);
        report.setCurrentStreak(stats.get("currentStreak") != null ? Integer.parseInt(stats.get("currentStreak").toString()) : 0);
        report.setMaxStreak(stats.get("maxStreak") != null ? Integer.parseInt(stats.get("maxStreak").toString()) : 0);

        if ("week".equals(period)) {
            report.setTotalDays(7);
            int activeDays = stats.get("activeDays7") != null ? Integer.parseInt(stats.get("activeDays7").toString()) : 0;
            report.setCheckinDays(activeDays);
        } else {
            report.setTotalDays(30);
            int activeDays = stats.get("activeDays30") != null ? Integer.parseInt(stats.get("activeDays30").toString()) : 0;
            report.setCheckinDays(activeDays);
        }

        report.setCompletionRate(report.getTotalDays() > 0
            ? Math.round((double) report.getCheckinDays() / report.getTotalDays() * 1000.0) / 10.0
            : 0.0);

        @SuppressWarnings("unchecked")
        Map<String, Integer> itemStats = stats.get("itemStats") != null
            ? (Map<String, Integer>) stats.get("itemStats")
            : Map.of();
        report.setItemStats(itemStats);

        return Result.success(report);
    }

    @GetMapping("/reminders")
    @Operation(summary = "获取打卡提醒列表", description = "获取当前用户的打卡提醒设置")
    public Result<List<CheckinReminder>> getReminders() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        List<CheckinReminder> reminders = checkinReminderService.getUserReminders(userId);
        return Result.success(reminders);
    }

    @PostMapping("/reminders")
    @Operation(summary = "创建打卡提醒", description = "创建新的打卡提醒")
    public Result<CheckinReminder> createReminder(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        Long itemId = body.get("itemId") != null ? Long.valueOf(body.get("itemId").toString()) : null;
        String timeStr = (String) body.get("remindTime");
        LocalTime remindTime = LocalTime.parse(timeStr);

        CheckinReminder reminder = checkinReminderService.createReminder(userId, itemId, remindTime);
        return Result.success(reminder);
    }

    @PutMapping("/reminders/{id}")
    @Operation(summary = "更新打卡提醒", description = "更新打卡提醒设置")
    public Result<CheckinReminder> updateReminder(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        Long itemId = body.get("itemId") != null ? Long.valueOf(body.get("itemId").toString()) : null;
        LocalTime remindTime = body.get("remindTime") != null ? LocalTime.parse((String) body.get("remindTime")) : null;
        Boolean isEnabled = body.get("isEnabled") != null ? Boolean.valueOf(body.get("isEnabled").toString()) : null;

        CheckinReminder reminder = checkinReminderService.updateReminder(id, userId, itemId, remindTime, isEnabled);
        if (reminder == null) {
            return Result.error("提醒不存在或无权限");
        }
        return Result.success(reminder);
    }

    @DeleteMapping("/reminders/{id}")
    @Operation(summary = "删除打卡提醒", description = "删除打卡提醒")
    public Result<Void> deleteReminder(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        checkinReminderService.deleteReminder(id, userId);
        return Result.success(null);
    }
}

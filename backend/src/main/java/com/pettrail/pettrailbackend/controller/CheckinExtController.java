package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.CheckinReminderDTO;
import com.pettrail.pettrailbackend.dto.CheckinReminderUpdateDTO;
import com.pettrail.pettrailbackend.dto.CheckinReportVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.CheckinReminder;
import com.pettrail.pettrailbackend.service.CheckinReminderService;
import com.pettrail.pettrailbackend.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
@Tag(name = "打卡", description = "打卡相关接口")
public class CheckinExtController extends BaseController {

    private final CheckinReminderService checkinReminderService;
    private final CheckinService checkinService;

    @GetMapping("/report")
    @Operation(summary = "获取打卡报表")
    public Result<CheckinReportVO> getReport(@RequestParam(defaultValue = "week") String period) {
        Long userId = requireLogin();
        Map<String, Object> stats = checkinService.getUserStats(userId);
        CheckinReportVO report = new CheckinReportVO();
        report.setPeriod(period);
        report.setTotalCheckins(stats.get("totalCount") != null ? Integer.parseInt(stats.get("totalCount").toString()) : 0);
        report.setCurrentStreak(stats.get("currentStreak") != null ? Integer.parseInt(stats.get("currentStreak").toString()) : 0);
        report.setMaxStreak(stats.get("maxStreak") != null ? Integer.parseInt(stats.get("maxStreak").toString()) : 0);

        if ("week".equals(period)) {
            LocalDate today = LocalDate.now();
            LocalDate weekStart = today.with(DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M月d日");
            report.setStartDate(weekStart.format(fmt));
            report.setEndDate(weekEnd.format(fmt));
            report.setTotalDays(7);
            report.setCheckinDays(stats.get("weekDays") != null ? Integer.parseInt(stats.get("weekDays").toString()) : 0);
        } else {
            LocalDate today = LocalDate.now();
            LocalDate monthStart = today.withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M月d日");
            report.setStartDate(monthStart.format(fmt));
            report.setEndDate(monthEnd.format(fmt));
            report.setTotalDays(monthEnd.getDayOfMonth());
            report.setCheckinDays(stats.get("monthDays") != null ? Integer.parseInt(stats.get("monthDays").toString()) : 0);
        }

        report.setCompletionRate(report.getTotalDays() > 0
            ? Math.round((double) report.getCheckinDays() / report.getTotalDays() * 1000.0) / 10.0
            : 0.0);

        List<Map<String, Object>> itemStats = stats.get("itemStats") != null
            ? (List<Map<String, Object>>) stats.get("itemStats")
            : List.of();
        report.setItemStats(itemStats);

        return Result.success(report);
    }

    @GetMapping("/reminders")
    @Operation(summary = "获取打卡提醒列表")
    public Result<List<CheckinReminder>> getReminders() {
        Long userId = requireLogin();
        return Result.success(checkinReminderService.getUserReminders(userId));
    }

    @PostMapping("/reminders")
    @Operation(summary = "创建打卡提醒")
    public Result<CheckinReminder> createReminder(@RequestBody CheckinReminderDTO dto) {
        Long userId = requireLogin();
        Long itemId = dto.getItemId();
        LocalTime remindTime = LocalTime.parse(dto.getRemindTime());
        return Result.success(checkinReminderService.createReminder(userId, itemId, remindTime));
    }

    @PutMapping("/reminders/{id}")
    @Operation(summary = "更新打卡提醒")
    public Result<CheckinReminder> updateReminder(@PathVariable Long id, @RequestBody CheckinReminderUpdateDTO dto) {
        Long userId = requireLogin();
        Long itemId = dto.getItemId();
        LocalTime remindTime = dto.getRemindTime() != null ? LocalTime.parse(dto.getRemindTime()) : null;
        Boolean isEnabled = dto.getIsEnabled();

        CheckinReminder reminder = checkinReminderService.updateReminder(id, userId, itemId, remindTime, isEnabled);
        if (reminder == null) {
            return Result.error("提醒不存在或无权限");
        }
        return Result.success(reminder);
    }

    @DeleteMapping("/reminders/{id}")
    @Operation(summary = "删除打卡提醒")
    public Result<Void> deleteReminder(@PathVariable Long id) {
        Long userId = requireLogin();
        checkinReminderService.deleteReminder(id, userId);
        return Result.success(null);
    }
}

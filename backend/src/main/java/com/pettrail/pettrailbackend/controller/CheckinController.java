package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.CheckinItem;
import com.pettrail.pettrailbackend.entity.CheckinRecord;
import com.pettrail.pettrailbackend.service.CheckinService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打卡控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    /**
     * 获取打卡项列表
     */
    @GetMapping("/items")
    public Result<List<CheckinItem>> getCheckinItems() {
        List<CheckinItem> items = checkinService.getCheckinItems();
        return Result.success(items);
    }

    /**
     * 打卡
     */
    @PostMapping
    public Result<CheckinRecord> checkin(@RequestBody java.util.Map<String, Object> requestBody) {
        log.info("打卡接口请求参数: {}", requestBody);

        Long itemId = requestBody.get("itemId") != null ? Long.parseLong(requestBody.get("itemId").toString()) : null;

        Long petId = null;
        if (requestBody.get("petId") != null) {
            petId = Long.parseLong(requestBody.get("petId").toString());
        }

        String note = requestBody.get("note") != null ? requestBody.get("note").toString() : null;

        @SuppressWarnings("unchecked")
        List<String> images = requestBody.get("images") != null ?
            (List<String>) requestBody.get("images") : null;

        Long userId = UserContext.getCurrentUserId();
        log.info("打卡接口解析参数: userId={}, petId={}, itemId={}, note={}", userId, petId, itemId, note);

        if (userId == null) {
            log.warn("打卡失败: 用户未登录");
            return Result.error(401, "用户未登录");
        }

        if (itemId == null) {
            log.warn("打卡失败: 打卡项ID为空, userId={}", userId);
            return Result.error(400, "打卡项ID不能为空");
        }

        try {
            CheckinRecord record = checkinService.checkin(userId, petId, itemId, note, images);
            log.info("打卡成功: userId={}, itemId={}, recordId={}", userId, itemId, record.getId());
            return Result.success(record);
        } catch (Exception e) {
            log.error("打卡异常: userId={}, petId={}, itemId={}, error={}", userId, petId, itemId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取打卡日历
     */
    @GetMapping("/calendar")
    public Result<List<CheckinRecord>> getCalendar(
            @RequestParam int year,
            @RequestParam int month) {
        
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        List<CheckinRecord> records = checkinService.getCalendar(userId, year, month);
        return Result.success(records);
    }

    /**
     * 取消打卡
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelCheckin(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        checkinService.cancelCheckin(userId, id);
        return Result.success();
    }

    /**
     * 获取用户打卡统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        // TODO: 实现统计接口
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", 0);
        stats.put("currentStreak", 0);
        stats.put("maxStreak", 0);
        
        return Result.success(stats);
    }
}

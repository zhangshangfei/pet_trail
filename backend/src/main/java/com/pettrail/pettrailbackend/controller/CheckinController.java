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
        Long userId = UserContext.getCurrentUserId();
        List<CheckinItem> items = checkinService.getUserCheckinItems(userId);
        return Result.success(items);
    }

    @PostMapping("/items")
    public Result<CheckinItem> createCustomItem(@RequestBody java.util.Map<String, Object> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        String name = body.get("name") != null ? body.get("name").toString() : null;
        String icon = body.get("icon") != null ? body.get("icon").toString() : null;
        Integer type = body.get("type") != null ? Integer.parseInt(body.get("type").toString()) : null;
        String description = body.get("description") != null ? body.get("description").toString() : null;

        if (name == null || name.trim().isEmpty()) {
            return Result.error(400, "打卡项名称不能为空");
        }
        try {
            CheckinItem item = checkinService.createCustomItem(userId, name, icon, type, description);
            return Result.success(item);
        } catch (Exception e) {
            log.error("创建自定义打卡项失败: {}", e.getMessage(), e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    @PutMapping("/items/{id}")
    public Result<CheckinItem> updateCustomItem(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        String name = body.get("name") != null ? body.get("name").toString() : null;
        String icon = body.get("icon") != null ? body.get("icon").toString() : null;
        Integer type = body.get("type") != null ? Integer.parseInt(body.get("type").toString()) : null;
        String description = body.get("description") != null ? body.get("description").toString() : null;

        try {
            CheckinItem item = checkinService.updateCustomItem(userId, id, name, icon, type, description);
            return Result.success(item);
        } catch (Exception e) {
            log.error("更新自定义打卡项失败: {}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> deleteCustomItem(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        try {
            checkinService.deleteCustomItem(userId, id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除自定义打卡项失败: {}", e.getMessage(), e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @PostMapping("/items/{id}/hide")
    public Result<Void> hideItem(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        try {
            checkinService.hideItem(userId, id);
            return Result.success();
        } catch (Exception e) {
            log.error("隐藏打卡项失败: {}", e.getMessage(), e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    @PostMapping("/items/{id}/show")
    public Result<Void> showItem(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        try {
            checkinService.showItem(userId, id);
            return Result.success();
        } catch (Exception e) {
            log.error("显示打卡项失败: {}", e.getMessage(), e);
            return Result.error("操作失败：" + e.getMessage());
        }
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

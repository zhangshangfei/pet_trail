package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.CheckinItemRequest;
import com.pettrail.pettrailbackend.dto.CheckinRequest;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.CheckinItem;
import com.pettrail.pettrailbackend.entity.CheckinRecord;
import com.pettrail.pettrailbackend.service.CheckinService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckinController extends BaseController {

    private final CheckinService checkinService;

    @GetMapping("/items")
    public Result<List<CheckinItem>> getCheckinItems() {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(checkinService.getUserCheckinItems(userId));
    }

    @PostMapping("/items")
    public Result<CheckinItem> createCustomItem(@RequestBody CheckinItemRequest request) {
        Long userId = requireLogin();
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return Result.error(400, "打卡项名称不能为空");
        }
        return Result.success(checkinService.createCustomItem(userId, request.getName(),
                request.getIcon(), request.getType(), request.getDescription()));
    }

    @PutMapping("/items/{id}")
    public Result<CheckinItem> updateCustomItem(@PathVariable Long id, @RequestBody CheckinItemRequest request) {
        Long userId = requireLogin();
        return Result.success(checkinService.updateCustomItem(userId, id, request.getName(),
                request.getIcon(), request.getType(), request.getDescription()));
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> deleteCustomItem(@PathVariable Long id) {
        Long userId = requireLogin();
        checkinService.deleteCustomItem(userId, id);
        return Result.success();
    }

    @PostMapping("/items/{id}/hide")
    public Result<Void> hideItem(@PathVariable Long id) {
        Long userId = requireLogin();
        checkinService.hideItem(userId, id);
        return Result.success();
    }

    @PostMapping("/items/{id}/show")
    public Result<Void> showItem(@PathVariable Long id) {
        Long userId = requireLogin();
        checkinService.showItem(userId, id);
        return Result.success();
    }

    @PostMapping
    public Result<CheckinRecord> checkin(@RequestBody CheckinRequest request) {
        Long userId = requireLogin();
        if (request.getItemId() == null) {
            return Result.error(400, "打卡项ID不能为空");
        }
        return Result.success(checkinService.checkin(userId, request.getPetId(),
                request.getItemId(), request.getNote(), request.getImages()));
    }

    @GetMapping("/calendar")
    public Result<List<CheckinRecord>> getCalendar(@RequestParam int year, @RequestParam int month) {
        Long userId = requireLogin();
        return Result.success(checkinService.getCalendar(userId, year, month));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancelCheckin(@PathVariable Long id) {
        Long userId = requireLogin();
        checkinService.cancelCheckin(userId, id);
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Long userId = requireLogin();
        return Result.success(checkinService.getUserStats(userId));
    }
}

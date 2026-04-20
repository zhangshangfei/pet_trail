package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.AchievementCheckDTO;
import com.pettrail.pettrailbackend.dto.AchievementVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.UserAchievement;
import com.pettrail.pettrailbackend.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
@Tag(name = "成就系统", description = "成就相关接口")
public class AchievementController extends BaseController {

    private final AchievementService achievementService;

    @GetMapping
    @Operation(summary = "获取用户成就列表")
    public Result<Map<String, Object>> listAchievements(
            @RequestParam(required = false) Integer type) {
        Long userId = requireLogin();
        List<AchievementVO> achievements = achievementService.getUserAchievements(userId);

        if (type != null) {
            achievements = achievements.stream()
                    .filter(a -> a.getType().equals(type))
                    .toList();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("achievements", achievements);
        result.put("unlockedCount", achievementService.getUnlockedCount(userId));
        result.put("totalCount", achievementService.getTotalCount());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取成就详情")
    public Result<AchievementVO> getAchievementDetail(@PathVariable Long id) {
        Long userId = requireLogin();
        AchievementVO vo = achievementService.getAchievementDetail(userId, id);
        if (vo == null) {
            return Result.error(404, "成就不存在");
        }
        return Result.success(vo);
    }

    @PostMapping("/{id}/claim")
    @Operation(summary = "领取已完成的成就奖励")
    public Result<UserAchievement> claimAchievement(@PathVariable Long id) {
        Long userId = requireLogin();
        return Result.success(achievementService.claimAchievement(userId, id));
    }

    @PostMapping("/check")
    @Operation(summary = "手动触发成就检查")
    public Result<Map<String, Object>> checkAchievements(@RequestBody AchievementCheckDTO dto) {
        Long userId = requireLogin();
        String conditionType = dto.getConditionType();
        boolean unlocked = achievementService.checkAndUnlock(userId, conditionType);

        Map<String, Object> result = new HashMap<>();
        result.put("newlyUnlocked", unlocked);
        result.put("unlockedCount", achievementService.getUnlockedCount(userId));
        return Result.success(result);
    }
}

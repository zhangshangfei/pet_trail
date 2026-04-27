package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Challenge;
import com.pettrail.pettrailbackend.entity.ChallengeParticipant;
import com.pettrail.pettrailbackend.mapper.ChallengeMapper;
import com.pettrail.pettrailbackend.mapper.ChallengeParticipantMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/challenges")
@RequiredArgsConstructor
@Tag(name = "Admin-挑战赛管理", description = "后台挑战赛配置管理")
public class AdminChallengeController extends BaseAdminController {

    private final ChallengeMapper challengeMapper;
    private final ChallengeParticipantMapper participantMapper;

    @GetMapping
    @Operation(summary = "分页查询挑战赛列表")
    public Result<Page<Challenge>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type) {
        Page<Challenge> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Challenge> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(Challenge::getStatus, status);
        if (type != null) wrapper.eq(Challenge::getType, type);
        wrapper.orderByDesc(Challenge::getCreatedAt);
        return Result.success(challengeMapper.selectPage(pageParam, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取挑战赛详情")
    public Result<Challenge> detail(@PathVariable Long id) {
        return Result.success(challengeMapper.selectById(id));
    }

    @PostMapping
    @Operation(summary = "创建挑战赛")
    public Result<Challenge> create(@RequestBody Challenge challenge) {
        challenge.setCreatedAt(LocalDateTime.now());
        challenge.setUpdatedAt(LocalDateTime.now());
        challenge.setParticipantCount(0);
        challengeMapper.insert(challenge);
        return Result.success(challenge);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新挑战赛")
    public Result<Challenge> update(@PathVariable Long id, @RequestBody Challenge challenge) {
        Challenge existing = challengeMapper.selectById(id);
        if (existing == null) return Result.error(404, "挑战赛不存在");
        challenge.setId(id);
        challenge.setUpdatedAt(LocalDateTime.now());
        challengeMapper.updateById(challenge);
        return Result.success(challenge);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除挑战赛")
    public Result<String> delete(@PathVariable Long id) {
        challengeMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新挑战赛状态")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Challenge challenge = challengeMapper.selectById(id);
        if (challenge == null) return Result.error(404, "挑战赛不存在");
        challenge.setStatus(body.get("status"));
        challenge.setUpdatedAt(LocalDateTime.now());
        challengeMapper.updateById(challenge);
        return Result.success("状态更新成功");
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "获取挑战赛统计")
    public Result<Map<String, Object>> stats(@PathVariable Long id) {
        Map<String, Object> stats = new HashMap<>();
        long totalParticipants = participantMapper.selectCount(
                new LambdaQueryWrapper<ChallengeParticipant>().eq(ChallengeParticipant::getChallengeId, id));
        long completedCount = participantMapper.selectCount(
                new LambdaQueryWrapper<ChallengeParticipant>().eq(ChallengeParticipant::getChallengeId, id).eq(ChallengeParticipant::getCompleted, true));
        stats.put("totalParticipants", totalParticipants);
        stats.put("completedCount", completedCount);
        stats.put("completionRate", totalParticipants > 0 ? String.format("%.1f%%", completedCount * 100.0 / totalParticipants) : "0%");
        return Result.success(stats);
    }

    @GetMapping("/{id}/participants")
    @Operation(summary = "获取挑战赛参与列表")
    public Result<Page<ChallengeParticipant>> participants(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ChallengeParticipant> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ChallengeParticipant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChallengeParticipant::getChallengeId, id);
        wrapper.orderByDesc(ChallengeParticipant::getProgress);
        return Result.success(participantMapper.selectPage(pageParam, wrapper));
    }
}

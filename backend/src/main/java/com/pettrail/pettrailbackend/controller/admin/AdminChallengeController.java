package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Challenge;
import com.pettrail.pettrailbackend.entity.ChallengeParticipant;
import com.pettrail.pettrailbackend.service.ChallengeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/challenges")
@RequiredArgsConstructor
@Tag(name = "Admin-挑战赛管理", description = "后台挑战赛配置管理")
public class AdminChallengeController extends BaseAdminController {

    private final ChallengeService challengeService;

    @GetMapping
    @Operation(summary = "分页查询挑战赛列表")
    public Result<Page<Challenge>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type) {
        return Result.success(challengeService.adminListChallenges(page, size, status, type));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取挑战赛详情")
    public Result<Challenge> detail(@PathVariable Long id) {
        return Result.success(challengeService.getChallengeDetail(id));
    }

    @PostMapping
    @Operation(summary = "创建挑战赛")
    public Result<Challenge> create(@RequestBody Challenge challenge) {
        return Result.success(challengeService.adminCreateChallenge(challenge));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新挑战赛")
    public Result<Challenge> update(@PathVariable Long id, @RequestBody Challenge challenge) {
        return Result.success(challengeService.adminUpdateChallenge(id, challenge));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除挑战赛")
    public Result<String> delete(@PathVariable Long id) {
        challengeService.adminDeleteChallenge(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新挑战赛状态")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        challengeService.adminUpdateChallengeStatus(id, body.get("status"));
        return Result.success("状态更新成功");
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "获取挑战赛统计")
    public Result<Map<String, Object>> stats(@PathVariable Long id) {
        return Result.success(challengeService.adminGetChallengeStats(id));
    }

    @GetMapping("/{id}/participants")
    @Operation(summary = "获取挑战赛参与列表")
    public Result<Page<ChallengeParticipant>> participants(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(challengeService.adminListParticipants(id, page, size));
    }
}

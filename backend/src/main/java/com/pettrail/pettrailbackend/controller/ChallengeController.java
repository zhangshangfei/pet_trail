package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Challenge;
import com.pettrail.pettrailbackend.entity.ChallengeParticipant;
import com.pettrail.pettrailbackend.service.ChallengeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/challenges")
@Tag(name = "挑战赛", description = "挑战赛相关接口")
@RequiredArgsConstructor
public class ChallengeController extends BaseController {

    private final ChallengeService challengeService;

    @GetMapping
    public Result<List<Challenge>> listChallenges() {
        return Result.success(challengeService.getActiveChallenges());
    }

    @GetMapping("/{id}")
    public Result<Challenge> getChallengeDetail(@PathVariable Long id) {
        return Result.success(challengeService.getChallengeDetail(id));
    }

    @PostMapping("/{id}/join")
    public Result<ChallengeParticipant> joinChallenge(@PathVariable Long id) {
        Long userId = requireLogin();
        return Result.success(challengeService.joinChallenge(userId, id));
    }

    @GetMapping("/my")
    public Result<List<ChallengeParticipant>> myChallenges() {
        Long userId = requireLogin();
        return Result.success(challengeService.getUserParticipations(userId));
    }

    @PostMapping("/{id}/progress")
    public Result<ChallengeParticipant> updateProgress(@PathVariable Long id) {
        Long userId = requireLogin();
        return Result.success(challengeService.updateProgress(userId, id));
    }

    @GetMapping("/{id}/leaderboard")
    public Result<List<ChallengeParticipant>> leaderboard(
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(challengeService.getLeaderboard(id, limit));
    }
}

package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.entity.Challenge;
import com.pettrail.pettrailbackend.entity.ChallengeParticipant;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.ChallengeMapper;
import com.pettrail.pettrailbackend.mapper.ChallengeParticipantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeMapper challengeMapper;
    private final ChallengeParticipantMapper participantMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AchievementService achievementService;

    private static final String CHALLENGE_CACHE_PREFIX = "challenge:";
    private static final long CACHE_TTL_HOURS = 1;

    public List<Challenge> getActiveChallenges() {
        String cacheKey = CHALLENGE_CACHE_PREFIX + "active";
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                List<Challenge> result = (List<Challenge>) cached;
                return result;
            }
        } catch (Exception e) {
            log.warn("挑战赛缓存读取异常: {}", e.getMessage());
        }

        LocalDateTime now = LocalDateTime.now();
        List<Challenge> challenges = challengeMapper.selectList(
                new LambdaQueryWrapper<Challenge>()
                        .eq(Challenge::getStatus, 1)
                        .le(Challenge::getStartDate, now)
                        .ge(Challenge::getEndDate, now)
                        .orderByAsc(Challenge::getEndDate));

        try {
            redisTemplate.opsForValue().set(cacheKey, challenges, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("挑战赛缓存写入异常: {}", e.getMessage());
        }

        return challenges;
    }

    public Challenge getChallengeDetail(Long challengeId) {
        Challenge challenge = challengeMapper.selectById(challengeId);
        if (challenge == null) {
            throw new BusinessException(404, "挑战赛不存在");
        }
        return challenge;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChallengeParticipant joinChallenge(Long userId, Long challengeId) {
        Challenge challenge = challengeMapper.selectById(challengeId);
        if (challenge == null) {
            throw new BusinessException(404, "挑战赛不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(challenge.getStartDate()) || now.isAfter(challenge.getEndDate())) {
            throw new BusinessException("挑战赛不在进行中");
        }

        ChallengeParticipant existing = participantMapper.selectOne(
                new LambdaQueryWrapper<ChallengeParticipant>()
                        .eq(ChallengeParticipant::getChallengeId, challengeId)
                        .eq(ChallengeParticipant::getUserId, userId));
        if (existing != null) {
            throw new BusinessException(409, "已参与该挑战赛");
        }

        ChallengeParticipant participant = new ChallengeParticipant();
        participant.setChallengeId(challengeId);
        participant.setUserId(userId);
        participant.setProgress(0);
        participant.setCompleted(false);
        participant.setCreatedAt(LocalDateTime.now());
        participantMapper.insert(participant);

        challenge.setParticipantCount(challenge.getParticipantCount() + 1);
        challengeMapper.updateById(challenge);

        log.info("用户参与挑战赛: userId={}, challengeId={}", userId, challengeId);
        return participant;
    }

    public List<ChallengeParticipant> getUserParticipations(Long userId) {
        return participantMapper.selectList(
                new LambdaQueryWrapper<ChallengeParticipant>()
                        .eq(ChallengeParticipant::getUserId, userId)
                        .orderByDesc(ChallengeParticipant::getCreatedAt));
    }

    @Transactional(rollbackFor = Exception.class)
    public ChallengeParticipant updateProgress(Long userId, Long challengeId) {
        ChallengeParticipant participant = participantMapper.selectOne(
                new LambdaQueryWrapper<ChallengeParticipant>()
                        .eq(ChallengeParticipant::getChallengeId, challengeId)
                        .eq(ChallengeParticipant::getUserId, userId));
        if (participant == null) {
            throw new BusinessException(404, "未参与该挑战赛");
        }

        Challenge challenge = challengeMapper.selectById(challengeId);
        if (challenge == null || challenge.getConditionType() == null) {
            return participant;
        }

        int currentProgress = achievementService.getCurrentProgress(userId, challenge.getConditionType());
        participant.setProgress(currentProgress);

        if (!participant.getCompleted() && currentProgress >= challenge.getConditionValue()) {
            participant.setCompleted(true);
            participant.setCompletedAt(LocalDateTime.now());
            log.info("挑战赛完成: userId={}, challengeId={}", userId, challengeId);
        }

        participantMapper.updateById(participant);
        return participant;
    }

    public List<ChallengeParticipant> getLeaderboard(Long challengeId, int limit) {
        return participantMapper.selectList(
                new LambdaQueryWrapper<ChallengeParticipant>()
                        .eq(ChallengeParticipant::getChallengeId, challengeId)
                        .orderByDesc(ChallengeParticipant::getProgress)
                        .last("LIMIT " + limit));
    }

    public Page<Challenge> adminListChallenges(int page, int size, Integer status, Integer type) {
        Page<Challenge> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Challenge> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(Challenge::getStatus, status);
        if (type != null) wrapper.eq(Challenge::getType, type);
        wrapper.orderByDesc(Challenge::getCreatedAt);
        return challengeMapper.selectPage(pageParam, wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public Challenge adminCreateChallenge(Challenge challenge) {
        challenge.setCreatedAt(LocalDateTime.now());
        challenge.setUpdatedAt(LocalDateTime.now());
        challenge.setParticipantCount(0);
        challengeMapper.insert(challenge);
        return challenge;
    }

    @Transactional(rollbackFor = Exception.class)
    public Challenge adminUpdateChallenge(Long id, Challenge challenge) {
        Challenge existing = challengeMapper.selectById(id);
        if (existing == null) throw new BusinessException(404, "挑战赛不存在");
        challenge.setId(id);
        challenge.setUpdatedAt(LocalDateTime.now());
        challengeMapper.updateById(challenge);
        return challenge;
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteChallenge(Long id) {
        challengeMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateChallengeStatus(Long id, Integer status) {
        Challenge challenge = challengeMapper.selectById(id);
        if (challenge == null) throw new BusinessException(404, "挑战赛不存在");
        challenge.setStatus(status);
        challenge.setUpdatedAt(LocalDateTime.now());
        challengeMapper.updateById(challenge);
    }

    public Map<String, Object> adminGetChallengeStats(Long id) {
        Map<String, Object> stats = new HashMap<>();
        long totalParticipants = participantMapper.selectCount(
                new LambdaQueryWrapper<ChallengeParticipant>().eq(ChallengeParticipant::getChallengeId, id));
        long completedCount = participantMapper.selectCount(
                new LambdaQueryWrapper<ChallengeParticipant>().eq(ChallengeParticipant::getChallengeId, id).eq(ChallengeParticipant::getCompleted, true));
        stats.put("totalParticipants", totalParticipants);
        stats.put("completedCount", completedCount);
        stats.put("completionRate", totalParticipants > 0 ? String.format("%.1f%%", completedCount * 100.0 / totalParticipants) : "0%");
        return stats;
    }

    public Page<ChallengeParticipant> adminListParticipants(Long id, int page, int size) {
        Page<ChallengeParticipant> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ChallengeParticipant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChallengeParticipant::getChallengeId, id);
        wrapper.orderByDesc(ChallengeParticipant::getProgress);
        return participantMapper.selectPage(pageParam, wrapper);
    }
}

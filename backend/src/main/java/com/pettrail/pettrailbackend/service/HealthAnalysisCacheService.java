package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.dto.HealthAnalysisVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthAnalysisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "health:analysis:";
    private static final long CACHE_TTL_HOURS = 24;

    public String buildCacheKey(Long userId, Long petId) {
        return CACHE_PREFIX + userId + ":" + petId;
    }

    public HealthAnalysisVO get(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null && cached instanceof HealthAnalysisVO) {
                log.debug("健康分析缓存命中: key={}", cacheKey);
                return (HealthAnalysisVO) cached;
            }
        } catch (Exception e) {
            log.warn("健康分析缓存读取异常: key={}, error={}", cacheKey, e.getMessage());
        }
        return null;
    }

    public void put(String cacheKey, HealthAnalysisVO vo) {
        if (vo == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(cacheKey, vo, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("健康分析缓存写入: key={}, ttl={}h", cacheKey, CACHE_TTL_HOURS);
        } catch (Exception e) {
            log.warn("健康分析缓存写入异常: key={}, error={}", cacheKey, e.getMessage());
        }
    }

    public void invalidate(Long userId, Long petId) {
        try {
            redisTemplate.delete(buildCacheKey(userId, petId));
        } catch (Exception e) {
            log.warn("健康分析缓存清除异常: userId={}, petId={}, error={}", userId, petId, e.getMessage());
        }
    }

    public void invalidateByPetId(Long petId) {
        try {
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*:" + petId);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("健康分析缓存按petId清除异常: petId={}, error={}", petId, e.getMessage());
        }
    }

    public void invalidateAll() {
        try {
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("健康分析缓存全量清除异常: error={}", e.getMessage());
        }
    }

    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
            int count = keys != null ? keys.size() : 0;
            stats.put("cacheCount", count);
            stats.put("ttlHours", CACHE_TTL_HOURS);
        } catch (Exception e) {
            stats.put("cacheCount", -1);
            stats.put("error", e.getMessage());
        }
        return stats;
    }

    public String getCachedAnalysis(Long petId) {
        try {
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*:" + petId);
            if (keys != null && !keys.isEmpty()) {
                Object cached = redisTemplate.opsForValue().get(keys.iterator().next());
                if (cached instanceof String) {
                    return (String) cached;
                }
            }
        } catch (Exception e) {
            log.warn("AI分析缓存读取异常: petId={}, error={}", petId, e.getMessage());
        }
        return null;
    }

    public void cacheAnalysis(Long petId, String analysis) {
        if (analysis == null || analysis.isEmpty()) {
            return;
        }
        try {
            String cacheKey = CACHE_PREFIX + "ai:" + petId;
            redisTemplate.opsForValue().set(cacheKey, analysis, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("AI分析缓存写入: petId={}, ttl={}h", petId, CACHE_TTL_HOURS);
        } catch (Exception e) {
            log.warn("AI分析缓存写入异常: petId={}, error={}", petId, e.getMessage());
        }
    }
}

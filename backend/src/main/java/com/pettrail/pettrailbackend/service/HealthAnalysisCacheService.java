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
    private static final String AI_CACHE_PREFIX = "health:analysis:ai:";
    private static final long CACHE_TTL_HOURS = 6;

    public String buildCacheKey(Long userId, Long petId) {
        return CACHE_PREFIX + userId + ":" + petId;
    }

    public String buildAiCacheKey(Long petId) {
        return AI_CACHE_PREFIX + petId;
    }

    public HealthAnalysisVO get(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached == null) {
                return null;
            }
            if (cached instanceof HealthAnalysisVO) {
                log.debug("健康分析缓存命中: key={}", cacheKey);
                return (HealthAnalysisVO) cached;
            }
            if (cached instanceof Map) {
                log.debug("健康分析缓存命中(反序列化为Map，尝试转换): key={}", cacheKey);
                return convertMapToVO((Map<String, Object>) cached);
            }
            log.warn("健康分析缓存类型异常: key={}, type={}", cacheKey, cached.getClass().getName());
        } catch (Exception e) {
            log.warn("健康分析缓存读取异常: key={}, error={}", cacheKey, e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private HealthAnalysisVO convertMapToVO(Map<String, Object> map) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            return mapper.convertValue(map, HealthAnalysisVO.class);
        } catch (Exception e) {
            log.warn("Map转HealthAnalysisVO失败: {}", e.getMessage());
            return null;
        }
    }

    public void put(String cacheKey, HealthAnalysisVO vo) {
        if (vo == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(cacheKey, vo, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.info("健康分析缓存写入: key={}, ttl={}h", cacheKey, CACHE_TTL_HOURS);
        } catch (Exception e) {
            log.warn("健康分析缓存写入异常: key={}, error={}", cacheKey, e.getMessage());
        }
    }

    public void invalidate(Long userId, Long petId) {
        try {
            redisTemplate.delete(buildCacheKey(userId, petId));
            redisTemplate.delete(buildAiCacheKey(petId));
            log.info("健康分析缓存清除: userId={}, petId={}", userId, petId);
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
            String aiKey = buildAiCacheKey(petId);
            redisTemplate.delete(aiKey);
            log.info("健康分析缓存按petId清除: petId={}, clearedKeys={}", petId, keys != null ? keys.size() : 0);
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
            log.info("健康分析缓存全量清除: count={}", keys != null ? keys.size() : 0);
        } catch (Exception e) {
            log.warn("健康分析缓存全量清除异常: error={}", e.getMessage());
        }
    }

    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            Set<String> voKeys = redisTemplate.keys(CACHE_PREFIX + "*");
            Set<String> aiKeys = redisTemplate.keys(AI_CACHE_PREFIX + "*");
            int voCount = voKeys != null ? voKeys.size() : 0;
            int aiCount = aiKeys != null ? aiKeys.size() : 0;
            int totalCount = voCount + aiCount;
            stats.put("activeEntries", totalCount);
            stats.put("voCacheCount", voCount);
            stats.put("aiCacheCount", aiCount);
            stats.put("cacheCount", totalCount);
            stats.put("hitRate", totalCount > 0 ? "N/A" : "0%");
            stats.put("hitCount", 0);
            stats.put("missCount", 0);
            stats.put("evictCount", 0);
            stats.put("ttlMinutes", CACHE_TTL_HOURS * 60);
            stats.put("ttlHours", CACHE_TTL_HOURS);
            stats.put("cachePrefix", CACHE_PREFIX);
            stats.put("aiCachePrefix", AI_CACHE_PREFIX);
            if (voKeys != null && !voKeys.isEmpty()) {
                stats.put("sampleKeys", voKeys.stream().limit(5).toList());
            }
        } catch (Exception e) {
            stats.put("activeEntries", -1);
            stats.put("cacheCount", -1);
            stats.put("error", e.getMessage());
        }
        return stats;
    }

    public String getCachedAnalysis(Long petId) {
        try {
            String aiKey = buildAiCacheKey(petId);
            Object cached = redisTemplate.opsForValue().get(aiKey);
            if (cached instanceof String) {
                log.debug("AI分析缓存命中: petId={}", petId);
                return (String) cached;
            }
            if (cached != null) {
                log.warn("AI分析缓存类型异常: petId={}, type={}", petId, cached.getClass().getName());
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
            String aiKey = buildAiCacheKey(petId);
            redisTemplate.opsForValue().set(aiKey, analysis, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.info("AI分析缓存写入: petId={}, ttl={}h, contentLength={}", petId, CACHE_TTL_HOURS, analysis.length());
        } catch (Exception e) {
            log.warn("AI分析缓存写入异常: petId={}, error={}", petId, e.getMessage());
        }
    }
}

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
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthAnalysisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "health:analysis:";
    private static final String AI_CACHE_PREFIX = "health:analysis:ai:";
    private static final long CACHE_TTL_HOURS = 6;

    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong putCount = new AtomicLong(0);
    private final AtomicLong evictCount = new AtomicLong(0);

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
                missCount.incrementAndGet();
                return null;
            }
            hitCount.incrementAndGet();
            if (cached instanceof HealthAnalysisVO) {
                log.debug("健康分析缓存命中: key={}", cacheKey);
                return (HealthAnalysisVO) cached;
            }
            if (cached instanceof Map) {
                log.debug("健康分析缓存命中(反序列化为Map，尝试转换): key={}", cacheKey);
                HealthAnalysisVO vo = convertMapToVO((Map<String, Object>) cached);
                if (vo != null) {
                    return vo;
                }
            }
            log.warn("健康分析缓存类型异常，尝试JSON转换: key={}, actualType={}", cacheKey, cached.getClass().getName());
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                String json = mapper.writeValueAsString(cached);
                return mapper.readValue(json, HealthAnalysisVO.class);
            } catch (Exception e2) {
                log.error("健康分析缓存JSON转换也失败，删除无效缓存: key={}", cacheKey, e2);
                redisTemplate.delete(cacheKey);
                return null;
            }
        } catch (Exception e) {
            log.warn("健康分析缓存读取异常: key={}, error={}", cacheKey, e.getMessage());
            missCount.incrementAndGet();
            return null;
        }
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
            putCount.incrementAndGet();
            log.info("健康分析缓存写入: key={}, ttl={}h", cacheKey, CACHE_TTL_HOURS);
        } catch (Exception e) {
            log.warn("健康分析缓存写入异常: key={}, error={}", cacheKey, e.getMessage());
        }
    }

    public void invalidate(Long userId, Long petId) {
        try {
            redisTemplate.delete(buildCacheKey(userId, petId));
            redisTemplate.delete(buildAiCacheKey(petId));
            evictCount.incrementAndGet();
            log.info("健康分析缓存清除: userId={}, petId={}", userId, petId);
        } catch (Exception e) {
            log.warn("健康分析缓存清除异常: userId={}, petId={}, error={}", userId, petId, e.getMessage());
        }
    }

    public void invalidateByPetId(Long petId) {
        try {
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*:" + petId);
            int count = 0;
            if (keys != null && !keys.isEmpty()) {
                count = keys.size();
                redisTemplate.delete(keys);
            }
            String aiKey = buildAiCacheKey(petId);
            redisTemplate.delete(aiKey);
            evictCount.addAndGet(count + 1);
            log.info("健康分析缓存按petId清除: petId={}, clearedKeys={}", petId, count + 1);
        } catch (Exception e) {
            log.warn("健康分析缓存按petId清除异常: petId={}, error={}", petId, e.getMessage());
        }
    }

    public void invalidateAll() {
        try {
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
            int count = 0;
            if (keys != null && !keys.isEmpty()) {
                count = keys.size();
                redisTemplate.delete(keys);
            }
            Set<String> aiKeys = redisTemplate.keys(AI_CACHE_PREFIX + "*");
            if (aiKeys != null && !aiKeys.isEmpty()) {
                count += aiKeys.size();
                redisTemplate.delete(aiKeys);
            }
            evictCount.addAndGet(count);
            log.info("健康分析缓存全量清除: count={}", count);
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

            long hits = hitCount.get();
            long misses = missCount.get();
            long puts = putCount.get();
            long evicts = evictCount.get();
            long totalAccess = hits + misses;
            double hitRate = totalAccess > 0 ? Math.round(hits * 1000.0 / totalAccess) / 10.0 : 0.0;

            stats.put("activeEntries", totalCount);
            stats.put("voCacheCount", voCount);
            stats.put("aiCacheCount", aiCount);
            stats.put("cacheCount", totalCount);
            stats.put("hitRate", hitRate + "%");
            stats.put("hitCount", hits);
            stats.put("missCount", misses);
            stats.put("putCount", puts);
            stats.put("evictCount", evicts);
            stats.put("totalAccess", totalAccess);
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
        return getCachedAnalysis(petId, true);
    }

    public String getCachedAnalysis(Long petId, boolean trackStats) {
        try {
            String aiKey = buildAiCacheKey(petId);
            Object cached = redisTemplate.opsForValue().get(aiKey);
            if (cached instanceof String) {
                if (trackStats) {
                    hitCount.incrementAndGet();
                }
                log.debug("AI分析缓存命中: petId={}", petId);
                return (String) cached;
            }
            if (cached != null) {
                log.warn("AI分析缓存类型异常: petId={}, type={}", petId, cached.getClass().getName());
            }
            if (trackStats) {
                missCount.incrementAndGet();
            }
        } catch (Exception e) {
            log.warn("AI分析缓存读取异常: petId={}, error={}", petId, e.getMessage());
            if (trackStats) {
                missCount.incrementAndGet();
            }
        }
        return null;
    }

    public void cacheAnalysis(Long petId, String analysis) {
        cacheAnalysis(petId, analysis, true);
    }

    public void cacheAnalysis(Long petId, String analysis, boolean trackStats) {
        if (analysis == null || analysis.isEmpty()) {
            return;
        }
        try {
            String aiKey = buildAiCacheKey(petId);
            redisTemplate.opsForValue().set(aiKey, analysis, CACHE_TTL_HOURS, TimeUnit.HOURS);
            if (trackStats) {
                putCount.incrementAndGet();
            }
            log.info("AI分析缓存写入: petId={}, ttl={}h, contentLength={}", petId, CACHE_TTL_HOURS, analysis.length());
        } catch (Exception e) {
            log.warn("AI分析缓存写入异常: petId={}, error={}", petId, e.getMessage());
        }
    }
}

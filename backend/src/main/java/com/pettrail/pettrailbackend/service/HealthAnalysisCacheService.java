package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.dto.HealthAnalysisVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class HealthAnalysisCacheService {

    private static final long CACHE_TTL_MINUTES = 30;
    private static final long CLEANUP_INTERVAL_MINUTES = 5;

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong evictCount = new AtomicLong(0);

    private final ScheduledExecutorService cleanupScheduler;

    public HealthAnalysisCacheService() {
        cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "health-cache-cleanup");
            t.setDaemon(true);
            return t;
        });
        cleanupScheduler.scheduleAtFixedRate(
                this::cleanupExpiredEntries,
                CLEANUP_INTERVAL_MINUTES,
                CLEANUP_INTERVAL_MINUTES,
                TimeUnit.MINUTES
        );
        log.info("[健康分析缓存] 初始化完成, TTL={}分钟, 清理间隔={}分钟", CACHE_TTL_MINUTES, CLEANUP_INTERVAL_MINUTES);
    }

    public String buildCacheKey(Long userId, Long petId) {
        LocalDate today = LocalDate.now();
        return String.format("health:%d:pet:%d:date:%s", userId, petId, today);
    }

    public HealthAnalysisVO get(String cacheKey) {
        CacheEntry entry = cache.get(cacheKey);
        if (entry == null) {
            missCount.incrementAndGet();
            log.debug("[健康分析缓存] 缓存未命中: key={}", cacheKey);
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(cacheKey);
            evictCount.incrementAndGet();
            missCount.incrementAndGet();
            log.debug("[健康分析缓存] 缓存已过期: key={}, 存活时间={}ms", cacheKey, entry.getAge());
            return null;
        }

        hitCount.incrementAndGet();
        log.info("[健康分析缓存] 缓存命中: key={}, 剩余有效期={}秒", cacheKey, entry.getRemainingTTLSeconds());
        return entry.getValue();
    }

    public void put(String cacheKey, HealthAnalysisVO value) {
        CacheEntry entry = new CacheEntry(value, CACHE_TTL_MINUTES);
        cache.put(cacheKey, entry);
        log.info("[健康分析缓存] 缓存更新: key={}, TTL={}分钟", cacheKey, CACHE_TTL_MINUTES);
    }

    public void invalidate(Long userId, Long petId) {
        String prefix = String.format("health:%d:pet:%d:", userId, petId);
        int removed = 0;
        for (String key : new ArrayList<>(cache.keySet())) {
            if (key.startsWith(prefix)) {
                cache.remove(key);
                removed++;
            }
        }
        if (removed > 0) {
            log.info("[健康分析缓存] 手动失效: userId={}, petId={}, 清除条目数={}", userId, petId, removed);
        }
    }

    public void invalidateAll() {
        int size = cache.size();
        cache.clear();
        log.info("[健康分析缓存] 全量清除: 清除条目数={}", size);
    }

    public void invalidateByPetId(Long petId) {
        String suffix = String.format(":pet:%d:", petId);
        int removed = 0;
        for (String key : new ArrayList<>(cache.keySet())) {
            if (key.contains(suffix)) {
                cache.remove(key);
                removed++;
            }
        }
        if (removed > 0) {
            log.info("[健康分析缓存] 按宠物失效: petId={}, 清除条目数={}", petId, removed);
        }
    }

    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalEntries", cache.size());
        stats.put("hitCount", hitCount.get());
        stats.put("missCount", missCount.get());
        stats.put("evictCount", evictCount.get());
        long total = hitCount.get() + missCount.get();
        stats.put("hitRate", total > 0 ? String.format("%.1f%%", (hitCount.get() * 100.0 / total)) : "0%");
        stats.put("ttlMinutes", CACHE_TTL_MINUTES);
        stats.put("cleanupIntervalMinutes", CLEANUP_INTERVAL_MINUTES);

        long activeEntries = cache.values().stream().filter(e -> !e.isExpired()).count();
        long expiredEntries = cache.size() - activeEntries;
        stats.put("activeEntries", activeEntries);
        stats.put("expiredEntries", expiredEntries);

        return stats;
    }

    private void cleanupExpiredEntries() {
        int removed = 0;
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                cache.remove(entry.getKey());
                removed++;
            }
        }
        if (removed > 0) {
            evictCount.addAndGet(removed);
            log.info("[健康分析缓存] 定时清理: 清除过期条目数={}, 剩余条目数={}", removed, cache.size());
        }
    }

    private static class CacheEntry {
        private final HealthAnalysisVO value;
        private final long createdAt;
        private final long expireAt;

        CacheEntry(HealthAnalysisVO value, long ttlMinutes) {
            this.value = value;
            this.createdAt = System.currentTimeMillis();
            this.expireAt = this.createdAt + TimeUnit.MINUTES.toMillis(ttlMinutes);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }

        long getAge() {
            return System.currentTimeMillis() - createdAt;
        }

        long getRemainingTTLSeconds() {
            long remaining = expireAt - System.currentTimeMillis();
            return remaining > 0 ? TimeUnit.MILLISECONDS.toSeconds(remaining) : 0;
        }

        HealthAnalysisVO getValue() {
            return value;
        }
    }
}

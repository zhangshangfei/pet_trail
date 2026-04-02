package com.pettrail.pettrailbackend.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 多级缓存工具类
 * L1: Caffeine 本地缓存
 * L2: Redis 分布式缓存
 * L3: 数据库
 */
@Component
public class MultiLevelCache {

    @Autowired
    private CacheManager localCacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取缓存，如果不存在则从加载器加载
     */
    public <T> T get(String key, Class<T> clazz, Supplier<T> loader) {
        return get(key, clazz, loader, 30, TimeUnit.MINUTES);
    }

    /**
     * 获取缓存，支持自定义过期时间
     */
    public <T> T get(String key, Class<T> clazz, Supplier<T> loader, 
                     long timeout, TimeUnit unit) {
        
        // L1: 本地缓存 (Caffeine)
        Cache localCache = localCacheManager.getCache("local");
        if (localCache != null) {
            T cached = localCache.get(key, clazz);
            if (cached != null) {
                return cached;
            }
        }

        // L2: Redis 缓存
        String redisKey = buildRedisKey(key);
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null && clazz.isInstance(cached)) {
            // 回写本地缓存
            if (localCache != null) {
                localCache.put(key, cached);
            }
            return clazz.cast(cached);
        }

        // L3: 从数据库加载
        T value = loader.get();
        if (value != null) {
            // 写入 Redis
            redisTemplate.opsForValue().set(redisKey, value, timeout, unit);
            // 写入本地缓存
            if (localCache != null) {
                localCache.put(key, value);
            }
        }
        return value;
    }

    /**
     * 放入缓存
     */
    public void put(String key, Object value) {
        put(key, value, 30, TimeUnit.MINUTES);
    }

    /**
     * 放入缓存，支持自定义过期时间
     */
    public void put(String key, Object value, long timeout, TimeUnit unit) {
        // 写入本地缓存
        Cache localCache = localCacheManager.getCache("local");
        if (localCache != null) {
            localCache.put(key, value);
        }

        // 写入 Redis
        String redisKey = buildRedisKey(key);
        redisTemplate.opsForValue().set(redisKey, value, timeout, unit);
    }

    /**
     * 从缓存删除
     */
    public void evict(String key) {
        // 删除本地缓存
        Cache localCache = localCacheManager.getCache("local");
        if (localCache != null) {
            localCache.evict(key);
        }

        // 删除 Redis 缓存
        String redisKey = buildRedisKey(key);
        redisTemplate.delete(redisKey);
    }

    /**
     * 构建 Redis Key
     */
    private String buildRedisKey(String key) {
        return "cache:" + key;
    }

    /**
     * 清空所有缓存
     */
    public void clearAll() {
        localCacheManager.getCacheNames().forEach(name -> {
            Cache cache = localCacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
        redisTemplate.keys("cache:*").forEach(key -> {
            redisTemplate.delete(key);
        });
    }
}

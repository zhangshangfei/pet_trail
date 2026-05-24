package com.pettrail.pettrailbackend.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class MultiLevelCache {

    @Autowired
    private CacheManager localCacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisScanUtil redisScanUtil;

    public <T> T get(String key, Class<T> clazz, Supplier<T> loader) {
        return get(key, clazz, loader, 30, TimeUnit.MINUTES);
    }

    public <T> T get(String key, Class<T> clazz, Supplier<T> loader, 
                     long timeout, TimeUnit unit) {
        
        Cache localCache = localCacheManager.getCache("local");
        if (localCache != null) {
            T cached = localCache.get(key, clazz);
            if (cached != null) {
                return cached;
            }
        }

        String redisKey = buildRedisKey(key);
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null && clazz.isInstance(cached)) {
            if (localCache != null) {
                localCache.put(key, cached);
            }
            return clazz.cast(cached);
        }

        T value = loader.get();
        if (value != null) {
            redisTemplate.opsForValue().set(redisKey, value, timeout, unit);
            if (localCache != null) {
                localCache.put(key, value);
            }
        }
        return value;
    }

    public void put(String key, Object value) {
        put(key, value, 30, TimeUnit.MINUTES);
    }

    public void put(String key, Object value, long timeout, TimeUnit unit) {
        Cache localCache = localCacheManager.getCache("local");
        if (localCache != null) {
            localCache.put(key, value);
        }

        String redisKey = buildRedisKey(key);
        redisTemplate.opsForValue().set(redisKey, value, timeout, unit);
    }

    public void evict(String key) {
        Cache localCache = localCacheManager.getCache("local");
        if (localCache != null) {
            localCache.evict(key);
        }

        String redisKey = buildRedisKey(key);
        redisTemplate.delete(redisKey);
    }

    private String buildRedisKey(String key) {
        return "cache:" + key;
    }

    public void clearAll() {
        localCacheManager.getCacheNames().forEach(name -> {
            Cache cache = localCacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
        redisScanUtil.deleteByPattern("cache:*");
    }
}

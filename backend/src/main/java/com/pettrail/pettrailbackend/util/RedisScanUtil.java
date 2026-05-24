package com.pettrail.pettrailbackend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class RedisScanUtil {

    private static final int DEFAULT_BATCH_SIZE = 500;

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisScanUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Set<String> scanKeys(String pattern) {
        return scanKeys(pattern, DEFAULT_BATCH_SIZE);
    }

    public Set<String> scanKeys(String pattern, int batchSize) {
        Set<String> keys = new HashSet<>();
        try {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(batchSize)
                    .build();
            Cursor<String> cursor = redisTemplate.scan(options);
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
            cursor.close();
        } catch (Exception e) {
            log.error("SCAN扫描key失败, pattern={}: {}", pattern, e.getMessage());
        }
        return keys;
    }

    public long countKeys(String pattern) {
        return scanKeys(pattern).size();
    }

    public long deleteByPattern(String pattern) {
        Set<String> keys = scanKeys(pattern);
        if (keys.isEmpty()) return 0;
        Long deleted = redisTemplate.delete(keys);
        return deleted != null ? deleted : 0;
    }

    public List<String> scanKeysAsList(String pattern, int limit) {
        List<String> keys = new ArrayList<>();
        try {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(DEFAULT_BATCH_SIZE)
                    .build();
            Cursor<String> cursor = redisTemplate.scan(options);
            while (cursor.hasNext() && keys.size() < limit) {
                keys.add(cursor.next());
            }
            cursor.close();
        } catch (Exception e) {
            log.error("SCAN扫描key失败, pattern={}: {}", pattern, e.getMessage());
        }
        return keys;
    }
}

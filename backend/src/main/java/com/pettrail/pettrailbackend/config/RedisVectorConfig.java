package com.pettrail.pettrailbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "vector.enabled", havingValue = "true")
public class RedisVectorConfig {

    private static final String USER_INDEX = "idx_user_vectors";
    private static final String POST_INDEX = "idx_post_vectors";
    private static final int DIMENSIONS = 64;

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisVectorConfig(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        initIndexes();
    }

    private void initIndexes() {
        try {
            createUserIndex();
            createPostIndex();
            log.info("Redis 向量索引初始化完成");
        } catch (Exception e) {
            log.warn("Redis 向量索引初始化失败，将使用降级推荐: {}", e.getMessage());
        }
    }

    private void deleteKeysByPattern(String pattern) {
        try {
            List<String> keys = new ArrayList<>();
            redisTemplate.execute((connection) -> {
                try (var cursor = connection.keyCommands().scan(
                        ScanOptions.scanOptions().match(pattern).count(1000).build())) {
                    while (cursor.hasNext()) {
                        byte[] key = cursor.next();
                        keys.add(new String(key, StandardCharsets.UTF_8));
                    }
                }
                return null;
            }, true);

            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("清理旧向量数据: pattern={}, count={}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.warn("清理旧向量数据异常: {}", e.getMessage());
        }
    }

    private void dropIndexIfExists(String indexName) {
        try {
            redisTemplate.execute((connection) -> {
                connection.execute("FT.DROPINDEX", indexName.getBytes(StandardCharsets.UTF_8), "DD".getBytes(StandardCharsets.UTF_8));
                return null;
            }, true);
            log.info("已删除旧索引: {}", indexName);
        } catch (Exception ignored) {
        }
    }

    private void createUserIndex() {
        dropIndexIfExists(USER_INDEX);
        deleteKeysByPattern("vector:user:*");

        try {
            redisTemplate.execute((connection) -> {
                connection.execute("FT.CREATE",
                        USER_INDEX.getBytes(StandardCharsets.UTF_8),
                        "ON".getBytes(StandardCharsets.UTF_8),
                        "HASH".getBytes(StandardCharsets.UTF_8),
                        "PREFIX".getBytes(StandardCharsets.UTF_8),
                        "1".getBytes(StandardCharsets.UTF_8),
                        "vector:user:".getBytes(StandardCharsets.UTF_8),
                        "SCHEMA".getBytes(StandardCharsets.UTF_8),
                        "user_id".getBytes(StandardCharsets.UTF_8),
                        "TAG".getBytes(StandardCharsets.UTF_8),
                        "status".getBytes(StandardCharsets.UTF_8),
                        "NUMERIC".getBytes(StandardCharsets.UTF_8),
                        "vector".getBytes(StandardCharsets.UTF_8),
                        "VECTOR".getBytes(StandardCharsets.UTF_8),
                        "FLAT".getBytes(StandardCharsets.UTF_8),
                        "6".getBytes(StandardCharsets.UTF_8),
                        "TYPE".getBytes(StandardCharsets.UTF_8),
                        "FLOAT32".getBytes(StandardCharsets.UTF_8),
                        "DIM".getBytes(StandardCharsets.UTF_8),
                        String.valueOf(DIMENSIONS).getBytes(StandardCharsets.UTF_8),
                        "DISTANCE_METRIC".getBytes(StandardCharsets.UTF_8),
                        "COSINE".getBytes(StandardCharsets.UTF_8)
                );
                return null;
            }, true);
            log.info("用户向量索引创建成功: {}", USER_INDEX);
        } catch (Exception e) {
            log.warn("创建用户向量索引异常: {}", e.getMessage());
        }
    }

    private void createPostIndex() {
        dropIndexIfExists(POST_INDEX);
        deleteKeysByPattern("vector:post:*");

        try {
            redisTemplate.execute((connection) -> {
                connection.execute("FT.CREATE",
                        POST_INDEX.getBytes(StandardCharsets.UTF_8),
                        "ON".getBytes(StandardCharsets.UTF_8),
                        "HASH".getBytes(StandardCharsets.UTF_8),
                        "PREFIX".getBytes(StandardCharsets.UTF_8),
                        "1".getBytes(StandardCharsets.UTF_8),
                        "vector:post:".getBytes(StandardCharsets.UTF_8),
                        "SCHEMA".getBytes(StandardCharsets.UTF_8),
                        "post_id".getBytes(StandardCharsets.UTF_8),
                        "TAG".getBytes(StandardCharsets.UTF_8),
                        "author_id".getBytes(StandardCharsets.UTF_8),
                        "TAG".getBytes(StandardCharsets.UTF_8),
                        "deleted".getBytes(StandardCharsets.UTF_8),
                        "NUMERIC".getBytes(StandardCharsets.UTF_8),
                        "created_at".getBytes(StandardCharsets.UTF_8),
                        "NUMERIC".getBytes(StandardCharsets.UTF_8),
                        "vector".getBytes(StandardCharsets.UTF_8),
                        "VECTOR".getBytes(StandardCharsets.UTF_8),
                        "FLAT".getBytes(StandardCharsets.UTF_8),
                        "6".getBytes(StandardCharsets.UTF_8),
                        "TYPE".getBytes(StandardCharsets.UTF_8),
                        "FLOAT32".getBytes(StandardCharsets.UTF_8),
                        "DIM".getBytes(StandardCharsets.UTF_8),
                        String.valueOf(DIMENSIONS).getBytes(StandardCharsets.UTF_8),
                        "DISTANCE_METRIC".getBytes(StandardCharsets.UTF_8),
                        "COSINE".getBytes(StandardCharsets.UTF_8)
                );
                return null;
            }, true);
            log.info("动态向量索引创建成功: {}", POST_INDEX);
        } catch (Exception e) {
            log.warn("创建动态向量索引异常: {}", e.getMessage());
        }
    }
}

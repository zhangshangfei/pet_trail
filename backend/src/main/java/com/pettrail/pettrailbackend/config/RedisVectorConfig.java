package com.pettrail.pettrailbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

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

    private void createUserIndex() {
        try {
            redisTemplate.execute((connection) -> {
                byte[] indexName = USER_INDEX.getBytes();
                try {
                    connection.execute("FT.INFO", indexName);
                    log.info("用户向量索引已存在: {}", USER_INDEX);
                    return null;
                } catch (Exception ignored) {
                }

                String createCmd = String.format(
                    "FT.CREATE %s ON HASH PREFIX 1 vector:user: SCHEMA " +
                    "user_id NUMERIC SORTABLE " +
                    "status NUMERIC FILTERABLE " +
                    "vector VECTOR FLAT 6 TYPE FLOAT32 DIM %d DISTANCE_METRIC COSINE",
                    USER_INDEX, DIMENSIONS
                );
                connection.execute("FT.CREATE", createCmd.getBytes());
                log.info("用户向量索引创建成功: {}", USER_INDEX);
                return null;
            }, true);
        } catch (Exception e) {
            log.warn("创建用户向量索引异常: {}", e.getMessage());
        }
    }

    private void createPostIndex() {
        try {
            redisTemplate.execute((connection) -> {
                byte[] indexName = POST_INDEX.getBytes();
                try {
                    connection.execute("FT.INFO", indexName);
                    log.info("动态向量索引已存在: {}", POST_INDEX);
                    return null;
                } catch (Exception ignored) {
                }

                String createCmd = String.format(
                    "FT.CREATE %s ON HASH PREFIX 1 vector:post: SCHEMA " +
                    "post_id NUMERIC SORTABLE " +
                    "author_id NUMERIC FILTERABLE " +
                    "deleted NUMERIC FILTERABLE " +
                    "created_at NUMERIC FILTERABLE " +
                    "vector VECTOR FLAT 6 TYPE FLOAT32 DIM %d DISTANCE_METRIC COSINE",
                    POST_INDEX, DIMENSIONS
                );
                connection.execute("FT.CREATE", createCmd.getBytes());
                log.info("动态向量索引创建成功: {}", POST_INDEX);
                return null;
            }, true);
        } catch (Exception e) {
            log.warn("创建动态向量索引异常: {}", e.getMessage());
        }
    }
}

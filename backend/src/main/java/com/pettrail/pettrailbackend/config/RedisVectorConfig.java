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
                try {
                    connection.execute("FT.INFO", USER_INDEX.getBytes());
                    log.info("用户向量索引已存在: {}", USER_INDEX);
                    return null;
                } catch (Exception ignored) {
                }

                connection.execute("FT.CREATE",
                        USER_INDEX.getBytes(),
                        "ON".getBytes(),
                        "HASH".getBytes(),
                        "PREFIX".getBytes(),
                        "1".getBytes(),
                        "vector:user:".getBytes(),
                        "SCHEMA".getBytes(),
                        "user_id".getBytes(),
                        "NUMERIC".getBytes(),
                        "SORTABLE".getBytes(),
                        "vector".getBytes(),
                        "VECTOR".getBytes(),
                        "FLAT".getBytes(),
                        "6".getBytes(),
                        "TYPE".getBytes(),
                        "FLOAT32".getBytes(),
                        "DIM".getBytes(),
                        String.valueOf(DIMENSIONS).getBytes(),
                        "DISTANCE_METRIC".getBytes(),
                        "COSINE".getBytes()
                );
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
                try {
                    connection.execute("FT.INFO", POST_INDEX.getBytes());
                    log.info("动态向量索引已存在: {}", POST_INDEX);
                    return null;
                } catch (Exception ignored) {
                }

                connection.execute("FT.CREATE",
                        POST_INDEX.getBytes(),
                        "ON".getBytes(),
                        "HASH".getBytes(),
                        "PREFIX".getBytes(),
                        "1".getBytes(),
                        "vector:post:".getBytes(),
                        "SCHEMA".getBytes(),
                        "post_id".getBytes(),
                        "NUMERIC".getBytes(),
                        "SORTABLE".getBytes(),
                        "author_id".getBytes(),
                        "NUMERIC".getBytes(),
                        "FILTERABLE".getBytes(),
                        "deleted".getBytes(),
                        "NUMERIC".getBytes(),
                        "FILTERABLE".getBytes(),
                        "created_at".getBytes(),
                        "NUMERIC".getBytes(),
                        "FILTERABLE".getBytes(),
                        "vector".getBytes(),
                        "VECTOR".getBytes(),
                        "FLAT".getBytes(),
                        "6".getBytes(),
                        "TYPE".getBytes(),
                        "FLOAT32".getBytes(),
                        "DIM".getBytes(),
                        String.valueOf(DIMENSIONS).getBytes(),
                        "DISTANCE_METRIC".getBytes(),
                        "COSINE".getBytes()
                );
                log.info("动态向量索引创建成功: {}", POST_INDEX);
                return null;
            }, true);
        } catch (Exception e) {
            log.warn("创建动态向量索引异常: {}", e.getMessage());
        }
    }
}

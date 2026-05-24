package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.PetMapper;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnProperty(name = "vector.enabled", havingValue = "true")
public class VectorSyncService {

    private static final String USER_KEY_PREFIX = "vector:user:";
    private static final String POST_KEY_PREFIX = "vector:post:";
    private static final String SYNC_TRACK_PREFIX = "vector:sync:";

    private final VectorService vectorService;
    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final PostMapper postMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public VectorSyncService(VectorService vectorService, UserMapper userMapper,
                             PetMapper petMapper, PostMapper postMapper,
                             RedisTemplate<String, Object> redisTemplate) {
        this.vectorService = vectorService;
        this.userMapper = userMapper;
        this.petMapper = petMapper;
        this.postMapper = postMapper;
        this.redisTemplate = redisTemplate;
    }

    @Async
    public void syncUserVector(Long userId) {
        try {
            float[] vector = vectorService.buildUserVector(userId);
            User user = userMapper.selectById(userId);
            if (user == null) return;

            String key = USER_KEY_PREFIX + userId;
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] vectorBytes = floatArrayToBytes(vector);

            redisTemplate.execute((RedisCallback<Void>) connection -> {
                connection.hashCommands().hSet(keyBytes, "user_id".getBytes(StandardCharsets.UTF_8), String.valueOf(userId).getBytes(StandardCharsets.UTF_8));
                connection.hashCommands().hSet(keyBytes, "status".getBytes(StandardCharsets.UTF_8), String.valueOf(user.getStatus() != null ? user.getStatus() : 1).getBytes(StandardCharsets.UTF_8));
                connection.hashCommands().hSet(keyBytes, "vector".getBytes(StandardCharsets.UTF_8), vectorBytes);
                return null;
            });

            redisTemplate.opsForValue().set(
                SYNC_TRACK_PREFIX + "user:" + userId,
                LocalDateTime.now().toString(),
                7, TimeUnit.DAYS);

            log.debug("用户向量同步完成: userId={}", userId);
        } catch (Exception e) {
            log.warn("用户向量同步失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Async
    public void syncPostVector(Long postId) {
        try {
            float[] vector = vectorService.buildPostVector(postId);
            Post post = postMapper.selectById(postId);
            if (post == null) return;

            String key = POST_KEY_PREFIX + postId;
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] vectorBytes = floatArrayToBytes(vector);

            redisTemplate.execute((RedisCallback<Void>) connection -> {
                connection.hashCommands().hSet(keyBytes, "post_id".getBytes(StandardCharsets.UTF_8), String.valueOf(postId).getBytes(StandardCharsets.UTF_8));
                connection.hashCommands().hSet(keyBytes, "author_id".getBytes(StandardCharsets.UTF_8), String.valueOf(post.getUserId()).getBytes(StandardCharsets.UTF_8));
                connection.hashCommands().hSet(keyBytes, "deleted".getBytes(StandardCharsets.UTF_8), String.valueOf(post.getDeleted() != null ? post.getDeleted() : 0).getBytes(StandardCharsets.UTF_8));
                long createdAt = post.getCreatedAt() != null ?
                    post.getCreatedAt().toEpochSecond(java.time.ZoneOffset.of("+8")) : 0;
                connection.hashCommands().hSet(keyBytes, "created_at".getBytes(StandardCharsets.UTF_8), String.valueOf(createdAt).getBytes(StandardCharsets.UTF_8));
                connection.hashCommands().hSet(keyBytes, "vector".getBytes(StandardCharsets.UTF_8), vectorBytes);
                return null;
            });

            redisTemplate.opsForValue().set(
                SYNC_TRACK_PREFIX + "post:" + postId,
                LocalDateTime.now().toString(),
                7, TimeUnit.DAYS);

            log.debug("动态向量同步完成: postId={}", postId);
        } catch (Exception e) {
            log.warn("动态向量同步失败: postId={}, error={}", postId, e.getMessage());
        }
    }

    @Async
    public void syncUserVectorByPetChange(Long petId) {
        try {
            Pet pet = petMapper.selectById(petId);
            if (pet != null) {
                syncUserVector(pet.getUserId());
            }
        } catch (Exception e) {
            log.warn("宠物变更触发向量同步失败: petId={}, error={}", petId, e.getMessage());
        }
    }

    @Async
    public void syncUserVectorByFollowChange(Long userId) {
        syncUserVector(userId);
    }

    public int syncAllUsers() {
        List<User> users = userMapper.selectList(
            new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        int count = 0;
        for (User user : users) {
            try {
                float[] vector = vectorService.buildUserVector(user.getId());
                String key = USER_KEY_PREFIX + user.getId();
                byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                byte[] vectorBytes = floatArrayToBytes(vector);

                redisTemplate.execute((RedisCallback<Void>) connection -> {
                    connection.hashCommands().hSet(keyBytes, "user_id".getBytes(StandardCharsets.UTF_8), String.valueOf(user.getId()).getBytes(StandardCharsets.UTF_8));
                    connection.hashCommands().hSet(keyBytes, "status".getBytes(StandardCharsets.UTF_8), "1".getBytes(StandardCharsets.UTF_8));
                    connection.hashCommands().hSet(keyBytes, "vector".getBytes(StandardCharsets.UTF_8), vectorBytes);
                    return null;
                });
                count++;
            } catch (Exception e) {
                log.warn("全量同步用户向量失败: userId={}", user.getId());
            }
        }
        log.info("用户向量全量同步完成: total={}, synced={}", users.size(), count);
        return count;
    }

    public int syncAllPosts() {
        List<Post> posts = postMapper.selectList(
            new LambdaQueryWrapper<Post>()
                .eq(Post::getDeleted, 0)
                .eq(Post::getAuditStatus, 1)
                .last("LIMIT 10000"));
        int count = 0;
        for (Post post : posts) {
            try {
                float[] vector = vectorService.buildPostVector(post.getId());
                String key = POST_KEY_PREFIX + post.getId();
                byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                byte[] vectorBytes = floatArrayToBytes(vector);

                long createdAt = post.getCreatedAt() != null ?
                    post.getCreatedAt().toEpochSecond(java.time.ZoneOffset.of("+8")) : 0;

                redisTemplate.execute((RedisCallback<Void>) connection -> {
                    connection.hashCommands().hSet(keyBytes, "post_id".getBytes(StandardCharsets.UTF_8), String.valueOf(post.getId()).getBytes(StandardCharsets.UTF_8));
                    connection.hashCommands().hSet(keyBytes, "author_id".getBytes(StandardCharsets.UTF_8), String.valueOf(post.getUserId()).getBytes(StandardCharsets.UTF_8));
                    connection.hashCommands().hSet(keyBytes, "deleted".getBytes(StandardCharsets.UTF_8), "0".getBytes(StandardCharsets.UTF_8));
                    connection.hashCommands().hSet(keyBytes, "created_at".getBytes(StandardCharsets.UTF_8), String.valueOf(createdAt).getBytes(StandardCharsets.UTF_8));
                    connection.hashCommands().hSet(keyBytes, "vector".getBytes(StandardCharsets.UTF_8), vectorBytes);
                    return null;
                });
                count++;
            } catch (Exception e) {
                log.warn("全量同步动态向量失败: postId={}", post.getId());
            }
        }
        log.info("动态向量全量同步完成: total={}, synced={}", posts.size(), count);
        return count;
    }

    @Scheduled(fixedDelay = 3600000, initialDelay = 300000)
    public void scheduledIncrementalSync() {
        log.info("开始定时增量向量同步...");
        try {
            syncAllUsers();
            syncAllPosts();
        } catch (Exception e) {
            log.warn("定时向量同步异常: {}", e.getMessage());
        }
    }

    public void deleteUserVector(Long userId) {
        try {
            redisTemplate.delete(USER_KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("删除用户向量失败: userId={}", userId);
        }
    }

    public void deletePostVector(Long postId) {
        try {
            redisTemplate.delete(POST_KEY_PREFIX + postId);
        } catch (Exception e) {
            log.warn("删除动态向量失败: postId={}", postId);
        }
    }

    private byte[] floatArrayToBytes(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        for (float f : floats) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }
}

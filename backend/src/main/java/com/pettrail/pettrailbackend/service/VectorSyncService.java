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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.HashMap;
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

            Map<String, Object> fields = new HashMap<>();
            fields.put("user_id", userId);
            fields.put("status", user.getStatus() != null ? user.getStatus() : 1);
            fields.put("vector", floatArrayToBytes(vector));

            redisTemplate.opsForHash().putAll(USER_KEY_PREFIX + userId, fields);

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

            Map<String, Object> fields = new HashMap<>();
            fields.put("post_id", postId);
            fields.put("author_id", post.getUserId());
            fields.put("deleted", post.getDeleted() != null ? post.getDeleted() : 0);
            fields.put("created_at", post.getCreatedAt() != null ?
                post.getCreatedAt().toEpochSecond(java.time.ZoneOffset.of("+8")) : 0);
            fields.put("vector", floatArrayToBytes(vector));

            redisTemplate.opsForHash().putAll(POST_KEY_PREFIX + postId, fields);

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
                Map<String, Object> fields = new HashMap<>();
                fields.put("user_id", user.getId());
                fields.put("status", 1);
                fields.put("vector", floatArrayToBytes(vector));
                redisTemplate.opsForHash().putAll(USER_KEY_PREFIX + user.getId(), fields);
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
                Map<String, Object> fields = new HashMap<>();
                fields.put("post_id", post.getId());
                fields.put("author_id", post.getUserId());
                fields.put("deleted", 0);
                fields.put("created_at", post.getCreatedAt() != null ?
                    post.getCreatedAt().toEpochSecond(java.time.ZoneOffset.of("+8")) : 0);
                fields.put("vector", floatArrayToBytes(vector));
                redisTemplate.opsForHash().putAll(POST_KEY_PREFIX + post.getId(), fields);
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

package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.VectorSearchResult;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.PetMapper;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnProperty(name = "vector.enabled", havingValue = "true")
public class VectorService {

    private static final int DIMENSIONS = 64;
    private static final List<String> TOP_BREEDS = List.of(
        "金毛寻回犬", "拉布拉多", "泰迪", "柯基", "哈士奇",
        "边牧", "萨摩耶", "柴犬", "法斗", "德牧",
        "比熊", "雪纳瑞", "博美", "约克夏", "马尔济斯",
        "阿拉斯加", "秋田犬", "吉娃娃", "巴哥", "中华田园犬",
        "英国短毛猫", "美国短毛猫", "布偶猫", "橘猫", "狸花猫",
        "暹罗猫", "波斯猫", "缅因猫", "折耳猫", "加菲猫",
        "蓝猫", "奶牛猫", "三花猫", "白猫", "黑猫",
        "英短蓝白", "英短金渐层", "英短银渐层", "美短虎斑", "美短加白",
        "孟买猫", "无毛猫", "矮脚猫", "串串猫", "串串狗",
        "贵宾犬", "蝴蝶犬", "大麦町犬", "喜乐蒂牧羊犬", "其他"
    );

    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final PostMapper postMapper;
    private final FollowService followService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${vector.user-index:idx_user_vectors}")
    private String userIndex;

    @Value("${vector.post-index:idx_post_vectors}")
    private String postIndex;

    @Value("${vector.top-k:200}")
    private int topK;

    private volatile boolean vectorAvailable = true;
    private volatile long lastFailureTime = 0;
    private static final long CIRCUIT_BREAKER_RESET_MS = 60_000;

    public VectorService(UserMapper userMapper, PetMapper petMapper,
                         PostMapper postMapper, FollowService followService,
                         RedisTemplate<String, Object> redisTemplate) {
        this.userMapper = userMapper;
        this.petMapper = petMapper;
        this.postMapper = postMapper;
        this.followService = followService;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        log.info("VectorService 初始化完成, dimensions={}, topK={}, userIndex={}, postIndex={}",
                DIMENSIONS, topK, userIndex, postIndex);
    }

    public boolean isAvailable() {
        if (!vectorAvailable) {
            if (System.currentTimeMillis() - lastFailureTime > CIRCUIT_BREAKER_RESET_MS) {
                vectorAvailable = true;
                log.info("向量服务熔断恢复，重新尝试");
            }
        }
        return vectorAvailable;
    }

    public float[] buildUserVector(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return new float[DIMENSIONS];

        List<Pet> pets = petMapper.selectList(
            new LambdaQueryWrapper<Pet>().eq(Pet::getUserId, userId));

        float[] vector = new float[DIMENSIONS];

        int[] breedSlots = encodeBreedSlots(pets);
        for (int i = 0; i < 50; i++) {
            vector[i] = breedSlots[i];
        }

        int totalPets = Math.max(pets.size(), 1);
        long catCount = pets.stream().filter(p -> p.getCategory() != null && p.getCategory() == 1).count();
        long dogCount = pets.stream().filter(p -> p.getCategory() != null && p.getCategory() == 2).count();
        vector[50] = (float) catCount / totalPets;
        vector[51] = (float) dogCount / totalPets;

        int followerCount = followService.getFollowerCount(userId);
        long postCountLong = postMapper.selectCount(
            new LambdaQueryWrapper<Post>().eq(Post::getUserId, userId));
        int postCount = (int) postCountLong;
        int followeeCount = followService.getFolloweeCount(userId);

        vector[52] = Math.min(1.0f, followerCount / 100.0f);
        vector[53] = Math.min(1.0f, postCount / 50.0f);
        vector[54] = Math.min(1.0f, followerCount / 100.0f);
        vector[55] = Math.min(1.0f, postCount / 50.0f);
        vector[56] = Math.min(1.0f, followeeCount / 50.0f);

        vector[57] = Math.min(1.0f, followeeCount / 100.0f);
        float followerRatio = followerCount > 0 ? Math.min(1.0f, (float) followeeCount / followerCount) : 0.0f;
        vector[58] = followerRatio;
        vector[59] = 0.0f;

        if (user.getCreatedAt() != null) {
            long daysSince = ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now());
            vector[60] = Math.min(1.0f, (float) daysSince / 365);
            vector[61] = daysSince <= 7 ? 1.0f : 0.0f;
            vector[62] = daysSince <= 30 ? 1.0f : 0.0f;
            vector[63] = daysSince <= 7 ? 1.0f : 0.0f;
        }

        return vector;
    }

    public float[] buildPostVector(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) return new float[DIMENSIONS];

        float[] authorVec = buildUserVector(post.getUserId());
        float[] vector = new float[DIMENSIONS];

        System.arraycopy(authorVec, 0, vector, 0, 52);

        int likeCount = post.getLikeCount() != null ? post.getLikeCount() : 0;
        int commentCount = post.getCommentCount() != null ? post.getCommentCount() : 0;
        int shareCount = post.getShareCount() != null ? post.getShareCount() : 0;
        int eeCount = post.getEeCount() != null ? post.getEeCount() : 0;

        vector[52] = Math.min(1.0f, likeCount / 100.0f);
        vector[53] = Math.min(1.0f, commentCount / 50.0f);
        vector[54] = Math.min(1.0f, shareCount / 20.0f);
        vector[55] = Math.min(1.0f, eeCount / 50.0f);

        if (post.getCreatedAt() != null) {
            long hoursSince = ChronoUnit.HOURS.between(post.getCreatedAt(), LocalDateTime.now());
            vector[56] = (float) (post.getCreatedAt().getHour() / 24.0);
            vector[57] = Math.min(1.0f, (float) hoursSince / 720.0f);
            vector[58] = hoursSince <= 24 ? 1.0f : 0.0f;
        }

        int authorFollowerCount = followService.getFollowerCount(post.getUserId());
        long authorPostCountLong = postMapper.selectCount(
            new LambdaQueryWrapper<Post>().eq(Post::getUserId, post.getUserId()));
        int authorPostCount = (int) authorPostCountLong;

        vector[59] = Math.min(1.0f, likeCount / 100.0f);
        vector[60] = Math.min(1.0f, commentCount / 50.0f);
        vector[61] = Math.min(1.0f, authorFollowerCount / 100.0f);
        vector[62] = Math.min(1.0f, authorPostCount / 50.0f);
        vector[63] = 0.0f;

        return vector;
    }

    public List<VectorSearchResult> searchSimilarUsers(float[] queryVector, int topK, Set<Long> excludeIds) {
        if (!isAvailable()) return Collections.emptyList();

        try {
            byte[] vectorBytes = floatArrayToBytes(queryVector);
            String baseQuery = "*=>[KNN " + topK + " @vector $vec AS score]";
            if (!excludeIds.isEmpty()) {
                String ids = excludeIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("|"));
                baseQuery = "(-@user_id:{" + ids + "})=>[KNN " + topK + " @vector $vec AS score]";
            }
            final String searchQuery = baseQuery;

            Object result = redisTemplate.execute((RedisCallback<Object>) connection -> {
                return connection.execute("FT.SEARCH",
                    userIndex.getBytes(StandardCharsets.UTF_8),
                    searchQuery.getBytes(StandardCharsets.UTF_8),
                    "PARAMS".getBytes(StandardCharsets.UTF_8),
                    "2".getBytes(StandardCharsets.UTF_8),
                    "vec".getBytes(StandardCharsets.UTF_8),
                    vectorBytes,
                    "DIALECT".getBytes(StandardCharsets.UTF_8),
                    "2".getBytes(StandardCharsets.UTF_8)
                );
            }, true);

            return parseSearchResult(result);
        } catch (Exception e) {
            handleFailure(e);
            return Collections.emptyList();
        }
    }

    public List<VectorSearchResult> searchSimilarPosts(float[] queryVector, int topK, Set<Long> excludeAuthorIds) {
        if (!isAvailable()) return Collections.emptyList();

        try {
            byte[] vectorBytes = floatArrayToBytes(queryVector);
            String baseQuery = "*=>[KNN " + topK + " @vector $vec AS score]";
            if (!excludeAuthorIds.isEmpty()) {
                String ids = excludeAuthorIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("|"));
                baseQuery = "(-@author_id:{" + ids + "})=>[KNN " + topK + " @vector $vec AS score]";
            }
            final String searchQuery = baseQuery;

            Object result = redisTemplate.execute((RedisCallback<Object>) connection -> {
                return connection.execute("FT.SEARCH",
                    postIndex.getBytes(StandardCharsets.UTF_8),
                    searchQuery.getBytes(StandardCharsets.UTF_8),
                    "PARAMS".getBytes(StandardCharsets.UTF_8),
                    "2".getBytes(StandardCharsets.UTF_8),
                    "vec".getBytes(StandardCharsets.UTF_8),
                    vectorBytes,
                    "DIALECT".getBytes(StandardCharsets.UTF_8),
                    "2".getBytes(StandardCharsets.UTF_8)
                );
            }, true);

            return parseSearchResult(result);
        } catch (Exception e) {
            handleFailure(e);
            return Collections.emptyList();
        }
    }

    private List<VectorSearchResult> parseSearchResult(Object result) {
        List<VectorSearchResult> results = new ArrayList<>();
        if (!(result instanceof List)) return results;

        List<?> list = (List<?>) result;
        if (list.size() < 2) return results;

        for (int i = 1; i < list.size(); i++) {
            Object item = list.get(i);
            if (item instanceof byte[]) {
                String key = new String((byte[]) item, StandardCharsets.UTF_8);
                Long id = extractIdFromKey(key);
                if (id != null && i + 1 < list.size() && list.get(i + 1) instanceof List) {
                    List<?> fields = (List<?>) list.get(i + 1);
                    Double score = extractScore(fields);
                    results.add(VectorSearchResult.of(id, score));
                    i++;
                }
            }
        }

        return results;
    }

    private Long extractIdFromKey(String key) {
        if (key == null) return null;
        if (key.startsWith("vector:user:")) {
            return Long.parseLong(key.substring("vector:user:".length()));
        }
        if (key.startsWith("vector:post:")) {
            return Long.parseLong(key.substring("vector:post:".length()));
        }
        return null;
    }

    private Double extractScore(List<?> fields) {
        for (int i = 0; i < fields.size() - 1; i++) {
            Object f = fields.get(i);
            String fieldName = f instanceof byte[] ? new String((byte[]) f, StandardCharsets.UTF_8) : String.valueOf(f);
            if ("score".equals(fieldName) || "__vector_score".equals(fieldName)) {
                Object val = fields.get(i + 1);
                String scoreStr = val instanceof byte[] ? new String((byte[]) val, StandardCharsets.UTF_8) : String.valueOf(val);
                try {
                    return Double.parseDouble(scoreStr);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private int[] encodeBreedSlots(List<Pet> pets) {
        int[] slots = new int[50];
        for (Pet pet : pets) {
            String breed = pet.getBreed();
            if (breed == null || breed.trim().isEmpty()) {
                slots[49] = 1;
                continue;
            }
            int idx = TOP_BREEDS.indexOf(breed);
            if (idx >= 0 && idx < 49) {
                slots[idx] = 1;
            } else {
                slots[49] = 1;
            }
        }
        return slots;
    }

    private byte[] floatArrayToBytes(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        for (float f : floats) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void handleFailure(Exception e) {
        log.warn("向量检索失败: {}", e.getMessage());
        vectorAvailable = false;
        lastFailureTime = System.currentTimeMillis();
    }
}

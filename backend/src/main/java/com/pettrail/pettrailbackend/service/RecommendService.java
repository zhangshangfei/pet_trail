package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.PostLike;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.FollowMapper;
import com.pettrail.pettrailbackend.mapper.PetMapper;
import com.pettrail.pettrailbackend.mapper.PostLikeMapper;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final FollowMapper followMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostMapper postMapper;
    private final FollowService followService;
    private final PostService postService;
    private final UserBehaviorService userBehaviorService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "recommend:";
    private static final String POST_CACHE_KEY_PREFIX = "recommend:posts:";
    private static final long CACHE_TTL_HOURS = 2;
    private static final long POST_CACHE_TTL_MINUTES = 30;

    private static final double W_BREED = 0.30;
    private static final double W_ACTIVITY = 0.25;
    private static final double W_SOCIAL = 0.20;
    private static final double W_INTERACT = 0.15;
    private static final double W_NEWUSER = 0.10;

    private static final double PW_INTEREST = 0.30;
    private static final double PW_HOT = 0.25;
    private static final double PW_SOCIAL = 0.20;
    private static final double PW_INTERACT = 0.15;
    private static final double PW_FRESH = 0.10;
    private static final double PW_BEHAVIOR = 0.10;
    private static final int CANDIDATE_LIMIT = 500;

    public List<Map<String, Object>> recommendUsers(Long currentUserId, int page, int size) {
        if (currentUserId == null) {
            return recommendForAnonymous(page, size);
        }

        String cacheKey = CACHE_KEY_PREFIX + currentUserId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> cachedList = (List<Map<String, Object>>) cached;
                int start = (page - 1) * size;
                int end = Math.min(start + size, cachedList.size());
                if (start < cachedList.size()) {
                    return new ArrayList<>(cachedList.subList(start, end));
                }
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.warn("推荐缓存读取异常，重新计算: {}", e.getMessage());
        }

        List<Map<String, Object>> fullList = computeRecommendations(currentUserId);

        try {
            redisTemplate.opsForValue().set(cacheKey, fullList, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("推荐缓存写入异常: {}", e.getMessage());
        }

        int start = (page - 1) * size;
        int end = Math.min(start + size, fullList.size());
        if (start < fullList.size()) {
            return fullList.subList(start, end);
        }
        return Collections.emptyList();
    }

    private List<Map<String, Object>> recommendForAnonymous(int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 50) size = 50;

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, 1);
        wrapper.orderByDesc(User::getCreatedAt);
        wrapper.last("LIMIT " + (page - 1) * size + ", " + size);
        List<User> users = userMapper.selectList(wrapper);

        return users.stream().map(user -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", user.getId());
            item.put("nickname", user.getNickname());
            item.put("avatar", user.getAvatar());
            item.put("gender", user.getGender());
            item.put("followerCount", followService.getFollowerCount(user.getId()));
            item.put("postCount", postService.getUserPostCount(user.getId()));
            item.put("isFollowing", false);
            item.put("recommendReason", "new_user");
            return item;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> computeRecommendations(Long currentUserId) {
        List<Long> myFolloweeIds = followMapper.selectFolloweeIds(currentUserId);
        Set<Long> excludedIds = new HashSet<>(myFolloweeIds);
        excludedIds.add(currentUserId);

        List<Pet> myPets = petMapper.selectList(
            new LambdaQueryWrapper<Pet>().eq(Pet::getUserId, currentUserId));
        Set<String> myBreeds = myPets.stream()
            .map(Pet::getBreed)
            .filter(b -> b != null && !b.trim().isEmpty())
            .collect(Collectors.toSet());
        int myCategory = myPets.stream()
            .mapToInt(p -> p.getCategory() != null ? p.getCategory() : 0)
            .max().orElse(0);

        Set<Long> twoHopIds = new HashSet<>();
        for (Long followeeId : myFolloweeIds) {
            List<Long> theirFollowees = followMapper.selectFolloweeIds(followeeId);
            twoHopIds.addAll(theirFollowees);
        }
        twoHopIds.removeAll(excludedIds);

        Set<Long> interactUserIds = new HashSet<>();
        LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(PostLike::getUserId, currentUserId);
        List<PostLike> myLikes = postLikeMapper.selectList(likeWrapper);
        List<Long> myLikedPostIds = myLikes.stream()
            .map(PostLike::getPostId)
            .collect(Collectors.toList());

        if (!myLikedPostIds.isEmpty()) {
            LambdaQueryWrapper<PostLike> otherLikeWrapper = new LambdaQueryWrapper<>();
            otherLikeWrapper.in(PostLike::getPostId, myLikedPostIds)
                .ne(PostLike::getUserId, currentUserId);
            List<PostLike> otherLikes = postLikeMapper.selectList(otherLikeWrapper);
            for (PostLike ol : otherLikes) {
                if (!excludedIds.contains(ol.getUserId())) {
                    interactUserIds.add(ol.getUserId());
                }
            }
        }

        LambdaQueryWrapper<User> candidateWrapper = new LambdaQueryWrapper<>();
        candidateWrapper.eq(User::getStatus, 1);
        candidateWrapper.notIn(User::getId, excludedIds);
        List<User> candidates = userMapper.selectList(candidateWrapper);

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Double> scoreMap = new ConcurrentHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (User candidate : candidates) {
            double score = 0.0;
            Long cid = candidate.getId();

            double breedScore = computeBreedScore(cid, myBreeds, myCategory);
            score += W_BREED * breedScore;

            double activityScore = computeActivityScore(cid);
            score += W_ACTIVITY * activityScore;

            double socialScore = twoHopIds.contains(cid) ? 1.0 : 0.0;
            score += W_SOCIAL * socialScore;

            double interactScore = interactUserIds.contains(cid) ? 1.0 : 0.0;
            score += W_INTERACT * interactScore;

            double newUserScore = computeNewUserScore(candidate.getCreatedAt(), now);
            score += W_NEWUSER * newUserScore;

            scoreMap.put(cid, score);
        }

        List<Long> sortedIds = scoreMap.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        List<Long> currentFolloweeIds = followMapper.selectFolloweeIds(currentUserId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long uid : sortedIds) {
            User user = candidates.stream()
                .filter(c -> c.getId().equals(uid))
                .findFirst().orElse(null);
            if (user == null) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("id", user.getId());
            item.put("nickname", user.getNickname());
            item.put("avatar", user.getAvatar());
            item.put("gender", user.getGender());
            item.put("followerCount", followService.getFollowerCount(user.getId()));
            item.put("postCount", postService.getUserPostCount(user.getId()));
            item.put("isFollowing", currentFolloweeIds.contains(user.getId()));
            item.put("recommendScore", Math.round(scoreMap.get(uid) * 1000.0) / 1000.0);
            item.put("recommendReason", determineReason(uid, myBreeds, myCategory, twoHopIds, interactUserIds));
            result.add(item);
        }

        return result;
    }

    private double computeBreedScore(Long userId, Set<String> myBreeds, int myCategory) {
        if (myBreeds.isEmpty() && myCategory == 0) return 0.3;

        List<Pet> theirPets = petMapper.selectList(
            new LambdaQueryWrapper<Pet>().eq(Pet::getUserId, userId));
        if (theirPets.isEmpty()) return 0.1;

        double maxScore = 0.0;
        for (Pet theirPet : theirPets) {
            double petScore = 0.0;

            if (theirPet.getBreed() != null && myBreeds.contains(theirPet.getBreed())) {
                petScore = 1.0;
            } else if (theirPet.getCategory() != null && theirPet.getCategory() == myCategory && myCategory > 0) {
                petScore = 0.6;
            } else {
                petScore = 0.1;
            }

            maxScore = Math.max(maxScore, petScore);
        }
        return maxScore;
    }

    private double computeActivityScore(Long userId) {
        int followerCount = followService.getFollowerCount(userId);
        int postCount = postService.getUserPostCount(userId);

        double followerScore = Math.min(1.0, followerCount / 50.0);
        double postScore = Math.min(1.0, postCount / 20.0);

        return 0.6 * followerScore + 0.4 * postScore;
    }

    private double computeNewUserScore(LocalDateTime createdAt, LocalDateTime now) {
        if (createdAt == null) return 0.0;
        long daysSince = ChronoUnit.DAYS.between(createdAt, now);
        if (daysSince <= 3) return 1.0;
        if (daysSince <= 7) return 0.8;
        if (daysSince <= 14) return 0.5;
        if (daysSince <= 30) return 0.3;
        return 0.0;
    }

    private String determineReason(Long userId, Set<String> myBreeds, int myCategory,
                                    Set<Long> twoHopIds, Set<Long> interactUserIds) {
        if (interactUserIds.contains(userId)) return "interact";
        if (twoHopIds.contains(userId)) return "social";

        List<Pet> theirPets = petMapper.selectList(
            new LambdaQueryWrapper<Pet>().eq(Pet::getUserId, userId));
        for (Pet pet : theirPets) {
            if (pet.getBreed() != null && myBreeds.contains(pet.getBreed())) {
                return "same_breed";
            }
            if (pet.getCategory() != null && pet.getCategory() == myCategory && myCategory > 0) {
                return "same_category";
            }
        }

        return "activity";
    }

    public List<Post> recommendPosts(Long userId, int page, int size) {
        if (userId == null) {
            return postMapper.selectRecommendFeed((page - 1) * size, size);
        }

        String cacheKey = POST_CACHE_KEY_PREFIX + userId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                List<Long> cachedIds = (List<Long>) cached;
                int start = (page - 1) * size;
                int end = Math.min(start + size, cachedIds.size());
                if (start < cachedIds.size()) {
                    List<Long> pageIds = cachedIds.subList(start, end);
                    return pageIds.stream()
                        .map(id -> postMapper.selectById(id))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                }
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.warn("动态推荐缓存读取异常，重新计算: {}", e.getMessage());
        }

        List<Long> sortedIds = computePostRecommendations(userId);

        try {
            redisTemplate.opsForValue().set(cacheKey, sortedIds, POST_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("动态推荐缓存写入异常: {}", e.getMessage());
        }

        int start = (page - 1) * size;
        int end = Math.min(start + size, sortedIds.size());
        if (start < sortedIds.size()) {
            List<Long> pageIds = sortedIds.subList(start, end);
            return pageIds.stream()
                .map(id -> postMapper.selectById(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private List<Long> computePostRecommendations(Long userId) {
        List<Post> candidates = postMapper.selectCandidatePosts(CANDIDATE_LIMIT);
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<Pet> myPets = petMapper.selectList(
            new LambdaQueryWrapper<Pet>().eq(Pet::getUserId, userId));
        Set<String> myBreeds = myPets.stream()
            .map(Pet::getBreed)
            .filter(b -> b != null && !b.trim().isEmpty())
            .collect(Collectors.toSet());
        int myCategory = myPets.stream()
            .mapToInt(p -> p.getCategory() != null ? p.getCategory() : 0)
            .max().orElse(0);

        List<Long> myFolloweeIds = followMapper.selectFolloweeIds(userId);
        Set<Long> followeeSet = new HashSet<>(myFolloweeIds);

        Set<Long> twoHopIds = new HashSet<>();
        for (Long followeeId : myFolloweeIds) {
            List<Long> theirFollowees = followMapper.selectFolloweeIds(followeeId);
            twoHopIds.addAll(theirFollowees);
        }
        twoHopIds.removeAll(followeeSet);
        twoHopIds.remove(userId);

        Set<Long> likedAuthorIds = new HashSet<>();
        LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(PostLike::getUserId, userId);
        List<PostLike> myLikes = postLikeMapper.selectList(likeWrapper);
        if (!myLikes.isEmpty()) {
            List<Long> myLikedPostIds = myLikes.stream()
                .map(PostLike::getPostId)
                .collect(Collectors.toList());
            List<Post> likedPosts = postMapper.selectBatchIds(myLikedPostIds);
            for (Post lp : likedPosts) {
                if (!lp.getUserId().equals(userId) && !followeeSet.contains(lp.getUserId())) {
                    likedAuthorIds.add(lp.getUserId());
                }
            }
        }

        Map<Long, Set<String>> authorBreedCache = new HashMap<>();
        Map<Long, Integer> authorCategoryCache = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        Map<Long, Double> scoreMap = new HashMap<>();

        for (Post post : candidates) {
            double score = 0.0;
            Long authorId = post.getUserId();

            double interestScore = computePostInterestScore(authorId, myBreeds, myCategory,
                authorBreedCache, authorCategoryCache);
            score += PW_INTEREST * interestScore;

            double hotScore = computePostHotScore(post);
            score += PW_HOT * hotScore;

            double socialScore = 0.0;
            if (followeeSet.contains(authorId)) {
                socialScore = 0.5;
            } else if (twoHopIds.contains(authorId)) {
                socialScore = 1.0;
            }
            score += PW_SOCIAL * socialScore;

            double interactScore = likedAuthorIds.contains(authorId) ? 1.0 : 0.0;
            score += PW_INTERACT * interactScore;

            double freshScore = computePostFreshnessScore(post.getCreatedAt(), now);
            score += PW_FRESH * freshScore;

            double behaviorScore = computeBehaviorScore(userId, post.getId());
            score += PW_BEHAVIOR * behaviorScore;

            scoreMap.put(post.getId(), score);
        }

        return scoreMap.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private double computePostInterestScore(Long authorId, Set<String> myBreeds, int myCategory,
                                             Map<Long, Set<String>> breedCache,
                                             Map<Long, Integer> categoryCache) {
        if (myBreeds.isEmpty() && myCategory == 0) return 0.3;

        Set<String> authorBreeds = breedCache.computeIfAbsent(authorId, id -> {
            List<Pet> pets = petMapper.selectList(
                new LambdaQueryWrapper<Pet>().eq(Pet::getUserId, id));
            return pets.stream()
                .map(Pet::getBreed)
                .filter(b -> b != null && !b.trim().isEmpty())
                .collect(Collectors.toSet());
        });

        int authorCategory = categoryCache.computeIfAbsent(authorId, id -> {
            List<Pet> pets = petMapper.selectList(
                new LambdaQueryWrapper<Pet>().eq(Pet::getUserId, id));
            return pets.stream()
                .mapToInt(p -> p.getCategory() != null ? p.getCategory() : 0)
                .max().orElse(0);
        });

        if (authorBreeds.isEmpty() && authorCategory == 0) return 0.1;

        for (String breed : authorBreeds) {
            if (myBreeds.contains(breed)) return 1.0;
        }

        if (authorCategory > 0 && authorCategory == myCategory) return 0.6;

        return 0.1;
    }

    private double computePostHotScore(Post post) {
        int likeCount = post.getLikeCount() != null ? post.getLikeCount() : 0;
        int commentCount = post.getCommentCount() != null ? post.getCommentCount() : 0;
        int shareCount = post.getShareCount() != null ? post.getShareCount() : 0;

        double rawHot = likeCount * 3.0 + commentCount * 2.0 + shareCount;

        long hoursSince = 1;
        if (post.getCreatedAt() != null) {
            hoursSince = ChronoUnit.HOURS.between(post.getCreatedAt(), LocalDateTime.now());
            hoursSince = Math.max(1, hoursSince);
        }

        double hotValue = rawHot / (hoursSince + 2);

        return Math.min(1.0, hotValue / 10.0);
    }

    private double computePostFreshnessScore(LocalDateTime createdAt, LocalDateTime now) {
        if (createdAt == null) return 0.0;
        long hoursSince = ChronoUnit.HOURS.between(createdAt, now);
        if (hoursSince <= 1) return 1.0;
        if (hoursSince <= 6) return 0.9;
        if (hoursSince <= 24) return 0.7;
        if (hoursSince <= 72) return 0.5;
        if (hoursSince <= 168) return 0.3;
        if (hoursSince <= 360) return 0.1;
        return 0.0;
    }

    public void invalidatePostCache(Long userId) {
        try {
            redisTemplate.delete(POST_CACHE_KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("清除动态推荐缓存异常: {}", e.getMessage());
        }
    }

    public void invalidateCache(Long userId) {
        try {
            redisTemplate.delete(CACHE_KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("清除推荐缓存异常: {}", e.getMessage());
        }
    }

    private double computeBehaviorScore(Long userId, Long postId) {
        try {
            List<Long> recentViewed = userBehaviorService.getRecentViewedPostIds(userId, 50);
            if (recentViewed.contains(postId)) {
                return 0.8;
            }
            List<Long> recentCollected = userBehaviorService.getRecentCollectedPostIds(userId, 20);
            if (recentCollected.contains(postId)) {
                return 1.0;
            }
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}

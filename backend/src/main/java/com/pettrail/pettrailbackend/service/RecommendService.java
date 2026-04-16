package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.entity.PostLike;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.FollowMapper;
import com.pettrail.pettrailbackend.mapper.PetMapper;
import com.pettrail.pettrailbackend.mapper.PostLikeMapper;
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
    private final FollowService followService;
    private final PostService postService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "recommend:";
    private static final long CACHE_TTL_HOURS = 2;

    private static final double W_BREED = 0.30;
    private static final double W_ACTIVITY = 0.25;
    private static final double W_SOCIAL = 0.20;
    private static final double W_INTERACT = 0.15;
    private static final double W_NEWUSER = 0.10;

    public List<Map<String, Object>> recommendUsers(Long currentUserId, int page, int size) {
        if (currentUserId == null) {
            return recommendForAnonymous(page, size);
        }

        String cacheKey = CACHE_KEY_PREFIX + currentUserId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> cachedList = (List<Map<String, Object>>) cached;
                int start = (page - 1) * size;
                int end = Math.min(start + size, cachedList.size());
                if (start < cachedList.size()) {
                    return cachedList.subList(start, end);
                }
                return Collections.emptyList();
            } catch (Exception e) {
                log.warn("推荐缓存读取异常，重新计算: {}", e.getMessage());
            }
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
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
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

    public void invalidateCache(Long userId) {
        try {
            redisTemplate.delete(CACHE_KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("清除推荐缓存异常: {}", e.getMessage());
        }
    }
}

package com.pettrail.pettrailbackend.converter;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.PostVO;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.PostEe;
import com.pettrail.pettrailbackend.entity.PostLike;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.PetMapper;
import com.pettrail.pettrailbackend.mapper.PostEeMapper;
import com.pettrail.pettrailbackend.mapper.PostLikeMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostConverter {

    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostEeMapper postEeMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public List<PostVO> convertToPostVOList(List<Post> posts, Long currentUserId) {
        if (posts == null || posts.isEmpty()) return Collections.emptyList();

        List<Post> activePosts = posts.stream()
                .filter(post -> post.getDeleted() == null || post.getDeleted() == 0)
                .toList();
        if (activePosts.isEmpty()) return Collections.emptyList();

        Map<Long, User> userMap = batchLoadUsers(activePosts);
        Map<Long, Pet> petMap = batchLoadPets(activePosts);
        Set<Long> likedPostIds = batchLoadLikedPostIds(activePosts, currentUserId);
        Set<Long> eeLikedPostIds = batchLoadEeLikedPostIds(activePosts, currentUserId);

        return activePosts.stream()
                .map(post -> convertToPostVO(post, currentUserId, userMap, petMap, likedPostIds, eeLikedPostIds))
                .collect(Collectors.toList());
    }

    public PostVO convertToPostVO(Post post, Long currentUserId) {
        Map<Long, User> userMap = batchLoadUsers(List.of(post));
        Map<Long, Pet> petMap = batchLoadPets(List.of(post));
        Set<Long> likedPostIds = batchLoadLikedPostIds(List.of(post), currentUserId);
        Set<Long> eeLikedPostIds = batchLoadEeLikedPostIds(List.of(post), currentUserId);

        return convertToPostVO(post, currentUserId, userMap, petMap, likedPostIds, eeLikedPostIds);
    }

    private Map<Long, User> batchLoadUsers(List<Post> posts) {
        Set<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return Map.of();

        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
    }

    private Map<Long, Pet> batchLoadPets(List<Post> posts) {
        Set<Long> petIds = posts.stream()
                .map(Post::getPetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (petIds.isEmpty()) return Map.of();

        List<Pet> pets = petMapper.selectBatchIds(petIds);
        return pets.stream()
                .collect(Collectors.toMap(Pet::getId, p -> p, (a, b) -> a));
    }

    private Set<Long> batchLoadLikedPostIds(List<Post> posts, Long currentUserId) {
        if (currentUserId == null) return Set.of();

        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .filter(Objects::nonNull)
                .toList();
        if (postIds.isEmpty()) return Set.of();

        Set<Long> likedIds = new HashSet<>();
        for (Long postId : postIds) {
            String userLikeKey = "post:user:like:" + postId + ":" + currentUserId;
            Boolean exists = redisTemplate.hasKey(userLikeKey);
            if (Boolean.TRUE.equals(exists)) {
                likedIds.add(postId);
            }
        }

        List<Long> missedPostIds = postIds.stream()
                .filter(id -> !likedIds.contains(id))
                .toList();
        if (!missedPostIds.isEmpty()) {
            LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PostLike::getUserId, currentUserId)
                    .in(PostLike::getPostId, missedPostIds)
                    .select(PostLike::getPostId);
            List<PostLike> dbLikes = postLikeMapper.selectList(wrapper);
            for (PostLike like : dbLikes) {
                likedIds.add(like.getPostId());
            }
        }

        return likedIds;
    }

    private Set<Long> batchLoadEeLikedPostIds(List<Post> posts, Long currentUserId) {
        if (currentUserId == null) return Set.of();

        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .filter(Objects::nonNull)
                .toList();
        if (postIds.isEmpty()) return Set.of();

        Set<Long> eeLikedIds = new HashSet<>();
        for (Long postId : postIds) {
            String userEeKey = "post:user:ee:" + postId + ":" + currentUserId;
            Boolean exists = redisTemplate.hasKey(userEeKey);
            if (Boolean.TRUE.equals(exists)) {
                eeLikedIds.add(postId);
            }
        }

        List<Long> missedPostIds = postIds.stream()
                .filter(id -> !eeLikedIds.contains(id))
                .toList();
        if (!missedPostIds.isEmpty()) {
            LambdaQueryWrapper<PostEe> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PostEe::getUserId, currentUserId)
                    .in(PostEe::getPostId, missedPostIds)
                    .select(PostEe::getPostId);
            List<PostEe> dbEes = postEeMapper.selectList(wrapper);
            for (PostEe ee : dbEes) {
                eeLikedIds.add(ee.getPostId());
            }
        }

        return eeLikedIds;
    }

    private PostVO convertToPostVO(Post post, Long userId, Map<Long, User> userMap,
                                    Map<Long, Pet> petMap, Set<Long> likedPostIds,
                                    Set<Long> eeLikedPostIds) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setPetId(post.getPetId());
        vo.setContent(post.getContent());
        vo.setImages(post.getImages());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setShareCount(post.getShareCount());
        vo.setLocation(post.getLocation());

        vo.setStickers(parseJsonArray(post.getStickers()));
        vo.setBubble(parseBubble(post.getBubble()));
        vo.setTags(parseJsonArray(post.getTags()));
        vo.setImageList(parseJsonArray(post.getImages()));
        vo.setVideoList(parseJsonArray(post.getVideos()));

        vo.setStatus(post.getStatus());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());

        fillUserInfo(vo, post, userMap);
        fillPetInfo(vo, post, petMap);
        fillInteractionInfo(vo, post, likedPostIds, eeLikedPostIds);

        return vo;
    }

    private void fillUserInfo(PostVO vo, Post post, Map<Long, User> userMap) {
        User user = userMap.get(post.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickname() != null ? user.getNickname() : "萌宠主人");
            vo.setUserAvatar(user.getAvatar() != null ? user.getAvatar() : "");
        } else {
            vo.setUserName("未知用户");
            vo.setUserAvatar("");
        }
    }

    private void fillPetInfo(PostVO vo, Post post, Map<Long, Pet> petMap) {
        if (post.getPetId() == null) {
            vo.setPetName("");
            vo.setPetAvatar("");
            vo.setPetType(0);
            vo.setPetAge("");
            return;
        }

        Pet pet = petMap.get(post.getPetId());
        if (pet != null) {
            vo.setPetName(pet.getName() != null ? pet.getName() : "未知宠物");
            vo.setPetAvatar(pet.getAvatar() != null ? pet.getAvatar() : "");
            vo.setPetType(pet.getCategory() != null ? pet.getCategory() : 0);
            vo.setPetAge(calculatePetAge(pet.getBirthday()));
        } else {
            vo.setPetName("未知宠物");
            vo.setPetAvatar("");
            vo.setPetType(0);
            vo.setPetAge("");
        }
    }

    private void fillInteractionInfo(PostVO vo, Post post, Set<Long> likedPostIds,
                                      Set<Long> eeLikedPostIds) {
        vo.setLiked(likedPostIds.contains(post.getId()));
        vo.setEeLiked(eeLikedPostIds.contains(post.getId()));
        vo.setEeCount(post.getEeCount() != null ? post.getEeCount() : 0);
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("null")) {
            return List.of();
        }
        try {
            return JSON.parseArray(json, String.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    private PostVO.BubbleVO parseBubble(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("null")) {
            return null;
        }
        try {
            return JSON.parseObject(json, PostVO.BubbleVO.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String calculatePetAge(LocalDate birthday) {
        if (birthday == null) return "";
        Period period = Period.between(birthday, LocalDate.now());
        int years = period.getYears();
        int months = period.getMonths();
        if (years > 0) {
            return months > 0 ? years + "岁" + months + "个月" : years + "岁";
        } else if (months > 0) {
            return months + "个月";
        } else {
            return Math.max(period.getDays(), 0) + "天";
        }
    }

}

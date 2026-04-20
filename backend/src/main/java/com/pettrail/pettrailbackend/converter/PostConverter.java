package com.pettrail.pettrailbackend.converter;

import com.alibaba.fastjson2.JSON;
import com.pettrail.pettrailbackend.dto.PostVO;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.service.PetService;
import com.pettrail.pettrailbackend.service.PostService;
import com.pettrail.pettrailbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostConverter {

    private final UserService userService;
    private final PetService petService;
    private final PostService postService;

    public List<PostVO> convertToPostVOList(List<Post> posts, Long currentUserId) {
        if (posts == null || posts.isEmpty()) return Collections.emptyList();

        Map<Long, User> userMap = batchLoadUsers(posts);
        Map<Long, Pet> petMap = batchLoadPets(posts);

        return posts.stream()
                .map(post -> convertToPostVO(post, currentUserId, userMap, petMap))
                .collect(Collectors.toList());
    }

    public PostVO convertToPostVO(Post post, Long currentUserId) {
        Map<Long, User> userMap = new HashMap<>();
        try {
            User user = userService.getProfile(post.getUserId());
            if (user != null) userMap.put(post.getUserId(), user);
        } catch (Exception e) {
            log.warn("查询用户信息失败: userId={}", post.getUserId());
        }

        Map<Long, Pet> petMap = new HashMap<>();
        if (post.getPetId() != null) {
            try {
                Pet pet = petService.getPetById(post.getPetId());
                if (pet != null) petMap.put(post.getPetId(), pet);
            } catch (Exception e) {
                log.warn("查询宠物信息失败: petId={}", post.getPetId());
            }
        }

        return convertToPostVO(post, currentUserId, userMap, petMap);
    }

    private Map<Long, User> batchLoadUsers(List<Post> posts) {
        Set<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = new HashMap<>();
        for (Long uid : userIds) {
            try {
                User user = userService.getProfile(uid);
                if (user != null) userMap.put(uid, user);
            } catch (Exception e) {
                log.warn("批量加载用户失败: uid={}", uid);
            }
        }
        return userMap;
    }

    private Map<Long, Pet> batchLoadPets(List<Post> posts) {
        Set<Long> petIds = posts.stream()
                .map(Post::getPetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Pet> petMap = new HashMap<>();
        for (Long pid : petIds) {
            try {
                Pet pet = petService.getPetById(pid);
                if (pet != null) petMap.put(pid, pet);
            } catch (Exception e) {
                log.warn("批量加载宠物失败: pid={}", pid);
            }
        }
        return petMap;
    }

    private PostVO convertToPostVO(Post post, Long userId, Map<Long, User> userMap, Map<Long, Pet> petMap) {
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
        fillInteractionInfo(vo, post, userId);

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

    private void fillInteractionInfo(PostVO vo, Post post, Long userId) {
        if (userId != null) {
            vo.setLiked(postService.isUserLiked(post.getId(), userId));
            vo.setEeLiked(postService.isUserEeLiked(post.getId(), userId));
        } else {
            vo.setLiked(false);
            vo.setEeLiked(false);
        }
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

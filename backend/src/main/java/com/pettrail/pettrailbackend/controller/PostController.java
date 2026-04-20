package com.pettrail.pettrailbackend.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.pettrail.pettrailbackend.dto.CommentVO;
import com.pettrail.pettrailbackend.dto.PostVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.exception.NotFoundException;
import com.pettrail.pettrailbackend.service.CommentService;
import com.pettrail.pettrailbackend.service.RecommendService;
import com.pettrail.pettrailbackend.service.UserService;
import com.pettrail.pettrailbackend.service.PetService;
import com.pettrail.pettrailbackend.service.TagService;
import com.pettrail.pettrailbackend.service.UserBehaviorService;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.Tag;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import com.pettrail.pettrailbackend.service.PostService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final RecommendService recommendService;
    private final UserService userService;
    private final PetService petService;
    private final PostMapper postMapper;
    private final TagService tagService;
    private final UserBehaviorService userBehaviorService;

    /**
     * 发布动态
     */
    @PostMapping
    public Result<Post> createPost(
            @RequestBody JSONObject data) {

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        String content = data.getString("content");
        Long petId = data.getLong("petId");
        List<String> images = data.getJSONArray("images") != null
            ? data.getJSONArray("images").toList(String.class)
            : null;
        List<String> videos = data.getJSONArray("videos") != null
            ? data.getJSONArray("videos").toList(String.class)
            : null;

        List<String> stickers = data.getJSONArray("stickers") != null
            ? data.getJSONArray("stickers").toList(String.class)
            : null;

        Map<String, String> bubble = null;
        JSONObject bubbleObj = data.getJSONObject("bubble");
        if (bubbleObj != null) {
            bubble = new HashMap<>();
            if (bubbleObj.getString("text") != null) bubble.put("text", bubbleObj.getString("text"));
            if (bubbleObj.getString("bgColor") != null) bubble.put("bgColor", bubbleObj.getString("bgColor"));
            if (bubbleObj.getString("textColor") != null) bubble.put("textColor", bubbleObj.getString("textColor"));
        }

        String location = data.getString("location");

        List<String> tagNames = data.getJSONArray("tags") != null
            ? data.getJSONArray("tags").toList(String.class)
            : null;

        try {
            Post post = postService.createPost(userId, petId, content, images, videos, stickers, bubble, location);

            if (tagNames != null && !tagNames.isEmpty()) {
                tagService.bindTagsToPost(post.getId(), tagNames);
                post.setTags(JSON.toJSONString(tagNames));
                postMapper.updateById(post);
            }

            postService.publishPostCreateEvent(post);
            userBehaviorService.recordPublish(userId, post.getId());
            return Result.success(post);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("违规")) {
                return Result.error(451, e.getMessage());
            }
            throw e;
        }
    }

    /**
     * 获取动态列表
     */
    @GetMapping("/feed")
    public Result<List<PostVO>> getFeed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String tab) {

        Long userId = UserContext.getCurrentUserId();
        
        log.info("获取动态列表 - page: {}, size: {}, tab: {}, userId: {}", page, size, tab, userId);
        
        if (userId == null && !"all".equals(tab) && !"recommend".equals(tab)) {
            log.info("用户未登录，Tab={} 返回空数据", tab);
            return Result.success(java.util.Collections.emptyList());
        }
        
        List<Post> posts;
        if ("recommend".equals(tab)) {
            posts = recommendService.recommendPosts(userId, page, size);
        } else {
            posts = postService.getFeed(page, size, tab, userId);
        }
        List<PostVO> postVOs = convertToPostVOList(posts, userId);
        log.info("获取动态列表成功，Tab={}，返回{}条数据", tab, postVOs.size());
        return Result.success(postVOs);
    }

    private List<PostVO> convertToPostVOList(List<Post> posts, Long currentUserId) {
        if (posts.isEmpty()) return Collections.emptyList();

        Set<Long> userIds = posts.stream().map(Post::getUserId).collect(Collectors.toSet());
        Set<Long> petIds = posts.stream().map(Post::getPetId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, User> userMap = new HashMap<>();
        for (Long uid : userIds) {
            try {
                userMap.put(uid, userService.getProfile(uid));
            } catch (Exception e) {
                log.warn("批量预加载用户失败: uid={}", uid);
            }
        }

        Map<Long, Pet> petMap = new HashMap<>();
        for (Long pid : petIds) {
            try {
                petMap.put(pid, petService.getPetById(pid));
            } catch (Exception e) {
                log.warn("批量预加载宠物失败: pid={}", pid);
            }
        }

        return posts.stream()
            .map(post -> convertToPostVO(post, currentUserId, userMap, petMap))
            .collect(Collectors.toList());
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

        if (post.getStickers() != null && !post.getStickers().trim().isEmpty() && !post.getStickers().equals("null")) {
            try {
                vo.setStickers(JSON.parseArray(post.getStickers(), String.class));
            } catch (Exception e) {
                vo.setStickers(List.of());
            }
        } else {
            vo.setStickers(List.of());
        }

        if (post.getBubble() != null && !post.getBubble().trim().isEmpty() && !post.getBubble().equals("null")) {
            try {
                vo.setBubble(JSON.parseObject(post.getBubble(), PostVO.BubbleVO.class));
            } catch (Exception e) {
                vo.setBubble(null);
            }
        }

        if (post.getTags() != null && !post.getTags().trim().isEmpty() && !post.getTags().equals("null")) {
            try {
                vo.setTags(JSON.parseArray(post.getTags(), String.class));
            } catch (Exception e) {
                vo.setTags(List.of());
            }
        } else {
            vo.setTags(List.of());
        }

        vo.setStatus(post.getStatus());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());

        if (post.getImages() != null && !post.getImages().trim().isEmpty() && !post.getImages().equals("null")) {
            try {
                vo.setImageList(JSON.parseArray(post.getImages(), String.class));
            } catch (Exception e) {
                vo.setImageList(List.of());
            }
        } else {
            vo.setImageList(List.of());
        }

        vo.setVideos(post.getVideos());
        if (post.getVideos() != null && !post.getVideos().trim().isEmpty() && !post.getVideos().equals("null")) {
            try {
                vo.setVideoList(JSON.parseArray(post.getVideos(), String.class));
            } catch (Exception e) {
                vo.setVideoList(List.of());
            }
        } else {
            vo.setVideoList(List.of());
        }

        try {
            User user = userMap.get(post.getUserId());
            if (user != null) {
                vo.setUserName(user.getNickname() != null ? user.getNickname() : "萌宠主人");
                vo.setUserAvatar(user.getAvatar() != null ? user.getAvatar() : "");
            } else {
                vo.setUserName("未知用户");
                vo.setUserAvatar("");
            }
        } catch (Exception e) {
            log.error("查询用户信息失败：userId={}", post.getUserId(), e);
            vo.setUserName("未知用户");
            vo.setUserAvatar("");
        }

        if (post.getPetId() != null) {
            try {
                Pet pet = petMap.get(post.getPetId());
                if (pet != null) {
                    vo.setPetName(pet.getName() != null ? pet.getName() : "未知宠物");
                    vo.setPetAvatar(pet.getAvatar() != null ? pet.getAvatar() : "");
                    vo.setPetType(pet.getCategory());
                    if (pet.getBirthday() != null) {
                        Period period = Period.between(pet.getBirthday(), LocalDate.now());
                        int years = period.getYears();
                        int months = period.getMonths();
                        if (years > 0) {
                            vo.setPetAge(months > 0 ? years + "岁" + months + "个月" : years + "岁");
                        } else if (months > 0) {
                            vo.setPetAge(months + "个月");
                        } else {
                            vo.setPetAge(Math.max(period.getDays(), 0) + "天");
                        }
                    } else {
                        vo.setPetAge("");
                    }
                } else {
                    vo.setPetName("未知宠物");
                    vo.setPetAvatar("");
                    vo.setPetType(0);
                    vo.setPetAge("");
                }
            } catch (Exception e) {
                log.error("查询宠物信息失败：petId={}", post.getPetId(), e);
                vo.setPetName("未知宠物");
                vo.setPetAvatar("");
                vo.setPetType(0);
                vo.setPetAge("");
            }
        } else {
            vo.setPetName("");
            vo.setPetAvatar("");
            vo.setPetType(0);
            vo.setPetAge("");
        }

        if (userId != null) {
            vo.setLiked(postService.isUserLiked(post.getId(), userId));
            vo.setEeLiked(postService.isUserEeLiked(post.getId(), userId));
            vo.setEeCount(post.getEeCount() != null ? post.getEeCount() : 0);
        } else {
            vo.setLiked(false);
            vo.setEeLiked(false);
            vo.setEeCount(post.getEeCount() != null ? post.getEeCount() : 0);
        }

        return vo;
    }

    /**
     * 获取动态详情
     */
    @GetMapping("/{id}")
    public Result<PostVO> getPostDetail(@PathVariable Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        Post post = postService.getPostDetail(id);

        Map<Long, User> userMap = new HashMap<>();
        try {
            userMap.put(post.getUserId(), userService.getProfile(post.getUserId()));
        } catch (Exception e) {
            log.warn("查询用户信息失败: userId={}", post.getUserId());
        }

        Map<Long, Pet> petMap = new HashMap<>();
        if (post.getPetId() != null) {
            try {
                petMap.put(post.getPetId(), petService.getPetById(post.getPetId()));
            } catch (Exception e) {
                log.warn("查询宠物信息失败: petId={}", post.getPetId());
            }
        }

        PostVO vo = convertToPostVO(post, currentUserId, userMap, petMap);
        return Result.success(vo);
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("liked", false);
            return Result.error(401, "用户未登录");
        }

        // 切换点赞状态
        boolean isNowLiked = postService.toggleLike(id, userId);

        Long likeCount = postService.getLikeCountFromCache(id);

        if (isNowLiked) {
            userBehaviorService.recordLike(userId, id);
        }

        Map<String, Object> result = new HashMap<>();
        // isNowLiked 表示操作后的状态：true=已点赞，false=已取消点赞
        result.put("liked", isNowLiked);
        result.put("likeCount", likeCount != null ? likeCount : 0);

        log.info("点赞操作 - postId: {}, userId: {}, isNowLiked: {}, likeCount: {}",
            id, userId, isNowLiked, likeCount);

        return Result.success(result);
    }

    /**
     * 收藏/取消收藏
     */
    @PostMapping("/{id}/ee")
    public Result<Map<String, Object>> toggleEe(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("eeLiked", false);
            return Result.error(401, "用户未登录");
        }

        boolean isNowEeLiked = postService.toggleEe(id, userId);

        if (isNowEeLiked) {
            userBehaviorService.recordCollect(userId, id);
        }

        Post post = postMapper.selectById(id);
        int eeCount = post != null ? (post.getEeCount() != null ? post.getEeCount() : 0) : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("eeLiked", isNowEeLiked);
        result.put("eeCount", eeCount);

        log.info("收藏操作 - postId: {}, userId: {}, isNowEeLiked: {}, eeCount: {}",
            id, userId, isNowEeLiked, eeCount);

        return Result.success(result);
    }

    @GetMapping("/user/{userId}")
    public Result<List<PostVO>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long currentUserId = UserContext.getCurrentUserId();
        int offset = (page - 1) * size;

        List<Post> posts = postMapper.selectByUserId(userId, offset, size);
        List<PostVO> postVOs = convertToPostVOList(posts, currentUserId);

        return Result.success(postVOs);
    }

    @GetMapping("/user/{userId}/likes")
    public Result<List<PostVO>> getUserLikedPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long currentUserId = UserContext.getCurrentUserId();
        int offset = (page - 1) * size;

        List<Post> posts = postMapper.selectLikedFeed(userId, offset, size);
        List<PostVO> postVOs = convertToPostVOList(posts, currentUserId);

        return Result.success(postVOs);
    }

    @PostMapping("/{id}/comments")
    public Result<CommentVO> createComment(
            @PathVariable Long id,
            @RequestBody JSONObject data) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        String content = data.getString("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error(400, "评论内容不能为空");
        }

        Long parentId = data.getLong("parentId");
        Long replyToId = data.getLong("replyToId");

        try {
            CommentVO comment = commentService.createComment(id, userId, content.trim(), parentId, replyToId);
            return Result.success(comment);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("违规")) {
                return Result.error(451, e.getMessage());
            }
            throw e;
        }
    }

    @GetMapping("/{id}/comments")
    public Result<List<CommentVO>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<CommentVO> comments = commentService.getComments(id, page, size);
        return Result.success(comments);
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public Result<String> deleteComment(@PathVariable Long id, @PathVariable Long commentId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        try {
            commentService.deleteComment(commentId, userId);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("无权")) {
                return Result.error(403, e.getMessage());
            }
            if (e.getMessage() != null && e.getMessage().contains("不存在")) {
                return Result.error(404, e.getMessage());
            }
            return Result.error("删除失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("删除评论失败：{}", e.getMessage(), e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/share")
    public Result<Map<String, Object>> sharePost(@PathVariable Long id) {
        try {
            int shareCount = postService.incrementShareCount(id);
            Map<String, Object> result = new HashMap<>();
            result.put("shareCount", shareCount);
            return Result.success(result);
        } catch (NotFoundException e) {
            return Result.error(404, "动态不存在");
        } catch (Exception e) {
            log.error("分享统计失败：{}", e.getMessage(), e);
            return Result.error("分享失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deletePost(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        try {
            postService.deletePost(id, userId);
            return Result.success("删除成功");
        } catch (NotFoundException e) {
            return Result.error(404, "动态不存在");
        } catch (RuntimeException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            log.error("删除动态失败：{}", e.getMessage(), e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}

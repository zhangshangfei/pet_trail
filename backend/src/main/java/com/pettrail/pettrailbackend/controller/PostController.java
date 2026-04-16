package com.pettrail.pettrailbackend.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pettrail.pettrailbackend.dto.CommentVO;
import com.pettrail.pettrailbackend.dto.PostVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.service.CommentService;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.PetMapper;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import com.pettrail.pettrailbackend.service.PostService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 动态控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final PostMapper postMapper;

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
            ? data.getJSONArray("images").toJavaList(String.class)
            : null;
        List<String> videos = data.getJSONArray("videos") != null
            ? data.getJSONArray("videos").toJavaList(String.class)
            : null;

        Post post = postService.createPost(userId, petId, content, images, videos);

        // 异步发送消息（内容审核、推送粉丝等）
        postService.publishPostCreateEvent(post);

        return Result.success(post);
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
        
        List<Post> posts = postService.getFeed(page, size, tab, userId);
        List<PostVO> postVOs = posts.stream()
            .map(post -> convertToPostVO(post, userId))
            .collect(Collectors.toList());
        log.info("获取动态列表成功，Tab={}，返回{}条数据", tab, postVOs.size());
        return Result.success(postVOs);
    }

    private PostVO convertToPostVO(Post post, Long userId) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setPetId(post.getPetId());
        vo.setContent(post.getContent());
        vo.setImages(post.getImages());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setShareCount(post.getShareCount());
        vo.setStatus(post.getStatus());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());

        if (post.getImages() != null && !post.getImages().equals("null")) {
            try {
                vo.setImageList(JSON.parseArray(post.getImages(), String.class));
            } catch (Exception e) {
                vo.setImageList(List.of());
            }
        } else {
            vo.setImageList(List.of());
        }

        vo.setVideos(post.getVideos());
        if (post.getVideos() != null && !post.getVideos().equals("null")) {
            try {
                vo.setVideoList(JSON.parseArray(post.getVideos(), String.class));
            } catch (Exception e) {
                vo.setVideoList(List.of());
            }
        } else {
            vo.setVideoList(List.of());
        }

        try {
            User user = userMapper.selectById(post.getUserId());
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
                Pet pet = petMapper.selectById(post.getPetId());
                if (pet != null) {
                    vo.setPetName(pet.getName() != null ? pet.getName() : "未知宠物");
                    vo.setPetAvatar(pet.getAvatar() != null ? pet.getAvatar() : "");
                    vo.setPetType(pet.getCategory());
                    if (pet.getBirthday() != null) {
                        int age = Period.between(pet.getBirthday(), LocalDate.now()).getYears();
                        vo.setPetAge(Math.max(age, 0));
                    } else {
                        vo.setPetAge(0);
                    }
                } else {
                    vo.setPetName("未知宠物");
                    vo.setPetAvatar("");
                    vo.setPetType(0);
                    vo.setPetAge(0);
                }
            } catch (Exception e) {
                log.error("查询宠物信息失败：petId={}", post.getPetId(), e);
                vo.setPetName("未知宠物");
                vo.setPetAvatar("");
                vo.setPetType(0);
                vo.setPetAge(0);
            }
        } else {
            vo.setPetName("");
            vo.setPetAvatar("");
            vo.setPetType(0);
            vo.setPetAge(0);
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
    public Result<Post> getPostDetail(@PathVariable Long id) {
        Post post = postService.getPostDetail(id);
        return Result.success(post);
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

        // 获取当前点赞数 - 使用 Redis 原子操作优化
        Long likeCount = postService.getLikeCountFromCache(id);

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

        // 切换收藏状态（内部已经更新了数据库）
        boolean isNowEeLiked = postService.toggleEe(id, userId);

        // 直接从数据库查询最新的收藏数（避免缓存问题）
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
        List<PostVO> postVOs = posts.stream()
            .map(post -> convertToPostVO(post, currentUserId))
            .collect(Collectors.toList());

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
        List<PostVO> postVOs = posts.stream()
            .map(post -> convertToPostVO(post, currentUserId))
            .collect(Collectors.toList());

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

        CommentVO comment = commentService.createComment(id, userId, content.trim(), parentId, replyToId);
        return Result.success(comment);
    }

    @GetMapping("/{id}/comments")
    public Result<List<CommentVO>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<CommentVO> comments = commentService.getComments(id, page, size);
        return Result.success(comments);
    }
}

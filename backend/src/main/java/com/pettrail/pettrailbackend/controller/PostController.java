package com.pettrail.pettrailbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.pettrail.pettrailbackend.dto.PostVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.service.PostService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

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

        Post post = postService.createPost(userId, petId, content, images);

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
            @RequestParam(defaultValue = "20") int size) {

        Long userId = UserContext.getCurrentUserId();
        List<Post> posts = postService.getFeed(page, size);
        
        // 转换为 VO，包含用户是否已点赞的信息
        List<PostVO> postVOs = posts.stream()
            .map(post -> {
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
                
                // 检查当前用户是否已点赞
                if (userId != null) {
                    boolean isLiked = postService.isUserLiked(post.getId(), userId);
                    vo.setLiked(isLiked);
                } else {
                    vo.setLiked(false);
                }
                
                return vo;
            })
            .collect(java.util.stream.Collectors.toList());
        
        return Result.success(postVOs);
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
        
        // 获取当前点赞数
        Post post = postService.getPostDetail(id);
        int likeCount = post != null ? post.getLikeCount() : 0;

        Map<String, Object> result = new HashMap<>();
        // isNowLiked 表示操作后的状态：true=已点赞，false=已取消点赞
        result.put("liked", isNowLiked);
        result.put("likeCount", likeCount);

        log.info("点赞操作 - postId: {}, userId: {}, isNowLiked: {}, likeCount: {}", 
            id, userId, isNowLiked, likeCount);

        return Result.success(result);
    }
}

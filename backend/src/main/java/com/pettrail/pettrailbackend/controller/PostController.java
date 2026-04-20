package com.pettrail.pettrailbackend.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.pettrail.pettrailbackend.converter.PostConverter;
import com.pettrail.pettrailbackend.dto.PostVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.exception.NotFoundException;
import com.pettrail.pettrailbackend.service.PostService;
import com.pettrail.pettrailbackend.service.RecommendService;
import com.pettrail.pettrailbackend.service.TagService;
import com.pettrail.pettrailbackend.service.UserBehaviorService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController extends BaseController {

    private final PostService postService;
    private final RecommendService recommendService;
    private final PostConverter postConverter;
    private final TagService tagService;
    private final UserBehaviorService userBehaviorService;

    @PostMapping
    public Result<Post> createPost(@RequestBody JSONObject data) {
        Long userId = requireLogin();

        String content = data.getString("content");
        Long petId = data.getLong("petId");
        List<String> images = data.getJSONArray("images") != null ? data.getJSONArray("images").toList(String.class) : null;
        List<String> videos = data.getJSONArray("videos") != null ? data.getJSONArray("videos").toList(String.class) : null;
        List<String> stickers = data.getJSONArray("stickers") != null ? data.getJSONArray("stickers").toList(String.class) : null;

        Map<String, String> bubble = null;
        JSONObject bubbleObj = data.getJSONObject("bubble");
        if (bubbleObj != null) {
            bubble = new HashMap<>();
            if (bubbleObj.getString("text") != null) bubble.put("text", bubbleObj.getString("text"));
            if (bubbleObj.getString("bgColor") != null) bubble.put("bgColor", bubbleObj.getString("bgColor"));
            if (bubbleObj.getString("textColor") != null) bubble.put("textColor", bubbleObj.getString("textColor"));
        }

        String location = data.getString("location");
        List<String> tagNames = data.getJSONArray("tags") != null ? data.getJSONArray("tags").toList(String.class) : null;

        Post post = postService.createPost(userId, petId, content, images, videos, stickers, bubble, location);

        if (tagNames != null && !tagNames.isEmpty()) {
            tagService.bindTagsToPost(post.getId(), tagNames);
            post.setTags(JSON.toJSONString(tagNames));
            postService.updatePost(post);
        }

        postService.publishPostCreateEvent(post);
        userBehaviorService.recordPublish(userId, post.getId());
        return Result.success(post);
    }

    @GetMapping("/feed")
    public Result<List<PostVO>> getFeed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String tab) {
        Long userId = UserContext.getCurrentUserId();

        if (userId == null && !"all".equals(tab) && !"recommend".equals(tab)) {
            return Result.success(java.util.Collections.emptyList());
        }

        List<Post> posts = "recommend".equals(tab)
                ? recommendService.recommendPosts(userId, page, size)
                : postService.getFeed(page, size, tab, userId);

        return Result.success(postConverter.convertToPostVOList(posts, userId));
    }

    @GetMapping("/{id}")
    public Result<PostVO> getPostDetail(@PathVariable Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        Post post = postService.getPostDetail(id);
        return Result.success(postConverter.convertToPostVO(post, currentUserId));
    }

    @GetMapping("/user/{userId}")
    public Result<List<PostVO>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = UserContext.getCurrentUserId();
        List<Post> posts = postService.getUserPosts(userId, page, size);
        return Result.success(postConverter.convertToPostVOList(posts, currentUserId));
    }

    @GetMapping("/user/{userId}/likes")
    public Result<List<PostVO>> getUserLikedPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = UserContext.getCurrentUserId();
        List<Post> posts = postService.getUserLikedPosts(userId, page, size);
        return Result.success(postConverter.convertToPostVOList(posts, currentUserId));
    }

    @DeleteMapping("/{id}")
    public Result<String> deletePost(@PathVariable Long id) {
        Long userId = requireLogin();
        postService.deletePost(id, userId);
        return Result.success("删除成功");
    }
}

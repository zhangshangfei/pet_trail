package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.CommentCreateDTO;
import com.pettrail.pettrailbackend.dto.CommentVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.exception.NotFoundException;
import com.pettrail.pettrailbackend.service.CommentService;
import com.pettrail.pettrailbackend.service.PostService;
import com.pettrail.pettrailbackend.service.UserBehaviorService;
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
public class PostInteractionController extends BaseController {

    private final PostService postService;
    private final CommentService commentService;
    private final UserBehaviorService userBehaviorService;

    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long id) {
        Long userId = requireLogin();
        boolean isNowLiked = postService.toggleLike(id, userId);
        Long likeCount = postService.getLikeCountFromCache(id);

        if (isNowLiked) {
            userBehaviorService.recordLike(userId, id);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("liked", isNowLiked);
        result.put("likeCount", likeCount != null ? likeCount : 0);
        return Result.success(result);
    }

    @PostMapping("/{id}/ee")
    public Result<Map<String, Object>> toggleEe(@PathVariable Long id) {
        Long userId = requireLogin();
        boolean isNowEeLiked = postService.toggleEe(id, userId);

        if (isNowEeLiked) {
            userBehaviorService.recordCollect(userId, id);
        }

        com.pettrail.pettrailbackend.entity.Post post = postService.getPostDetail(id);
        int eeCount = post != null ? (post.getEeCount() != null ? post.getEeCount() : 0) : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("eeLiked", isNowEeLiked);
        result.put("eeCount", eeCount);
        return Result.success(result);
    }

    @PostMapping("/{id}/share")
    public Result<Map<String, Object>> sharePost(@PathVariable Long id) {
        int shareCount = postService.incrementShareCount(id);
        Map<String, Object> result = new HashMap<>();
        result.put("shareCount", shareCount);
        return Result.success(result);
    }

    @PostMapping("/{id}/comments")
    public Result<CommentVO> createComment(@PathVariable Long id, @RequestBody CommentCreateDTO dto) {
        Long userId = requireLogin();
        String content = dto.getContent();
        if (content == null || content.trim().isEmpty()) {
            return Result.error(400, "评论内容不能为空");
        }

        Long parentId = dto.getParentId();
        Long replyToId = dto.getReplyToId();
        return Result.success(commentService.createComment(id, userId, content.trim(), parentId, replyToId));
    }

    @GetMapping("/{id}/comments")
    public Result<List<CommentVO>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(commentService.getComments(id, page, size));
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public Result<String> deleteComment(@PathVariable Long id, @PathVariable Long commentId) {
        Long userId = requireLogin();
        commentService.deleteComment(commentId, userId);
        return Result.success("删除成功");
    }
}

package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.converter.PostConverter;
import com.pettrail.pettrailbackend.dto.PostVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.dto.TagVO;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.Tag;
import com.pettrail.pettrailbackend.service.PostService;
import com.pettrail.pettrailbackend.service.TagService;
import com.pettrail.pettrailbackend.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "话题标签", description = "话题标签相关接口")
public class TagController extends BaseController {

    private final TagService tagService;
    private final PostService postService;
    private final PostConverter postConverter;

    @GetMapping("/hot")
    @Operation(summary = "获取热门标签")
    public Result<List<TagVO>> getHotTags(@RequestParam(defaultValue = "20") int limit) {
        List<Tag> tags = tagService.getHotTags(limit);
        return Result.success(tags.stream().map(this::convertToVO).collect(Collectors.toList()));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索标签")
    public Result<List<TagVO>> searchTags(@RequestParam String keyword, @RequestParam(defaultValue = "20") int limit) {
        List<Tag> tags = tagService.searchTags(keyword, limit);
        return Result.success(tags.stream().map(this::convertToVO).collect(Collectors.toList()));
    }

    @GetMapping("/{id}/posts")
    @Operation(summary = "获取标签下的动态")
    public Result<List<PostVO>> getTagPosts(@PathVariable Long id,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        List<Long> postIds = tagService.getPostIdsByTagId(id, page, size);
        if (postIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<Post> posts = postService.getPostsByIds(postIds);
        Long currentUserId = UserContext.getCurrentUserId();
        return Result.success(postConverter.convertToPostVOList(posts, currentUserId));
    }

    @GetMapping("/name/{name}/posts")
    @Operation(summary = "根据标签名称获取动态")
    public Result<List<PostVO>> getTagPostsByName(@PathVariable String name,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        Tag tag = tagService.getTagByName(name);
        if (tag == null) {
            return Result.success(Collections.emptyList());
        }
        List<Long> postIds = tagService.getPostIdsByTagId(tag.getId(), page, size);
        if (postIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<Post> posts = postService.getPostsByIds(postIds);
        Long currentUserId = UserContext.getCurrentUserId();
        return Result.success(postConverter.convertToPostVOList(posts, currentUserId));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "根据名称获取标签信息")
    public Result<TagVO> getTagByName(@PathVariable String name) {
        Tag tag = tagService.getTagByName(name);
        if (tag == null) {
            return Result.success(null);
        }
        return Result.success(convertToVO(tag));
    }

    private TagVO convertToVO(Tag tag) {
        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setUsageCount(tag.getUsageCount());
        vo.setIsHot(tag.getIsHot());
        vo.setIsOfficial(tag.getIsOfficial());
        return vo;
    }
}

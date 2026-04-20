package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.dto.TagVO;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.Tag;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import com.pettrail.pettrailbackend.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "话题标签", description = "话题标签相关接口")
public class TagController extends BaseController {

    private final TagService tagService;
    private final PostMapper postMapper;

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
    public Result<List<Object>> getTagPosts(@PathVariable Long id,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        List<Long> postIds = tagService.getPostIdsByTagId(id, page, size);
        if (postIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        List<Post> posts = postMapper.selectBatchIds(postIds);
        return Result.success(new ArrayList<>(posts));
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

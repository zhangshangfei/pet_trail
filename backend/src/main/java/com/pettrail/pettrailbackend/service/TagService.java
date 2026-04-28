package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.PostTag;
import com.pettrail.pettrailbackend.entity.Tag;
import com.pettrail.pettrailbackend.mapper.PostTagMapper;
import com.pettrail.pettrailbackend.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;
    private final PostTagMapper postTagMapper;

    public List<Tag> getHotTags(int limit) {
        return tagMapper.selectHotTags(limit);
    }

    public List<Tag> searchTags(String keyword, int limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getHotTags(limit);
        }
        return tagMapper.searchTags(keyword.trim(), limit);
    }

    public List<Tag> getTagsByPostId(Long postId) {
        List<Long> tagIds = postTagMapper.selectTagIdsByPostId(postId);
        if (tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        return tagMapper.selectBatchIds(tagIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Tag> bindTagsToPost(Long postId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> oldTagIds = postTagMapper.selectTagIdsByPostId(postId);
        for (Long oldTagId : oldTagIds) {
            tagMapper.decrementUsageCount(oldTagId);
        }
        LambdaQueryWrapper<PostTag> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(PostTag::getPostId, postId);
        postTagMapper.delete(deleteWrapper);

        List<Tag> result = new ArrayList<>();
        for (String name : tagNames) {
            String trimmed = name.trim().replaceAll("^#+", "");
            if (trimmed.isEmpty()) continue;

            Tag tag = tagMapper.selectByName(trimmed);
            if (tag == null) {
                tag = new Tag();
                tag.setName(trimmed);
                tag.setUsageCount(0);
                tag.setIsHot(false);
                tag.setIsOfficial(false);
                tag.setStatus(1);
                tagMapper.insert(tag);
            }

            PostTag postTag = new PostTag();
            postTag.setPostId(postId);
            postTag.setTagId(tag.getId());
            postTagMapper.insert(postTag);

            tagMapper.incrementUsageCount(tag.getId());
            result.add(tag);
        }

        return result;
    }

    public List<Long> getPostIdsByTagId(Long tagId, int page, int size) {
        int offset = (page - 1) * size;
        return postTagMapper.selectPostIdsByTagId(tagId, offset, size);
    }

    public Tag getOrCreateTag(String name) {
        String trimmed = name.trim().replaceAll("^#+", "");
        Tag tag = tagMapper.selectByName(trimmed);
        if (tag == null) {
            tag = new Tag();
            tag.setName(trimmed);
            tag.setUsageCount(0);
            tag.setIsHot(false);
            tag.setIsOfficial(false);
            tag.setStatus(1);
            tagMapper.insert(tag);
        }
        return tag;
    }

    public Tag getTagByName(String name) {
        String trimmed = name.trim().replaceAll("^#+", "");
        return tagMapper.selectByName(trimmed);
    }
}

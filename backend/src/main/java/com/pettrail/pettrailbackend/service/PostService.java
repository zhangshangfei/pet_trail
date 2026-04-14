package com.pettrail.pettrailbackend.service;

import com.alibaba.fastjson.JSON;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.PostLike;
import com.pettrail.pettrailbackend.event.PostCreateEvent;
import com.pettrail.pettrailbackend.exception.NotFoundException;
import com.pettrail.pettrailbackend.mapper.PostLikeMapper;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 动态服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建动态
     */
    @Transactional(rollbackFor = Exception.class)
    public Post createPost(Long userId, Long petId, String content, List<String> images) {
        Post post = new Post();
        post.setUserId(userId);
        post.setPetId(petId);
        post.setContent(content);
        post.setImages(JSON.toJSONString(images));
        post.setStatus(1); // 1-正常
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);
        postMapper.insert(post);
        return post;
    }

    /**
     * 获取动态详情
     */
    public Post getPostDetail(Long postId) {
        // 从缓存读取
        String cacheKey = "post:detail:" + postId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (Post) cached;
        }

        // 从数据库查询
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new NotFoundException("动态不存在");
        }

        // 写入缓存（30 分钟过期）
        redisTemplate.opsForValue().set(cacheKey, post, 30, TimeUnit.MINUTES);
        return post;
    }

    /**
     * 获取动态列表
     * @param page 页码
     * @param size 每页数量
     * @param tab tab类型：all-全部, follow-关注, recommend-推荐
     * @param userId 当前用户ID
     */
    public List<Post> getFeed(int page, int size, String tab, Long userId) {
        int offset = (page - 1) * size;
        
        // 根据 tab 类型返回不同的数据
        if ("follow".equals(tab) && userId != null) {
            // 关注：返回关注用户的动态（暂时返回全部，后续实现关注功能后修改）
            return postMapper.selectFeed(offset, size);
        } else if ("recommend".equals(tab)) {
            // 推荐：返回推荐动态（暂时返回全部，后续实现推荐算法后修改）
            return postMapper.selectFeed(offset, size);
        } else {
            // 全部：返回所有动态
            return postMapper.selectFeed(offset, size);
        }
    }

    /**
     * 点赞/取消点赞
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long postId, Long userId) {
        String likeKey = "post:like:" + postId;
        String userLikeKey = "post:user:like:" + postId + ":" + userId;

        // 检查是否已点赞
        Boolean isLiked = redisTemplate.hasKey(userLikeKey);

        if (Boolean.TRUE.equals(isLiked)) {
            // 取消点赞
            redisTemplate.delete(userLikeKey);
            redisTemplate.opsForHash().increment(likeKey, "count", -1);
            postLikeMapper.deleteByPostIdAndUserId(postId, userId);
            // 更新点赞计数
            Post post = postMapper.selectById(postId);
            if (post != null) {
                post.setLikeCount(post.getLikeCount() - 1);
                postMapper.updateById(post);
            }
            return false;
        } else {
            // 点赞
            redisTemplate.opsForValue().set(userLikeKey, "1", 7, TimeUnit.DAYS);
            redisTemplate.opsForHash().increment(likeKey, "count", 1);

            // 写入数据库
            PostLike postLike = new PostLike();
            postLike.setPostId(postId);
            postLike.setUserId(userId);
            postLikeMapper.insert(postLike);

            // 更新点赞计数
            Post post = postMapper.selectById(postId);
            if (post != null) {
                post.setLikeCount(post.getLikeCount() + 1);
                postMapper.updateById(post);
            }

            return true;
        }
    }

    /**
     * 检查用户是否已点赞
     */
    public boolean isUserLiked(Long postId, Long userId) {
        String userLikeKey = "post:user:like:" + postId + ":" + userId;
        Boolean exists = redisTemplate.hasKey(userLikeKey);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 收藏/取消收藏
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleEe(Long postId, Long userId) {
        String eeKey = "post:ee:" + postId;
        String userEeKey = "post:user:ee:" + postId + ":" + userId;

        // 检查是否已收藏
        Boolean isEeLiked = redisTemplate.hasKey(userEeKey);

        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new NotFoundException("动态不存在");
        }

        if (Boolean.TRUE.equals(isEeLiked)) {
            // 取消收藏
            redisTemplate.delete(userEeKey);
            redisTemplate.opsForHash().increment(eeKey, "count", -1);
            
            // 更新收藏计数
            int newCount = Math.max(0, (post.getEeCount() != null ? post.getEeCount() : 0) - 1);
            post.setEeCount(newCount);
            postMapper.updateById(post);
            
            return false;
        } else {
            // 收藏
            redisTemplate.opsForValue().set(userEeKey, "1", 7, TimeUnit.DAYS);
            redisTemplate.opsForHash().increment(eeKey, "count", 1);

            // 更新收藏计数
            int newCount = (post.getEeCount() != null ? post.getEeCount() : 0) + 1;
            post.setEeCount(newCount);
            postMapper.updateById(post);
            
            return true;
        }
    }

    /**
     * 检查用户是否已收藏
     */
    public boolean isUserEeLiked(Long postId, Long userId) {
        String userEeKey = "post:user:ee:" + postId + ":" + userId;
        Boolean exists = redisTemplate.hasKey(userEeKey);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 发布动态创建事件（异步处理）
     */
    public void publishPostCreateEvent(Post post) {
        // 发布 Spring Event 事件
        eventPublisher.publishEvent(new PostCreateEvent(this, post));
        log.info("发布动态创建事件：postId={}", post.getId());
    }
}

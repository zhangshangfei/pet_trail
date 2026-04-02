package com.pettrail.pettrailbackend.service;

import com.alibaba.fastjson.JSON;
import com.pettrail.pettrailbackend.config.RabbitMQConfig;
import com.pettrail.pettrailbackend.dto.PostCreateMessage;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.PostLike;
import com.pettrail.pettrailbackend.exception.NotFoundException;
import com.pettrail.pettrailbackend.mapper.PostLikeMapper;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private final RabbitTemplate rabbitTemplate;

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
     */
    public List<Post> getFeed(int page, int size) {
        int offset = (page - 1) * size;
        return postMapper.selectFeed(offset, size);
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
     * 发送动态创建消息（异步处理）
     */
    public void sendPostCreateMessage(Post post) {
        PostCreateMessage message = PostCreateMessage.builder()
            .postId(post.getId())
            .userId(post.getUserId())
            .petId(post.getPetId())
            .content(post.getContent())
            .images(post.getImages() != null ? JSON.parseArray(post.getImages(), String.class) : null)
            .createTime(System.currentTimeMillis())
            .build();

        // 发送异步消息到 RabbitMQ
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.POST_CREATE_EXCHANGE,
            RabbitMQConfig.POST_CREATE_ROUTING_KEY,
            message
        );
        log.info("发送动态创建消息：postId={}", post.getId());
    }
}

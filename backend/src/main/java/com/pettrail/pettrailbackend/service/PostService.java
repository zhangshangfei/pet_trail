package com.pettrail.pettrailbackend.service;

import com.alibaba.fastjson.JSON;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.PostEe;
import com.pettrail.pettrailbackend.entity.PostLike;
import com.pettrail.pettrailbackend.event.PostCreateEvent;
import com.pettrail.pettrailbackend.exception.NotFoundException;
import com.pettrail.pettrailbackend.mapper.PostEeMapper;
import com.pettrail.pettrailbackend.mapper.PostLikeMapper;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
    private final PostEeMapper postEeMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final ContentAuditService contentAuditService;

    @Transactional(rollbackFor = Exception.class)
    public Post createPost(Long userId, Long petId, String content, List<String> images, List<String> videos,
                           List<String> stickers, Map<String, String> bubble, String location) {
        if (!contentAuditService.auditText(content)) {
            throw new RuntimeException("内容包含违规信息，请修改后重新发布");
        }

        if (images != null) {
            for (String imageUrl : images) {
                if (!contentAuditService.auditImage(imageUrl)) {
                    throw new RuntimeException("图片包含违规内容，请更换后重新发布");
                }
            }
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setPetId(petId);
        post.setContent(content);
        post.setImages(images != null ? JSON.toJSONString(images) : null);
        post.setVideos(videos != null ? JSON.toJSONString(videos) : null);
        post.setStickers(stickers != null && !stickers.isEmpty() ? JSON.toJSONString(stickers) : null);
        post.setBubble(bubble != null ? JSON.toJSONString(bubble) : null);
        post.setLocation(location);
        post.setStatus(1);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);
        post.setEeCount(0);
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
     * @param tab tab类型：all-全部, follow-关注, recommend-推荐, collect-收藏
     * @param userId 当前用户ID
     */
    public List<Post> getFeed(int page, int size, String tab, Long userId) {
        int offset = (page - 1) * size;
        log.info("getFeed - page: {}, size: {}, tab: {}, userId: {}, offset: {}", page, size, tab, userId, offset);

        // 根据 tab 类型返回不同的数据
        if ("follow".equals(tab) && userId != null) {
            // 关注：返回关注用户的动态
            log.info("查询关注Tab动态");
            return postMapper.selectFollowFeed(userId, offset, size);
        } else if ("collect".equals(tab) && userId != null) {
            // 收藏：返回用户收藏的动态
            log.info("查询收藏Tab动态");
            return postMapper.selectCollectFeed(userId, offset, size);
        } else if ("recommend".equals(tab)) {
            log.info("查询推荐Tab动态");
            return postMapper.selectRecommendFeed(offset, size);
        } else {
            // 全部：返回所有动态
            log.info("查询全部Tab动态");
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

        // 先检查动态是否存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new NotFoundException("动态不存在");
        }

        // 检查是否已点赞
        Boolean isLiked = redisTemplate.hasKey(userLikeKey);

        if (isLiked) {
            // 取消点赞
            redisTemplate.delete(userLikeKey);
            redisTemplate.opsForHash().increment(likeKey, "count", -1);
            redisTemplate.expire(likeKey, 7, TimeUnit.DAYS); // 设置 TTL
            postLikeMapper.deleteByPostIdAndUserId(postId, userId);
            // 更新点赞计数
            post.setLikeCount(post.getLikeCount() - 1);
            postMapper.updateById(post);
        } else {
            // 点赞
            redisTemplate.opsForValue().set(userLikeKey, "1", 7, TimeUnit.DAYS);
            redisTemplate.opsForHash().increment(likeKey, "count", 1);
            redisTemplate.expire(likeKey, 7, TimeUnit.DAYS); // 设置 TTL

            // 写入数据库
            PostLike postLike = new PostLike();
            postLike.setPostId(postId);
            postLike.setUserId(userId);
            try {
                postLikeMapper.insert(postLike);
            } catch (DuplicateKeyException e) {
                log.warn("重复点赞: postId={}, userId={}", postId, userId);
                return true;
            }

            // 更新点赞计数
            post.setLikeCount(post.getLikeCount() + 1);
            postMapper.updateById(post);

            notificationService.createNotification(
                post.getUserId(), userId, "like", postId, "赞了你的动态");
        }

        // 清除详情缓存
        String cacheKey = "post:detail:" + postId;
        redisTemplate.delete(cacheKey);

        return !isLiked;
    }

    /**
     * 检查用户是否已点赞
     */
    public boolean isUserLiked(Long postId, Long userId) {
        String userLikeKey = "post:user:like:" + postId + ":" + userId;
        Boolean exists = redisTemplate.hasKey(userLikeKey);
        if (exists) {
            return true;
        }
        // Redis 未命中时，查数据库
        return postLikeMapper.selectByPostIdAndUserId(postId, userId) != null;
    }

    /**
     * 获取点赞数（从缓存获取）
     */
    public Long getLikeCountFromCache(Long postId) {
        String likeKey = "post:like:" + postId;
        Object count = redisTemplate.opsForHash().get(likeKey, "count");
        if (count != null) {
            return Long.valueOf(count.toString());
        }
        // 缓存未命中时从数据库查询
        Post post = postMapper.selectById(postId);
        return post != null ? post.getLikeCount() : 0L;
    }

    /**
     * 收藏/取消收藏
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleEe(Long postId, Long userId) {
        String eeKey = "post:ee:" + postId;
        String userEeKey = "post:user:ee:" + postId + ":" + userId;

        // 先检查动态是否存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new NotFoundException("动态不存在");
        }

        // 检查是否已收藏
        Boolean isEeLiked = redisTemplate.hasKey(userEeKey);

        if (isEeLiked) {
            // 取消收藏
            redisTemplate.delete(userEeKey);
            redisTemplate.opsForHash().increment(eeKey, "count", -1);
            redisTemplate.expire(eeKey, 7, TimeUnit.DAYS); // 设置 TTL

            // 删除数据库记录
            postEeMapper.deleteByPostIdAndUserId(postId, userId);

            // 更新收藏计数
            int newCount = Math.max(0, (post.getEeCount() != null ? post.getEeCount() : 0) - 1);
            post.setEeCount(newCount);
            postMapper.updateById(post);
        } else {
            // 收藏
            redisTemplate.opsForValue().set(userEeKey, "1", 7, TimeUnit.DAYS);
            redisTemplate.opsForHash().increment(eeKey, "count", 1);
            redisTemplate.expire(eeKey, 7, TimeUnit.DAYS); // 设置 TTL

            // 写入数据库
            PostEe postEe = new PostEe();
            postEe.setPostId(postId);
            postEe.setUserId(userId);
            try {
                postEeMapper.insert(postEe);
            } catch (DuplicateKeyException e) {
                log.warn("重复收藏: postId={}, userId={}", postId, userId);
                return true;
            }

            // 更新收藏计数
            int newCount = (post.getEeCount() != null ? post.getEeCount() : 0) + 1;
            post.setEeCount(newCount);
            postMapper.updateById(post);

            notificationService.createNotification(
                post.getUserId(), userId, "favorite", postId, "收藏了你的动态");
        }

        // 清除详情缓存
        String cacheKey = "post:detail:" + postId;
        redisTemplate.delete(cacheKey);

        return !isEeLiked;
    }

    /**
     * 检查用户是否已收藏
     */
    public boolean isUserEeLiked(Long postId, Long userId) {
        String userEeKey = "post:user:ee:" + postId + ":" + userId;
        Boolean exists = redisTemplate.hasKey(userEeKey);
        if (exists) {
            return true;
        }
        // Redis 未命中时，查数据库
        return postEeMapper.selectByPostIdAndUserId(postId, userId) != null;
    }

    /**
     * 发布动态创建事件（异步处理）
     */
    public void publishPostCreateEvent(Post post) {
        // 发布 Spring Event 事件
        eventPublisher.publishEvent(new PostCreateEvent(this, post));
        log.info("发布动态创建事件：postId={}", post.getId());
    }

    public int getUserPostCount(Long userId) {
        return postMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .eq(Post::getStatus, 1)
        ).intValue();
    }

    @Transactional(rollbackFor = Exception.class)
    public int incrementShareCount(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new NotFoundException("动态不存在");
        }
        int newCount = (post.getShareCount() != null ? post.getShareCount() : 0) + 1;
        post.setShareCount(newCount);
        postMapper.updateById(post);

        String cacheKey = "post:detail:" + postId;
        redisTemplate.delete(cacheKey);

        return newCount;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new NotFoundException("动态不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人动态");
        }
        post.setStatus(0);
        postMapper.updateById(post);

        String cacheKey = "post:detail:" + postId;
        redisTemplate.delete(cacheKey);

        String likeKey = "post:like:" + postId;
        String eeKey = "post:ee:" + postId;
        redisTemplate.delete(likeKey);
        redisTemplate.delete(eeKey);

        log.info("动态已删除：postId={}, userId={}", postId, userId);
    }
}

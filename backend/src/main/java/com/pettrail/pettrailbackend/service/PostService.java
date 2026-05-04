package com.pettrail.pettrailbackend.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.PostEe;
import com.pettrail.pettrailbackend.entity.PostLike;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.event.PostCreateEvent;
import com.pettrail.pettrailbackend.exception.NotFoundException;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.exception.ForbiddenException;
import com.pettrail.pettrailbackend.mapper.PostEeMapper;
import com.pettrail.pettrailbackend.mapper.PostLikeMapper;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final ContentAuditService contentAuditService;
    private final AchievementService achievementService;

    @Transactional(rollbackFor = Exception.class)
    public Post createPost(Long userId, Long petId, String content, List<String> images, List<String> videos,
                           List<String> stickers, Map<String, String> bubble, String location) {
        if (!contentAuditService.auditText(content)) {
            throw new BusinessException("内容包含违规信息，请修改后重新发布");
        }

        if (images != null) {
            for (String imageUrl : images) {
                if (!contentAuditService.auditImage(imageUrl)) {
                    throw new BusinessException("图片包含违规内容，请更换后重新发布");
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
        post.setAuditStatus(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);
        post.setEeCount(0);
        postMapper.insert(post);

        try {
            achievementService.checkAndUnlock(userId, "post_count");
        } catch (Exception e) {
            log.warn("成就检查失败: userId={}, error={}", userId, e.getMessage());
        }

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
            redisTemplate.delete(userLikeKey);
            redisTemplate.opsForHash().increment(likeKey, "count", -1);
            redisTemplate.expire(likeKey, 7, TimeUnit.DAYS);
            postLikeMapper.deleteByPostIdAndUserId(postId, userId);
            postMapper.updateLikeCountAtomic(postId, -1);
        } else {
            redisTemplate.opsForValue().set(userLikeKey, "1", 7, TimeUnit.DAYS);
            redisTemplate.opsForHash().increment(likeKey, "count", 1);
            redisTemplate.expire(likeKey, 7, TimeUnit.DAYS);

            PostLike postLike = new PostLike();
            postLike.setPostId(postId);
            postLike.setUserId(userId);
            try {
                postLikeMapper.insert(postLike);
            } catch (DuplicateKeyException e) {
                log.warn("重复点赞: postId={}, userId={}", postId, userId);
                return true;
            }

            postMapper.updateLikeCountAtomic(postId, 1);

            notificationService.createNotification(
                post.getUserId(), userId, "like", postId, "赞了你的动态");

            try {
                achievementService.checkAndUnlock(post.getUserId(), "like_received");
            } catch (Exception e) {
                log.warn("成就检查失败: userId={}, error={}", post.getUserId(), e.getMessage());
            }
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
            redisTemplate.delete(userEeKey);
            redisTemplate.opsForHash().increment(eeKey, "count", -1);
            redisTemplate.expire(eeKey, 7, TimeUnit.DAYS);

            postEeMapper.deleteByPostIdAndUserId(postId, userId);
            postMapper.updateEeCountAtomic(postId, -1);
        } else {
            redisTemplate.opsForValue().set(userEeKey, "1", 7, TimeUnit.DAYS);
            redisTemplate.opsForHash().increment(eeKey, "count", 1);
            redisTemplate.expire(eeKey, 7, TimeUnit.DAYS);

            PostEe postEe = new PostEe();
            postEe.setPostId(postId);
            postEe.setUserId(userId);
            try {
                postEeMapper.insert(postEe);
            } catch (DuplicateKeyException e) {
                log.warn("重复收藏: postId={}, userId={}", postId, userId);
                return true;
            }

            postMapper.updateEeCountAtomic(postId, 1);

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
                .eq(Post::getDeleted, 0)
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

    public void updatePost(Post post) {
        postMapper.updateById(post);
        String cacheKey = "post:detail:" + post.getId();
        redisTemplate.delete(cacheKey);
    }

    public List<Post> getUserPosts(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return postMapper.selectByUserId(userId, offset, size);
    }

    public List<Post> getUserLikedPosts(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return postMapper.selectLikedFeed(userId, offset, size);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new NotFoundException("动态不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new ForbiddenException("无权删除他人动态");
        }
        post.setDeleted(1);
        postMapper.updateById(post);

        String cacheKey = "post:detail:" + postId;
        redisTemplate.delete(cacheKey);

        String likeKey = "post:like:" + postId;
        String eeKey = "post:ee:" + postId;
        redisTemplate.delete(likeKey);
        redisTemplate.delete(eeKey);

        log.info("动态已删除：postId={}, userId={}", postId, userId);
    }

    public Page<Post> adminListPosts(int page, int size, Long userId, Integer auditStatus) {
        Page<Post> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getDeleted, 0);
        if (userId != null) wrapper.eq(Post::getUserId, userId);
        if (auditStatus != null) wrapper.eq(Post::getAuditStatus, auditStatus);
        wrapper.orderByDesc(Post::getCreatedAt);
        Page<Post> result = postMapper.selectPage(pageParam, wrapper);
        fillPostUserNickname(result);
        return result;
    }

    public Post adminGetPostDetail(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) throw new BusinessException(404, "动态不存在");
        if (post.getUserId() != null) {
            User user = userMapper.selectById(post.getUserId());
            if (user != null) post.setUserNickname(user.getNickname());
        }
        return post;
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminAuditPost(Long id, Integer auditStatus, String auditRemark) {
        Post post = postMapper.selectById(id);
        if (post == null) throw new BusinessException(404, "动态不存在");
        post.setAuditStatus(auditStatus);
        if (auditRemark != null) post.setAuditRemark(auditRemark);
        postMapper.updateById(post);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminDeletePost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) throw new BusinessException(404, "动态不存在");
        post.setDeleted(1);
        postMapper.updateById(post);

        String cacheKey = "post:detail:" + id;
        redisTemplate.delete(cacheKey);
        String likeKey = "post:like:" + id;
        String eeKey = "post:ee:" + id;
        redisTemplate.delete(likeKey);
        redisTemplate.delete(eeKey);

        log.info("管理端动态已删除：postId={}", id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminBatchAudit(List<Integer> postIds, Integer auditStatus, String auditRemark) {
        if (postIds == null || postIds.isEmpty() || auditStatus == null) {
            throw new BusinessException(400, "参数不完整");
        }
        for (Integer postId : postIds) {
            Post post = postMapper.selectById(postId.longValue());
            if (post != null) {
                post.setAuditStatus(auditStatus);
                if (auditRemark != null) post.setAuditRemark(auditRemark);
                postMapper.updateById(post);
            }
        }
    }

    public Page<Post> adminListDeletedPosts(int page, int size) {
        Page<Post> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getDeleted, 1);
        wrapper.orderByDesc(Post::getCreatedAt);
        Page<Post> result = postMapper.selectPage(pageParam, wrapper);
        fillPostUserNickname(result);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminRestorePost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) throw new BusinessException(404, "动态不存在");
        post.setDeleted(0);
        if (post.getStatus() == null || post.getStatus() == 0) {
            post.setStatus(1);
        }
        postMapper.updateById(post);

        String cacheKey = "post:detail:" + id;
        redisTemplate.delete(cacheKey);

        log.info("管理端动态已恢复：postId={}", id);
    }

    private void fillPostUserNickname(Page<Post> result) {
        Set<Long> userIds = result.getRecords().stream()
                .map(Post::getUserId)
                .filter(uid -> uid != null)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        Map<Long, String> nicknameMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId,
                        u -> u.getNickname() != null ? u.getNickname() : "用户" + u.getId(),
                        (a, b) -> a));
        for (Post post : result.getRecords()) {
            if (post.getUserId() != null) {
                post.setUserNickname(nicknameMap.getOrDefault(post.getUserId(), "未知用户"));
            }
        }
    }

    public List<Post> getPostsByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }
        return postMapper.selectBatchIds(postIds);
    }
}

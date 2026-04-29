package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.PostEe;
import com.pettrail.pettrailbackend.entity.PostLike;
import com.pettrail.pettrailbackend.exception.NotFoundException;
import com.pettrail.pettrailbackend.mapper.PostEeMapper;
import com.pettrail.pettrailbackend.mapper.PostLikeMapper;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostLikeMapper postLikeMapper;

    @Mock
    private PostEeMapper postEeMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ContentAuditService contentAuditService;

    @InjectMocks
    private PostService postService;

    private Post testPost;

    @BeforeEach
    void setUp() {
        testPost = new Post();
        testPost.setId(1L);
        testPost.setUserId(100L);
        testPost.setContent("测试动态");
        testPost.setLikeCount(5);
        testPost.setCommentCount(2);
        testPost.setShareCount(1);
        testPost.setEeCount(3);
        testPost.setStatus(1);

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void createPost_shouldAuditTextAndImages() {
        when(contentAuditService.auditText("测试内容")).thenReturn(true);
        when(contentAuditService.auditImage("http://img.jpg")).thenReturn(true);
        when(postMapper.insert(any(Post.class))).thenReturn(1);

        Post result = postService.createPost(100L, 10L, "测试内容",
                Arrays.asList("http://img.jpg"), null, null, null, "北京");

        assertNotNull(result);
        verify(contentAuditService).auditText("测试内容");
        verify(contentAuditService).auditImage("http://img.jpg");
        verify(postMapper).insert(any(Post.class));
    }

    @Test
    void createPost_shouldRejectViolentText() {
        when(contentAuditService.auditText("违规内容")).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                postService.createPost(100L, null, "违规内容", null, null, null, null, null));
        verify(postMapper, never()).insert(any(Post.class));
    }

    @Test
    void createPost_shouldRejectViolentImage() {
        when(contentAuditService.auditText("正常内容")).thenReturn(true);
        when(contentAuditService.auditImage("http://bad.jpg")).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                postService.createPost(100L, null, "正常内容",
                        Arrays.asList("http://bad.jpg"), null, null, null, null));
    }

    @Test
    void getPostDetail_shouldReturnFromCache() {
        when(valueOperations.get("post:detail:1")).thenReturn(testPost);

        Post result = postService.getPostDetail(1L);

        assertEquals(1L, result.getId());
        verify(postMapper, never()).selectById(anyLong());
    }

    @Test
    void getPostDetail_shouldQueryDbWhenCacheMiss() {
        when(valueOperations.get("post:detail:1")).thenReturn(null);
        when(postMapper.selectById(1L)).thenReturn(testPost);

        Post result = postService.getPostDetail(1L);

        assertEquals(1L, result.getId());
        verify(valueOperations).set(eq("post:detail:1"), eq(testPost), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    void getPostDetail_shouldThrowWhenNotFound() {
        when(valueOperations.get("post:detail:999")).thenReturn(null);
        when(postMapper.selectById(999L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> postService.getPostDetail(999L));
    }

    @Test
    void toggleLike_shouldLikeWhenNotLiked() {
        when(postMapper.selectById(1L)).thenReturn(testPost);
        when(redisTemplate.hasKey("post:user:like:1:200")).thenReturn(false);
        when(postLikeMapper.insert(any(PostLike.class))).thenReturn(1);
        when(postMapper.updateById(any(Post.class))).thenReturn(1);

        boolean result = postService.toggleLike(1L, 200L);

        assertTrue(result);
        verify(valueOperations).set(eq("post:user:like:1:200"), eq("1"), eq(7L), eq(TimeUnit.DAYS));
        verify(hashOperations).increment("post:like:1", "count", 1);
        verify(notificationService).createNotification(100L, 200L, "like", 1L, "赞了你的动态");
    }

    @Test
    void toggleLike_shouldUnlikeWhenAlreadyLiked() {
        when(postMapper.selectById(1L)).thenReturn(testPost);
        when(redisTemplate.hasKey("post:user:like:1:200")).thenReturn(true);
        when(postLikeMapper.deleteByPostIdAndUserId(1L, 200L)).thenReturn(1);
        when(postMapper.updateById(any(Post.class))).thenReturn(1);

        boolean result = postService.toggleLike(1L, 200L);

        assertFalse(result);
        verify(redisTemplate).delete("post:user:like:1:200");
        verify(hashOperations).increment("post:like:1", "count", -1);
        verify(notificationService, never()).createNotification(anyLong(), anyLong(), anyString(), anyLong(), anyString());
    }

    @Test
    void toggleLike_shouldHandleDuplicateKey() {
        when(postMapper.selectById(1L)).thenReturn(testPost);
        when(redisTemplate.hasKey("post:user:like:1:200")).thenReturn(false);
        when(postLikeMapper.insert(any(PostLike.class))).thenThrow(new DuplicateKeyException("duplicate"));

        boolean result = postService.toggleLike(1L, 200L);

        assertTrue(result);
    }

    @Test
    void toggleLike_shouldThrowWhenPostNotFound() {
        when(postMapper.selectById(999L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> postService.toggleLike(999L, 200L));
    }

    @Test
    void toggleEe_shouldCollectWhenNotCollected() {
        when(postMapper.selectById(1L)).thenReturn(testPost);
        when(redisTemplate.hasKey("post:user:ee:1:200")).thenReturn(false);
        when(postEeMapper.insert(any(PostEe.class))).thenReturn(1);
        when(postMapper.updateById(any(Post.class))).thenReturn(1);

        boolean result = postService.toggleEe(1L, 200L);

        assertTrue(result);
        verify(valueOperations).set(eq("post:user:ee:1:200"), eq("1"), eq(7L), eq(TimeUnit.DAYS));
        verify(notificationService).createNotification(100L, 200L, "favorite", 1L, "收藏了你的动态");
    }

    @Test
    void toggleEe_shouldUncollectWhenAlreadyCollected() {
        when(postMapper.selectById(1L)).thenReturn(testPost);
        when(redisTemplate.hasKey("post:user:ee:1:200")).thenReturn(true);
        when(postEeMapper.deleteByPostIdAndUserId(1L, 200L)).thenReturn(1);
        when(postMapper.updateById(any(Post.class))).thenReturn(1);

        boolean result = postService.toggleEe(1L, 200L);

        assertFalse(result);
        verify(redisTemplate).delete("post:user:ee:1:200");
    }

    @Test
    void isUserLiked_shouldCheckRedisFirst() {
        when(redisTemplate.hasKey("post:user:like:1:200")).thenReturn(true);

        assertTrue(postService.isUserLiked(1L, 200L));
        verify(postLikeMapper, never()).selectByPostIdAndUserId(anyLong(), anyLong());
    }

    @Test
    void isUserLiked_shouldFallbackToDbWhenRedisMiss() {
        when(redisTemplate.hasKey("post:user:like:1:200")).thenReturn(false);
        PostLike like = new PostLike();
        when(postLikeMapper.selectByPostIdAndUserId(1L, 200L)).thenReturn(like);

        assertTrue(postService.isUserLiked(1L, 200L));
    }

    @Test
    void getLikeCountFromCache_shouldReturnFromRedis() {
        when(hashOperations.get("post:like:1", "count")).thenReturn(5);

        Long count = postService.getLikeCountFromCache(1L);
        assertEquals(5L, count);
    }

    @Test
    void getLikeCountFromCache_shouldFallbackToDb() {
        when(hashOperations.get("post:like:1", "count")).thenReturn(null);
        when(postMapper.selectById(1L)).thenReturn(testPost);

        Long count = postService.getLikeCountFromCache(1L);
        assertEquals(5L, count);
    }

    @Test
    void deletePost_shouldSoftDelete() {
        when(postMapper.selectById(1L)).thenReturn(testPost);
        when(postMapper.updateById(any(Post.class))).thenReturn(1);

        postService.deletePost(1L, 100L);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).updateById(captor.capture());
        assertEquals(0, captor.getValue().getStatus());
        verify(redisTemplate).delete("post:detail:1");
        verify(redisTemplate).delete("post:like:1");
        verify(redisTemplate).delete("post:ee:1");
    }

    @Test
    void deletePost_shouldRejectOtherUser() {
        when(postMapper.selectById(1L)).thenReturn(testPost);

        assertThrows(RuntimeException.class, () -> postService.deletePost(1L, 999L));
        verify(postMapper, never()).updateById(any(Post.class));
    }

    @Test
    void incrementShareCount_shouldIncrement() {
        when(postMapper.selectById(1L)).thenReturn(testPost);
        when(postMapper.updateById(any(Post.class))).thenReturn(1);

        int newCount = postService.incrementShareCount(1L);

        assertEquals(2, newCount);
        verify(redisTemplate).delete("post:detail:1");
    }

    @Test
    void getUserPostCount_shouldReturnCount() {
        when(postMapper.selectCount(any())).thenReturn(10L);

        int count = postService.getUserPostCount(100L);
        assertEquals(10, count);
    }

    @Test
    void publishPostCreateEvent_shouldPublish() {
        postService.publishPostCreateEvent(testPost);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void getFeed_shouldReturnAllFeed() {
        when(postMapper.selectFeed(0, 10)).thenReturn(Arrays.asList(testPost));

        List<Post> result = postService.getFeed(1, 10, "all", null);
        assertEquals(1, result.size());
    }

    @Test
    void getFeed_shouldReturnFollowFeed() {
        when(postMapper.selectFollowFeed(100L, 0, 10)).thenReturn(Arrays.asList(testPost));

        List<Post> result = postService.getFeed(1, 10, "follow", 100L);
        assertEquals(1, result.size());
    }

    @Test
    void getFeed_shouldReturnCollectFeed() {
        when(postMapper.selectCollectFeed(100L, 0, 10)).thenReturn(Arrays.asList(testPost));

        List<Post> result = postService.getFeed(1, 10, "collect", 100L);
        assertEquals(1, result.size());
    }
}

package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.entity.Follow;
import com.pettrail.pettrailbackend.mapper.FollowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowMapper followMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private FollowService followService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void toggleFollow_shouldFollowWhenNotFollowing() {
        when(followMapper.selectOne(any())).thenReturn(null);
        when(followMapper.insert(any(Follow.class))).thenReturn(1);

        boolean result = followService.toggleFollow(100L, 200L);

        assertTrue(result);
        verify(followMapper).insert(any(Follow.class));
        verify(notificationService).createNotification(200L, 100L, "follow", 100L, "关注了你");
        verify(redisTemplate, atLeastOnce()).delete(contains("follow:"));
    }

    @Test
    void toggleFollow_shouldUnfollowWhenAlreadyFollowing() {
        Follow existing = new Follow();
        existing.setFollowerId(100L);
        existing.setFolloweeId(200L);
        when(followMapper.selectOne(any())).thenReturn(existing);
        when(followMapper.deleteByFollowerIdAndFolloweeId(100L, 200L)).thenReturn(1);

        boolean result = followService.toggleFollow(100L, 200L);

        assertFalse(result);
        verify(followMapper).deleteByFollowerIdAndFolloweeId(100L, 200L);
        verify(notificationService, never()).createNotification(anyLong(), anyLong(), anyString(), anyLong(), anyString());
    }

    @Test
    void toggleFollow_shouldRejectSelfFollow() {
        assertThrows(IllegalArgumentException.class, () -> followService.toggleFollow(100L, 100L));
    }

    @Test
    void isFollowing_shouldReturnFromCache() {
        when(valueOperations.get("follow:is:100:200")).thenReturn("1");

        assertTrue(followService.isFollowing(100L, 200L));
        verify(followMapper, never()).selectOne(any());
    }

    @Test
    void isFollowing_shouldReturnFalseFromCache() {
        when(valueOperations.get("follow:is:100:200")).thenReturn("0");

        assertFalse(followService.isFollowing(100L, 200L));
    }

    @Test
    void isFollowing_shouldFallbackToDbWhenCacheMiss() {
        when(valueOperations.get("follow:is:100:200")).thenReturn(null);
        Follow follow = new Follow();
        when(followMapper.selectOne(any())).thenReturn(follow);

        assertTrue(followService.isFollowing(100L, 200L));
        verify(valueOperations).set(eq("follow:is:100:200"), eq("1"), eq(1L), eq(TimeUnit.HOURS));
    }

    @Test
    void isFollowing_shouldHandleRedisException() {
        when(valueOperations.get("follow:is:100:200")).thenThrow(new RuntimeException("Redis error"));
        when(followMapper.selectOne(any())).thenReturn(null);

        assertFalse(followService.isFollowing(100L, 200L));
    }

    @Test
    void getFollowerCount_shouldReturnFromCache() {
        when(valueOperations.get("follow:count:follower:200")).thenReturn("10");

        int count = followService.getFollowerCount(200L);
        assertEquals(10, count);
        verify(followMapper, never()).countFollowers(anyLong());
    }

    @Test
    void getFollowerCount_shouldFallbackToDb() {
        when(valueOperations.get("follow:count:follower:200")).thenReturn(null);
        when(followMapper.countFollowers(200L)).thenReturn(10);

        int count = followService.getFollowerCount(200L);
        assertEquals(10, count);
        verify(valueOperations).set(eq("follow:count:follower:200"), eq(10), eq(10L), eq(TimeUnit.MINUTES));
    }

    @Test
    void getFolloweeCount_shouldReturnFromCache() {
        when(valueOperations.get("follow:count:followee:100")).thenReturn("5");

        int count = followService.getFolloweeCount(100L);
        assertEquals(5, count);
    }

    @Test
    void getFolloweeCount_shouldFallbackToDb() {
        when(valueOperations.get("follow:count:followee:100")).thenReturn(null);
        when(followMapper.countFollowees(100L)).thenReturn(5);

        int count = followService.getFolloweeCount(100L);
        assertEquals(5, count);
    }

    @Test
    void getFolloweeIds_shouldReturnIds() {
        when(followMapper.selectFolloweeIds(100L)).thenReturn(Arrays.asList(200L, 300L));

        List<Long> ids = followService.getFolloweeIds(100L);
        assertEquals(2, ids.size());
        assertTrue(ids.contains(200L));
        assertTrue(ids.contains(300L));
    }

    @Test
    void getFollowerIds_shouldReturnIds() {
        when(followMapper.selectFollowerIds(200L)).thenReturn(Arrays.asList(100L));

        List<Long> ids = followService.getFollowerIds(200L);
        assertEquals(1, ids.size());
        assertTrue(ids.contains(100L));
    }
}

package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.FollowMapper;
import com.pettrail.pettrailbackend.mapper.PetMapper;
import com.pettrail.pettrailbackend.mapper.PostLikeMapper;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PetMapper petMapper;

    @Mock
    private FollowMapper followMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostLikeMapper postLikeMapper;

    @Mock
    private FollowService followService;

    @Mock
    private PostService postService;

    @Mock
    private UserBehaviorService userBehaviorService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RecommendService recommendService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void recommendUsers_shouldReturnCachedResult() {
        List<Map<String, Object>> cachedList = Arrays.asList(
                Map.of("userId", 2L, "score", 0.8),
                Map.of("userId", 3L, "score", 0.6)
        );
        when(valueOperations.get("recommend:1")).thenReturn(cachedList);

        List<Map<String, Object>> result = recommendService.recommendUsers(1L, 1, 10);

        assertNotNull(result);
        verify(userMapper, never()).selectList(any());
    }

    @Test
    void recommendUsers_anonymousUser_shouldReturnNewUsers() {
        User candidateUser = new User();
        candidateUser.setId(2L);
        candidateUser.setNickname("候选用户");
        candidateUser.setCreatedAt(LocalDateTime.now().minusDays(5));

        when(userMapper.selectList(any())).thenReturn(Arrays.asList(candidateUser));

        List<Map<String, Object>> result = recommendService.recommendUsers(null, 1, 10);

        assertNotNull(result);
    }

    @Test
    void recommendPosts_shouldReturnCachedResult() {
        Post post = new Post();
        post.setId(1L);
        List<Post> cachedList = Arrays.asList(post);
        when(valueOperations.get("recommend:posts:1")).thenReturn(cachedList);

        List<Post> result = recommendService.recommendPosts(1L, 1, 10);

        assertNotNull(result);
        verify(postMapper, never()).selectList(any());
    }

    @Test
    void invalidateCache_shouldDeleteUserCache() {
        recommendService.invalidateCache(1L);
        verify(redisTemplate).delete("recommend:1");
    }

    @Test
    void invalidatePostCache_shouldDeletePostCache() {
        recommendService.invalidatePostCache(1L);
        verify(redisTemplate).delete("recommend:posts:1");
    }
}

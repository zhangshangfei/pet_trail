package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.dto.NotificationVO;
import com.pettrail.pettrailbackend.entity.Notification;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.NotificationMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import com.pettrail.pettrailbackend.websocket.NotificationWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private NotificationWebSocketHandler webSocketHandler;

    @InjectMocks
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setNickname("测试用户");
        testUser.setAvatar("https://example.com/avatar.jpg");
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.delete(anyString())).thenReturn(true);
        lenient().when(valueOperations.setIfAbsent(anyString(), any(), anyLong(), any(TimeUnit.class))).thenReturn(true);
    }

    @Test
    void createNotification_shouldNotNotifySelf() {
        notificationService.createNotification(1L, 1L, "like", 100L, "赞了你的动态");
        verify(notificationMapper, never()).insert(any(Notification.class));
    }

    @Test
    void createNotification_shouldInsertWhenDifferentUser() {
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        notificationService.createNotification(1L, 2L, "like", 100L, "赞了你的动态");
        verify(notificationMapper, times(1)).insert(notificationCaptor.capture());
        Notification captured = notificationCaptor.getValue();
        assertEquals(1L, captured.getUserId());
        assertEquals(2L, captured.getFromUserId());
        assertEquals("like", captured.getType());
        assertFalse(captured.getIsRead());
    }

    @Test
    void getUnreadCount_shouldReturnCorrectCount() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(notificationMapper.selectCount(any())).thenReturn(5L);
        int count = notificationService.getUnreadCount(1L);
        assertEquals(5, count);
    }

    @Test
    void getUnreadCount_shouldUseCache() {
        when(valueOperations.get(anyString())).thenReturn(3);
        int count = notificationService.getUnreadCount(1L);
        assertEquals(3, count);
        verify(notificationMapper, never()).selectCount(any());
    }

    @Test
    void markAsRead_shouldUpdateWhenOwnerMatches() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(1L);
        notification.setIsRead(false);

        when(notificationMapper.selectById(1L)).thenReturn(notification);
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        notificationService.markAsRead(1L, 1L);
        verify(notificationMapper).updateById(notificationCaptor.capture());
        assertTrue(notificationCaptor.getValue().getIsRead());
    }

    @Test
    void markAsRead_shouldNotUpdateWhenOwnerDoesNotMatch() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(2L);
        notification.setIsRead(false);

        when(notificationMapper.selectById(1L)).thenReturn(notification);
        notificationService.markAsRead(1L, 1L);
        verify(notificationMapper, never()).updateById(any(Notification.class));
    }

    @Test
    void markAllAsRead_shouldCallMapper() {
        when(notificationMapper.markAllAsRead(1L)).thenReturn(3);
        notificationService.markAllAsRead(1L);
        verify(notificationMapper).markAllAsRead(1L);
    }

    @Test
    void deleteNotification_shouldDeleteWhenOwnerMatches() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(1L);
        when(notificationMapper.selectById(1L)).thenReturn(notification);
        when(notificationMapper.deleteById(anyLong())).thenReturn(1);

        notificationService.deleteNotification(1L, 1L);
        verify(notificationMapper).deleteById(anyLong());
    }

    @Test
    void deleteNotification_shouldNotDeleteWhenOwnerDoesNotMatch() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(2L);
        when(notificationMapper.selectById(1L)).thenReturn(notification);

        notificationService.deleteNotification(1L, 1L);
        verify(notificationMapper, never()).deleteById(anyLong());
    }

    @Test
    void clearAllNotifications_shouldDeleteAll() {
        when(notificationMapper.delete(any())).thenReturn(5);
        notificationService.clearAllNotifications(1L);
        verify(notificationMapper).delete(any());
    }

    @Test
    void sendVaccineReminder_shouldCreateSystemNotification() {
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        notificationService.sendVaccineReminder(1L, 10L, "狂犬疫苗", LocalDate.of(2026, 5, 1));
        verify(notificationMapper).insert(notificationCaptor.capture());
        Notification captured = notificationCaptor.getValue();
        assertEquals(0L, captured.getFromUserId());
        assertEquals("system", captured.getType());
        assertTrue(captured.getContent().contains("狂犬疫苗"));
    }

    @Test
    void sendParasiteReminder_shouldCreateSystemNotification() {
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        notificationService.sendParasiteReminder(1L, 10L, 1, LocalDate.of(2026, 5, 1));
        verify(notificationMapper).insert(notificationCaptor.capture());
        Notification captured = notificationCaptor.getValue();
        assertEquals(0L, captured.getFromUserId());
        assertEquals("system", captured.getType());
        assertTrue(captured.getContent().contains("体外驱虫"));
    }

    @Test
    void sendParasiteReminder_type2_shouldShowInternalParasite() {
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        notificationService.sendParasiteReminder(1L, 10L, 2, LocalDate.of(2026, 5, 1));
        verify(notificationMapper).insert(notificationCaptor.capture());
        assertTrue(notificationCaptor.getValue().getContent().contains("体内驱虫"));
    }

    @Test
    void getNotifications_shouldReturnVOList() {
        Notification n1 = new Notification();
        n1.setId(1L);
        n1.setUserId(1L);
        n1.setFromUserId(2L);
        n1.setType("like");
        n1.setContent("赞了你的动态");
        n1.setIsRead(false);
        n1.setCreatedAt(LocalDateTime.now());

        when(notificationMapper.selectList(any())).thenReturn(Arrays.asList(n1));
        when(userMapper.selectById(2L)).thenReturn(testUser);

        List<NotificationVO> result = notificationService.getNotifications(1L, 1, 10, "like");
        assertEquals(1, result.size());
        assertEquals("测试用户", result.get(0).getFromUserName());
    }

    @Test
    void getNotifications_systemNotification_shouldShowSystemName() {
        Notification n1 = new Notification();
        n1.setId(1L);
        n1.setUserId(1L);
        n1.setFromUserId(0L);
        n1.setType("system");
        n1.setContent("系统通知");
        n1.setIsRead(false);
        n1.setCreatedAt(LocalDateTime.now());

        when(notificationMapper.selectList(any())).thenReturn(Arrays.asList(n1));

        List<NotificationVO> result = notificationService.getNotifications(1L, 1, 10, "system");
        assertEquals(1, result.size());
        assertEquals("系统通知", result.get(0).getFromUserName());
    }

    @Test
    void createNotification_shouldDeduplicate() {
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);

        notificationService.createNotification(1L, 2L, "like", 100L, "赞了你的动态");
        verify(notificationMapper, times(1)).insert(any(Notification.class));

        when(valueOperations.setIfAbsent(anyString(), any(), anyLong(), any(TimeUnit.class))).thenReturn(false);
        notificationService.createNotification(1L, 2L, "like", 100L, "赞了你的动态");
        verify(notificationMapper, times(1)).insert(any(Notification.class));
    }
}

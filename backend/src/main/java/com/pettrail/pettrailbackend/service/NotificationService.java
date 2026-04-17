package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.NotificationVO;
import com.pettrail.pettrailbackend.entity.Notification;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.NotificationMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String UNREAD_COUNT_PREFIX = "notification:unread:";
    private static final long UNREAD_CACHE_TTL_MINUTES = 10;
    private static final String NOTIFICATION_DEDUP_PREFIX = "notification:dedup:";

    public void createNotification(Long userId, Long fromUserId, String type, Long targetId, String content) {
        if (userId.equals(fromUserId)) {
            return;
        }

        String dedupKey = NOTIFICATION_DEDUP_PREFIX + userId + ":" + fromUserId + ":" + type + ":" + targetId;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", 60, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isNew)) {
            log.debug("通知去重，跳过：userId={}, fromUserId={}, type={}, targetId={}", userId, fromUserId, type, targetId);
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(fromUserId);
        notification.setType(type);
        notification.setTargetId(targetId);
        notification.setContent(content);
        notification.setIsRead(false);
        notificationMapper.insert(notification);

        invalidateUnreadCache(userId);
    }

    public List<NotificationVO> getNotifications(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt)
                .last("LIMIT " + offset + "," + size);

        List<Notification> notifications = notificationMapper.selectList(wrapper);
        return notifications.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public int getUnreadCount(Long userId) {
        String cacheKey = UNREAD_COUNT_PREFIX + userId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return Integer.parseInt(cached.toString());
            }
        } catch (Exception e) {
            log.warn("未读数缓存读取异常: {}", e.getMessage());
        }

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false);
        int count = Math.toIntExact(notificationMapper.selectCount(wrapper));

        try {
            redisTemplate.opsForValue().set(cacheKey, count, UNREAD_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("未读数缓存写入异常: {}", e.getMessage());
        }

        return count;
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setIsRead(true);
            notificationMapper.updateById(notification);
            invalidateUnreadCache(userId);
        }
    }

    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
        invalidateUnreadCache(userId);
    }

    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notificationMapper.deleteById(notificationId);
            invalidateUnreadCache(userId);
            log.info("删除通知：notificationId={}, userId={}", notificationId, userId);
        }
    }

    public void clearAllNotifications(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId);
        notificationMapper.delete(wrapper);
        invalidateUnreadCache(userId);
        log.info("清空所有通知：userId={}", userId);
    }

    public void sendVaccineReminder(Long userId, Long petId, String vaccineName, LocalDate nextDate) {
        createNotification(userId, 0L, "system", petId,
                "疫苗提醒：您的宠物即将在 " + nextDate + " 接种 " + vaccineName);
    }

    public void sendParasiteReminder(Long userId, Long petId, Integer type, LocalDate nextDate) {
        String typeName = type != null && type == 2 ? "体内驱虫" : "体外驱虫";
        createNotification(userId, 0L, "system", petId,
                "驱虫提醒：您的宠物即将在 " + nextDate + " 进行" + typeName);
    }

    public void sendCheckinAchievementNotification(Long userId, String achievementName) {
        createNotification(userId, 0L, "system", null,
                "恭喜解锁成就：" + achievementName);
    }

    public void sendWelcomeNotification(Long userId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(0L);
        notification.setType("system");
        notification.setContent("欢迎来到宠迹！开始记录你和宠物的美好时光吧 🎉");
        notification.setIsRead(false);
        notificationMapper.insert(notification);
    }

    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setUserId(notification.getUserId());
        vo.setFromUserId(notification.getFromUserId());
        vo.setType(notification.getType());
        vo.setTargetId(notification.getTargetId());
        vo.setContent(notification.getContent());
        vo.setIsRead(notification.getIsRead());
        vo.setCreatedAt(notification.getCreatedAt());

        if (notification.getFromUserId() != null && notification.getFromUserId() == 0L) {
            vo.setFromUserName("系统通知");
            vo.setFromUserAvatar("");
            return vo;
        }

        try {
            User fromUser = userMapper.selectById(notification.getFromUserId());
            if (fromUser != null) {
                vo.setFromUserName(fromUser.getNickname() != null ? fromUser.getNickname() : "萌宠主人");
                vo.setFromUserAvatar(fromUser.getAvatar() != null ? fromUser.getAvatar() : "");
            } else {
                vo.setFromUserName("未知用户");
                vo.setFromUserAvatar("");
            }
        } catch (Exception e) {
            vo.setFromUserName("未知用户");
            vo.setFromUserAvatar("");
        }

        return vo;
    }

    private void invalidateUnreadCache(Long userId) {
        try {
            redisTemplate.delete(UNREAD_COUNT_PREFIX + userId);
        } catch (Exception e) {
            log.warn("未读数缓存清除异常: {}", e.getMessage());
        }
    }
}

package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.NotificationVO;
import com.pettrail.pettrailbackend.entity.Notification;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.NotificationMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    public void createNotification(Long userId, Long fromUserId, String type, Long targetId, String content) {
        if (userId.equals(fromUserId)) {
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
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false);
        return Math.toIntExact(notificationMapper.selectCount(wrapper));
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setIsRead(true);
            notificationMapper.updateById(notification);
        }
    }

    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
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
}

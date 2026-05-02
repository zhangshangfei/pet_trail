package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationAdminVO {

    private Long id;

    private Long userId;

    private String userNickname;

    private String userAvatar;

    private Long fromUserId;

    private String fromUserNickname;

    private String fromUserAvatar;

    private String type;

    private Long targetId;

    private String content;

    private String title;

    private Boolean isRead;

    private LocalDateTime createdAt;
}

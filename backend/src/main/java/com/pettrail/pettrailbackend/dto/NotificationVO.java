package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {

    private Long id;

    private Long userId;

    private Long fromUserId;

    private String type;

    private Long targetId;

    private String content;

    private String title;

    private Boolean isRead;

    private LocalDateTime createdAt;

    private String fromUserName;

    private String fromUserAvatar;
}

package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackAdminVO {

    private Long id;

    private Long userId;

    private String userNickname;

    private String userAvatar;

    private String type;

    private String content;

    private String contact;

    private String images;

    private Integer status;

    private String reply;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

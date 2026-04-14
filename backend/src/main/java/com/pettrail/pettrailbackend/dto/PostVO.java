package com.pettrail.pettrailbackend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态 VO（包含用户点赞状态、用户信息、宠物信息）
 */
@Data
public class PostVO {

    private Long id;

    private Long userId;

    private Long petId;

    private String content;

    private String images;

    private List<String> imageList;

    private Integer likeCount;

    private Integer commentCount;

    private Integer shareCount;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 当前用户是否已点赞
    private Boolean liked;

    // 用户信息
    private String userName;

    private String userAvatar;

    // 宠物信息
    private String petName;

    private String petAvatar;

    private Integer petType;

    private Integer petAge;
}

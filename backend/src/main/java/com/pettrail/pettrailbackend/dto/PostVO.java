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

    private String videos;

    private List<String> videoList;

    private Integer likeCount;

    private Integer commentCount;

    private Integer shareCount;

    private String location;

    private List<String> stickers;

    private BubbleVO bubble;

    private List<String> tags;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 当前用户是否已点赞
    private Boolean liked;

    // 当前用户是否已收藏/ee
    private Boolean eeLiked;

    // ee/收藏计数
    private Integer eeCount;

    // 用户信息
    private String userName;

    private String userAvatar;

    // 宠物信息
    private String petName;

    private String petAvatar;

    private Integer petType;

    private String petAge;

    @Data
    public static class BubbleVO {
        private String text;
        private String bgColor;
        private String textColor;
    }
}

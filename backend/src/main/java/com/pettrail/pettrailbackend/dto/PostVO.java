package com.pettrail.pettrailbackend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态 VO（包含用户点赞状态）
 */
@Data
public class PostVO {

    private Long id;

    private Long userId;

    private Long petId;

    private String content;

    private String images;

    private Integer likeCount;

    private Integer commentCount;

    private Integer shareCount;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    // 当前用户是否已点赞
    private Boolean liked;
}

package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态实体
 */
@Data
@TableName("posts")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long petId;

    private String content;

    private String images;

    private String videos;

    private Integer likeCount;

    private Integer commentCount;

    private Integer shareCount;

    private Integer eeCount;

    private String location;

    private String stickers;

    private String bubble;

    private Integer status;

    private Integer auditStatus;

    private String auditRemark;

    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

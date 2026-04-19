package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notifications")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long fromUserId;
    private String type;
    private Long targetId;
    private String content;
    private String title;
    private Boolean isRead;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String userNickname;

    @TableField(exist = false)
    private String fromUserNickname;
}

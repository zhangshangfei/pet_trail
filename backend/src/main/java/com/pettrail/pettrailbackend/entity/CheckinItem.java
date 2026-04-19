package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 打卡项实体
 */
@Data
@TableName("checkin_items")
public class CheckinItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String icon;

    private Integer type;

    private String description;

    private Integer sortOrder;

    private Integer isDefault;

    private Integer isEnabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

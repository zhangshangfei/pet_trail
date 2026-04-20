package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("achievements")
public class Achievement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String icon;

    private Integer type;

    private String conditionType;

    private Integer conditionValue;

    private Integer sortOrder;

    private Integer isEnabled;

    private LocalDateTime createdAt;
}

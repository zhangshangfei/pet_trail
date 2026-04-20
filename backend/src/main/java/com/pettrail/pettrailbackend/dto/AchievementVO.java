package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AchievementVO {

    private Long id;

    private String name;

    private String description;

    private String icon;

    private Integer type;

    private String typeName;

    private String conditionType;

    private Integer conditionValue;

    private Integer sortOrder;

    private Boolean unlocked;

    private LocalDateTime unlockedAt;

    private Integer currentProgress;

    private Integer status;
}

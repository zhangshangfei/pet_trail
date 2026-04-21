package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_model")
public class AiModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String modelName;
    private String displayName;
    private String provider;
    private String baseUrl;
    private String apiKey;
    private String modelVersion;
    private String parameters;
    private Integer status;
    private Integer isDefault;
    private Integer sortOrder;
    private String description;
    private String icon;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiModelVO {
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
    private Boolean isActive;
    private Long callCount;
    private Long successCount;
    private Long failCount;
    private Double avgResponseTime;
}

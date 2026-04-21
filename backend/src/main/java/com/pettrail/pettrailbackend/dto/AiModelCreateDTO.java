package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class AiModelCreateDTO {
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
}

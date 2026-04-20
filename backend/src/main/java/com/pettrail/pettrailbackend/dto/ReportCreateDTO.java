package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class ReportCreateDTO {
    private Long targetId;
    private String targetType;
    private String reason;
    private String description;
}

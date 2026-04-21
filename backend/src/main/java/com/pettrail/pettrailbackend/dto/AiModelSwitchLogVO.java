package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiModelSwitchLogVO {
    private Long id;
    private Long fromModelId;
    private String fromModelName;
    private Long toModelId;
    private String toModelName;
    private String switchType;
    private Long operatorId;
    private String operatorName;
    private String reason;
    private String status;
    private Long duration;
    private String errorMessage;
    private LocalDateTime createdAt;
}

package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportVO {

    private Long id;

    private Long reporterId;

    private Long targetId;

    private String targetType;

    private String targetNickname;

    private String reason;

    private String description;

    private Integer status;

    private String result;

    private LocalDateTime createdAt;
}

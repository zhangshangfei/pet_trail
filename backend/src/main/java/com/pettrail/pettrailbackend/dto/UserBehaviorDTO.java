package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class UserBehaviorDTO {
    private String action;
    private String targetType;
    private Long targetId;
    private Integer duration;
    private String extra;
}

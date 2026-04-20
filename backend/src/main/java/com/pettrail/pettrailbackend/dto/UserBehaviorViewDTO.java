package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class UserBehaviorViewDTO {
    private String targetType;
    private Long targetId;
    private Integer duration;
}

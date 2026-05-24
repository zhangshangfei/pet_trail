package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class FollowActionVO {

    private Boolean following;
    private Integer followerCount;
    private Integer followeeCount;
}

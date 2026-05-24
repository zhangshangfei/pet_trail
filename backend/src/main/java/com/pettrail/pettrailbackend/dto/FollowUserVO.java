package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class FollowUserVO {

    private Long id;
    private String nickname;
    private String avatar;
    private Integer gender;
    private Integer followerCount;
    private Integer followeeCount;
    private Integer postCount;
    private Boolean isFollowing;
}

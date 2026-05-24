package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecommendUserVO {

    private Long id;
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDateTime createdAt;
    private Integer followerCount;
    private Integer followeeCount;
    private Integer postCount;
    private Boolean isFollowing;
    private Double recommendScore;
    private String recommendReason;
}

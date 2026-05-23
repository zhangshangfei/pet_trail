package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChallengeParticipantVO {

    private Long id;

    private Long challengeId;

    private Long userId;

    private Integer progress;

    private Boolean completed;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;

    private String title;

    private String description;

    private String coverImage;

    private Integer type;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String conditionType;

    private Integer conditionValue;

    private String rewardDescription;

    private Integer participantCount;
}

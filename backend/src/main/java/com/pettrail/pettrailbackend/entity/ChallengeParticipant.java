package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("challenge_participants")
public class ChallengeParticipant {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long challengeId;
    private Long userId;
    private Integer progress;
    private Boolean completed;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}

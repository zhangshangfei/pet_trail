package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_model_switch_log")
public class AiModelSwitchLog {
    @TableId(type = IdType.AUTO)
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

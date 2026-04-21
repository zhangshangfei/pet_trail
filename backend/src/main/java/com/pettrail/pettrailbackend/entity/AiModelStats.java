package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("ai_model_stats")
public class AiModelStats {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long modelId;
    private LocalDate statsDate;
    private Long callCount;
    private Long successCount;
    private Long failCount;
    private Long totalResponseTime;
    private Double avgResponseTime;
    private Double successRate;
    private Long minResponseTime;
    private Long maxResponseTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 步数记录实体
 */
@Data
@TableName("step_records")
public class StepRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long petId;

    private Integer steps;

    private BigDecimal distance;

    private LocalDate recordDate;

    private Integer source;

    private LocalDateTime createdAt;
}

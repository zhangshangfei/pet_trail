package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("weight_records")
public class WeightRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long petId;           // 宠物ID
    private BigDecimal weight;    // 体重(kg)
    private LocalDate recordDate; // 记录日期
    private String note;          // 备注

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

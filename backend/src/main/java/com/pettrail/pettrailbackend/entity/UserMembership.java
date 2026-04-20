package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_membership")
public class UserMembership {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String plan;

    private BigDecimal amount;

    private LocalDateTime startDate;

    private LocalDateTime expireDate;

    private Integer status;

    private String orderNo;

    private String transactionId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

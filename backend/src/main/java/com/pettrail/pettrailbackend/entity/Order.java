package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String orderNo;

    private String plan;

    private BigDecimal amount;

    private Integer status;

    private String wxTransactionId;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;
}

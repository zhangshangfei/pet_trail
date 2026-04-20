package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {

    private Long id;

    private String orderNo;

    private String plan;

    private BigDecimal amount;

    private Integer status;

    private String wxTransactionId;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    private String payParams;

    private LocalDateTime expireDate;
}

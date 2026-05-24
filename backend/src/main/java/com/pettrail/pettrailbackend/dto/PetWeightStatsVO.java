package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PetWeightStatsVO {

    private Long petId;
    private String petName;
    private String petAvatar;
    private BigDecimal avgWeight;
    private BigDecimal maxWeight;
    private BigDecimal minWeight;
    private BigDecimal totalWeightChange;
    private Integer recordCount;
}

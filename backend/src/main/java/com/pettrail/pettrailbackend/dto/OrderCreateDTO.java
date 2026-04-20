package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCreateDTO {

    private String plan;

    private Long petId;
}

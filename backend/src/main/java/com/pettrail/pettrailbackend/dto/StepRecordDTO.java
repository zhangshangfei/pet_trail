package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class StepRecordDTO {
    private Integer steps;
    private java.math.BigDecimal distance;
    private String recordDate;
    private Long petId;
}

package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class WaterRecordDTO {
    private java.math.BigDecimal amount;
    private String recordDate;
    private String recordTime;
    private Long petId;
}

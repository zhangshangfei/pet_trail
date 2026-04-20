package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class WeightRecordDTO {
    private java.math.BigDecimal weight;
    private String recordDate;
    private String note;
}

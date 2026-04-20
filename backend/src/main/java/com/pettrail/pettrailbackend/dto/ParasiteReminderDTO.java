package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class ParasiteReminderDTO {
    private Long petId;
    private Integer type;
    private String productName;
    private String nextDate;
    private Integer intervalDays;
    private String note;
}

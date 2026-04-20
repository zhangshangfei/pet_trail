package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class VaccineReminderDTO {
    private String vaccineName;
    private Integer vaccineType;
    private String nextDate;
    private Integer reminderDays;
    private Long petId;
    private String note;
}

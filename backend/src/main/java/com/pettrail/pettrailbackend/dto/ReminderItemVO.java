package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReminderItemVO {

    private String type;
    private Long petId;
    private String petName;
    private String name;
    private LocalDate date;
    private Integer daysUntil;
}

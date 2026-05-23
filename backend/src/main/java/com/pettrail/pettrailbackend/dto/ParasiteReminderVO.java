package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ParasiteReminderVO {

    private Long id;

    private Long petId;

    private Integer type;

    private String typeName;

    private String productName;

    private LocalDate nextDate;

    private Integer intervalDays;

    private Integer status;

    private String note;

    private LocalDateTime createdAt;
}

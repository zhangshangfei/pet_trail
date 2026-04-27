package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class VetAppointmentVO {
    private Long id;
    private Long userId;
    private Long clinicId;
    private Long petId;
    private LocalDate appointmentDate;
    private String appointmentTime;
    private String symptom;
    private Integer status;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userName;
    private String clinicName;
    private String petName;
}

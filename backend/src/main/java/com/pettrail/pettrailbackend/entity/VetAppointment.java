package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("vet_appointments")
public class VetAppointment {
    @TableId(type = IdType.AUTO)
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
}

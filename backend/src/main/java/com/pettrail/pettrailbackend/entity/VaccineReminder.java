package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 疫苗提醒实体
 */
@Data
@TableName("vaccine_reminders")
public class VaccineReminder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long petId;

    private Long userId;

    private String vaccineName;

    private Integer vaccineType;

    private LocalDate nextDate;

    private Integer status;

    private Integer reminderDays;

    private Integer isNotified;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

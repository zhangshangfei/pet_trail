package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("vaccine_reminders")
public class VaccineReminder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long petId;           // 宠物ID
    private String vaccineName;   // 疫苗名称
    private LocalDate nextDate;   // 下次接种日期
    private Integer status;       // 状态: 0-未接种, 1-已接种

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

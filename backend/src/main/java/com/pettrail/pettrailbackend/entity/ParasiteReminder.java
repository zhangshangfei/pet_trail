package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 驱虫提醒实体
 */
@Data
@TableName("parasite_reminders")
public class ParasiteReminder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long petId;

    private Long userId;

    private Integer type;

    private String productName;

    private LocalDate nextDate;

    private Integer intervalDays;

    private Integer status;

    private Integer isNotified;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

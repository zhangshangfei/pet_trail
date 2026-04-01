package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("parasite_reminders")
public class ParasiteReminder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long petId;           // 宠物ID
    private String type;          // 类型: 体内, 体外
    private LocalDate nextDate;   // 下次驱虫日期
    private Integer status;       // 状态: 0-未完成, 1-已完成

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

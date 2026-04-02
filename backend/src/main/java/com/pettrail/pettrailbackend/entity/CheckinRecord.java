package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡记录实体
 */
@Data
@TableName("checkin_records_202401")
public class CheckinRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long petId;

    private Long itemId;

    private LocalDate recordDate;

    private Integer status;

    private String note;

    private String images;

    private LocalDateTime createdAt;
}

package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡统计实体
 */
@Data
@TableName("checkin_stats")
public class CheckinStats {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long itemId;

    private Integer totalCount;

    private Integer currentStreak;

    private Integer maxStreak;

    private LocalDate lastCheckinDate;

    private LocalDateTime updatedAt;
}

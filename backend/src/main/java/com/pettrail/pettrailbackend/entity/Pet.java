package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pets")
public class Pet {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;           // 所属用户ID
    private String name;           // 宠物名称
    private String breed;          // 品种
    private Integer gender;        // 性别: 0-未知, 1-公, 2-母
    private Integer sterilized;    // 是否绝育: 0-未绝育, 1-已绝育
    private Integer category;      // 类别: 0-其他, 1-猫, 2-狗
    private LocalDate birthday;    // 生日
    private String avatar;         // 头像URL
    private BigDecimal weight;     // 当前体重(kg)
    private String color;          // 毛色

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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

    private Long userId;
    private String name;
    private String breed;
    private Integer gender;
    private Integer sterilized;
    private Integer category;
    private LocalDate birthday;
    private String avatar;
    private BigDecimal weight;
    private String color;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String userNickname;
}

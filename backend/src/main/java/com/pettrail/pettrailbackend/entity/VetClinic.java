package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("vet_clinics")
public class VetClinic {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String coverImage;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private String businessHours;
    private BigDecimal rating;
    private String specialties;
    private Boolean isPartner;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

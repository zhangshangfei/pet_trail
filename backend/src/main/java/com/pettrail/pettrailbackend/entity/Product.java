package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String coverImage;
    private String images;
    private String category;
    private String brand;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal commissionRate;
    private String sourceUrl;
    private String sourcePlatform;
    private Integer salesCount;
    private BigDecimal rating;
    private Integer petType;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

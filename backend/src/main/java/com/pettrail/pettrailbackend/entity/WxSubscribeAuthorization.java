package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wx_subscribe_authorization")
public class WxSubscribeAuthorization {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String templateType;

    private Integer credits;

    private Integer usedCredits;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

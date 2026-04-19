package com.pettrail.pettrailbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_hidden_items")
public class UserHiddenItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long itemId;

    private LocalDateTime createdAt;
}

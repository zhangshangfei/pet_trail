package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
    private String permissions;
    private Long merchantId;
    private String merchantName;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}

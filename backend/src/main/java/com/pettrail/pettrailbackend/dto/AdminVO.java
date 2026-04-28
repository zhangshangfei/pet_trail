package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class AdminVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Long roleId;
    private String roleName;
    private String roleCode;
    private Long merchantId;
    private String merchantName;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private List<String> permissions;
    private List<String> buttons;
}

package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class AdminCreateDTO {
    private String username;
    private String password;
    private String nickname;
    private String role;
    private String permissions;
    private Long merchantId;
}

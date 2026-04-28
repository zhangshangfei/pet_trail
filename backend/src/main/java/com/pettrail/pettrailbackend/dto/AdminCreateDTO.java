package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class AdminCreateDTO {
    private String username;
    private String password;
    private String nickname;
    private Long roleId;
    private Long merchantId;
}

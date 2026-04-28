package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class AdminUpdateDTO {
    private String nickname;
    private Long roleId;
    private Long merchantId;
}

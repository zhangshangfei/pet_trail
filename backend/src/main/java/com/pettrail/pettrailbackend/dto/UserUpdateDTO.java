package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String nickname;
    private String avatar;
    private String phone;
    private Integer gender;
}

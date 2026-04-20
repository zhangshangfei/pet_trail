package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class CheckinItemRequest {

    private String name;
    private String icon;
    private Integer type;
    private String description;
}

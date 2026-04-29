package com.pettrail.pettrailbackend.dto;

import lombok.Data;


@Data
public class OrderCreateDTO {

    private String plan;

    private Long petId;
}

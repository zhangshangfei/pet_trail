package com.pettrail.pettrailbackend.dto;

import lombok.Data;


@Data
public class TagVO {
    private Long id;
    private String name;
    private Integer usageCount;
    private Boolean isHot;
    private Boolean isOfficial;
}

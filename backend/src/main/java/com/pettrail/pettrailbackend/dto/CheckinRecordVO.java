package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CheckinRecordVO {

    private Long id;

    private Long petId;

    private Long itemId;

    private String itemName;

    private String itemIcon;

    private LocalDate recordDate;

    private Integer status;

    private String note;

    private String images;

    private LocalDateTime createdAt;
}

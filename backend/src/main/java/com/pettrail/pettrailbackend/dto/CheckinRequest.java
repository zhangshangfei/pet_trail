package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class CheckinRequest {

    private Long itemId;
    private java.util.List<Long> itemIds;
    private Long petId;
    private String note;
    private java.util.List<String> images;
    private String date;
}

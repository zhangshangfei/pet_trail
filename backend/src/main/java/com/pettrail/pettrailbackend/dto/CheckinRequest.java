package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class CheckinRequest {

    private Long itemId;
    private Long petId;
    private String note;
    private java.util.List<String> images;
    private String date;
}

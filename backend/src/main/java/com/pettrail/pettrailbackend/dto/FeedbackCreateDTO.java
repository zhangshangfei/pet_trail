package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class FeedbackCreateDTO {
    private String type;
    private String content;
    private String contact;
    private String images;
}

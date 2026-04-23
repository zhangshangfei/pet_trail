package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class FeedbackCreateDTO {
    private String type;
    private String content;
    private String contact;
    private List<String> images;
}

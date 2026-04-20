package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PostCreateDTO {
    private String content;
    private Long petId;
    private List<String> images;
    private List<String> videos;
    private List<String> stickers;
    private Map<String, String> bubble;
    private String location;
    private List<String> tags;
}

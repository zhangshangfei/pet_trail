package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class UploadBase64DTO {
    private String fileBase64;
    private String fileName;
    private String contentType;
}

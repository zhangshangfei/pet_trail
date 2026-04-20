package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class PetAlbumDTO {
    private String imageUrl;
    private String title;
    private String note;
    private String recordDate;
}

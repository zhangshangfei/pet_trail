package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PetAlbumVO {

    private Long id;

    private Long petId;

    private String imageUrl;

    private String title;

    private String note;

    private LocalDate recordDate;

    private LocalDateTime createdAt;
}

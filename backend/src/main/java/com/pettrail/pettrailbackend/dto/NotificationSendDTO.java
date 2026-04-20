package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class NotificationSendDTO {
    private Long userId;
    private String content;
    private String title;
}

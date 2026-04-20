package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class CheckinReminderDTO {
    private Long itemId;
    private String remindTime;
}

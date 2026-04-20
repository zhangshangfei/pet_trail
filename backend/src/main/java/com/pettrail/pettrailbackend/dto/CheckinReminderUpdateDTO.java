package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class CheckinReminderUpdateDTO {
    private Long itemId;
    private String remindTime;
    private Boolean isEnabled;
}

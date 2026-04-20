package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class FeedingReminderDTO {
    private Long petId;
    private String mealType;
    private String time;
    private String repeat;
    private String note;
}

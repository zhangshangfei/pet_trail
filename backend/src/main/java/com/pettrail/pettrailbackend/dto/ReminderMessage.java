package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReminderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;

    private Long reminderId;

    private Long userId;

    private Long petId;

    private String payload;

    public static ReminderMessage feeding(Long reminderId, Long userId, Long petId, String payload) {
        ReminderMessage msg = new ReminderMessage();
        msg.setType("feeding");
        msg.setReminderId(reminderId);
        msg.setUserId(userId);
        msg.setPetId(petId);
        msg.setPayload(payload);
        return msg;
    }

    public static ReminderMessage checkin(Long reminderId, Long userId, Long petId, String payload) {
        ReminderMessage msg = new ReminderMessage();
        msg.setType("checkin");
        msg.setReminderId(reminderId);
        msg.setUserId(userId);
        msg.setPetId(petId);
        msg.setPayload(payload);
        return msg;
    }
}

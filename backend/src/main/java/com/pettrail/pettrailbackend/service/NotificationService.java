package com.pettrail.pettrailbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 通知服务
 * 后续接入微信小程序模板消息
 */
@Slf4j
@Service
public class NotificationService {

    /**
     * 发送疫苗提醒
     */
    public void sendVaccineReminder(Long userId, Long petId, String vaccineName, LocalDate nextDate) {
        log.info("发送疫苗提醒：userId={}, petId={}, vaccineName={}, nextDate={}", 
            userId, petId, vaccineName, nextDate);
        // TODO: 接入微信小程序模板消息
        // templateMsgService.sendVaccineReminder(userId, petId, vaccineName, nextDate);
    }

    /**
     * 发送驱虫提醒
     */
    public void sendParasiteReminder(Long userId, Long petId, Integer type, LocalDate nextDate) {
        String typeName = type == 1 ? "体内驱虫" : "体外驱虫";
        log.info("发送驱虫提醒：userId={}, petId={}, type={}, nextDate={}", 
            userId, petId, typeName, nextDate);
        // TODO: 接入微信小程序模板消息
    }

    /**
     * 发送打卡提醒
     */
    public void sendCheckinReminder(Long userId, String itemName) {
        log.info("发送打卡提醒：userId={}, itemName={}", userId, itemName);
        // TODO: 接入微信小程序模板消息
    }
}

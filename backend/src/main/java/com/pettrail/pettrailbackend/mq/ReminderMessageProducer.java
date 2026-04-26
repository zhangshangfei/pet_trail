package com.pettrail.pettrailbackend.mq;

import com.pettrail.pettrailbackend.config.RabbitMQConfig;
import com.pettrail.pettrailbackend.dto.ReminderMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReminderMessageProducer {

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    public void sendDelayedFeedingReminder(ReminderMessage message, long delayMillis) {
        sendDelayedMessage(message, RabbitMQConfig.FEEDING_ROUTING_KEY, delayMillis);
    }

    public void sendDelayedCheckinReminder(ReminderMessage message, long delayMillis) {
        sendDelayedMessage(message, RabbitMQConfig.CHECKIN_ROUTING_KEY, delayMillis);
    }

    public void cancelFeedingReminder(Long reminderId) {
        log.debug("喂食提醒取消（通过下次消费时校验DB状态实现）: reminderId={}", reminderId);
    }

    public void cancelCheckinReminder(Long reminderId) {
        log.debug("打卡提醒取消（通过下次消费时校验DB状态实现）: reminderId={}", reminderId);
    }

    private void sendDelayedMessage(ReminderMessage message, String routingKey, long delayMillis) {
        if (rabbitTemplate == null) {
            log.debug("RabbitMQ未启用，跳过延迟消息发送: type={}, reminderId={}",
                message.getType(), message.getReminderId());
            return;
        }
        try {
            MessagePostProcessor processor = msg -> {
                msg.getMessageProperties().setDelay((int) Math.min(delayMillis, Integer.MAX_VALUE));
                msg.getMessageProperties().setMessageId(
                    message.getType() + ":" + message.getReminderId() + ":" + System.currentTimeMillis());
                return msg;
            };

            rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELAYED_EXCHANGE_NAME,
                routingKey,
                message,
                processor
            );

            log.info("发送延迟消息: type={}, reminderId={}, delayMs={}, routingKey={}",
                message.getType(), message.getReminderId(), delayMillis, routingKey);
        } catch (Exception e) {
            log.error("发送延迟消息失败: type={}, reminderId={}, error={}",
                message.getType(), message.getReminderId(), e.getMessage());
        }
    }
}

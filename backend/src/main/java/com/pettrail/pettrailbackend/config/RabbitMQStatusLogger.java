package com.pettrail.pettrailbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
public class RabbitMQStatusLogger {

    @Value("${spring.rabbitmq.enabled:false}")
    private String rabbitmqEnabled;

    @Value("${RABBITMQ_HOST:localhost}")
    private String rabbitmqHost;

    @Value("${RABBITMQ_PORT:5672}")
    private String rabbitmqPort;

    @Value("${RABBITMQ_USERNAME:guest}")
    private String rabbitmqUsername;

    @Value("${RABBITMQ_VHOST:/}")
    private String rabbitmqVhost;

    @PostConstruct
    public void logStatus() {
        log.info("====== RabbitMQ 启用状态检查 ======");
        log.info("spring.rabbitmq.enabled = {}", rabbitmqEnabled);
        if ("true".equalsIgnoreCase(rabbitmqEnabled)) {
            log.info("RabbitMQ 已启用, 将尝试连接 {}:{}", rabbitmqHost, rabbitmqPort);
            log.info("RabbitMQ 用户名={}, vhost={}", rabbitmqUsername, rabbitmqVhost);
        } else {
            log.info("RabbitMQ 未启用, 将使用 FallbackReminderTask 定时任务兜底");
            log.info("如需启用 RabbitMQ, 请设置环境变量 RABBITMQ_ENABLED=true");
        }
    }
}

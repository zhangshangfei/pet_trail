package com.pettrail.pettrailbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMQConfig {

    public static final String DELAYED_EXCHANGE = "reminder.delayed.exchange";
    public static final String DLX_EXCHANGE = "reminder.dlx.exchange";
    public static final String FEEDING_QUEUE = "reminder.feeding.queue";
    public static final String CHECKIN_QUEUE = "reminder.checkin.queue";
    public static final String FEEDING_DLQ = "reminder.feeding.dlq";
    public static final String CHECKIN_DLQ = "reminder.checkin.dlq";
    public static final String FEEDING_ROUTING_KEY = "reminder.feeding";
    public static final String CHECKIN_ROUTING_KEY = "reminder.checkin";

    @PostConstruct
    public void init() {
        log.info("====== RabbitMQ 配置类已加载 ======");
        log.info("spring.rabbitmq.enabled=true, RabbitMQ 相关 Bean 将被创建");
    }

    @Bean
    public ConnectionFactory rabbitConnectionFactory(
            @Value("${RABBITMQ_HOST:localhost}") String host,
            @Value("${RABBITMQ_PORT:5672}") int port,
            @Value("${RABBITMQ_USERNAME:guest}") String username,
            @Value("${RABBITMQ_PASSWORD:guest}") String password,
            @Value("${RABBITMQ_VHOST:/}") String vhost) {
        log.info("====== RabbitMQ 环境变量读取 ======");
        log.info("RABBITMQ_HOST={}", host);
        log.info("RABBITMQ_PORT={}", port);
        log.info("RABBITMQ_USERNAME={}", username);
        log.info("RABBITMQ_PASSWORD={}", password.replaceAll(".", "*"));
        log.info("RABBITMQ_VHOST={}", vhost);

        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(vhost);

        try {
            Connection conn = factory.createConnection();
            if (conn != null && conn.isOpen()) {
                log.info("====== RabbitMQ 连接成功! host={}:{} vhost={} ======", host, port, vhost);
                conn.close();
            } else {
                log.warn("====== RabbitMQ 连接返回但未打开, host={}:{} ======", host, port);
            }
        } catch (Exception e) {
            log.error("====== RabbitMQ 连接失败! host={}:{} 错误: {} ======", host, port, e.getMessage(), e);
        }

        return factory;
    }

    @Bean
    public CustomExchange delayedExchange() {
        log.info("创建延迟交换机: {} (type=x-delayed-message)", DELAYED_EXCHANGE);
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(DELAYED_EXCHANGE, "x-delayed-message", true, false, args);
    }

    @Bean
    public DirectExchange dlxExchange() {
        log.info("创建死信交换机: {}", DLX_EXCHANGE);
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue feedingQueue() {
        log.info("创建喂食提醒队列: {} (DLX={})", FEEDING_QUEUE, DLX_EXCHANGE);
        return QueueBuilder.durable(FEEDING_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "feeding.dlq")
                .build();
    }

    @Bean
    public Queue checkinQueue() {
        log.info("创建打卡提醒队列: {} (DLX={})", CHECKIN_QUEUE, DLX_EXCHANGE);
        return QueueBuilder.durable(CHECKIN_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "checkin.dlq")
                .build();
    }

    @Bean
    public Queue feedingDlq() {
        log.info("创建喂食死信队列: {}", FEEDING_DLQ);
        return QueueBuilder.durable(FEEDING_DLQ).build();
    }

    @Bean
    public Queue checkinDlq() {
        log.info("创建打卡死信队列: {}", CHECKIN_DLQ);
        return QueueBuilder.durable(CHECKIN_DLQ).build();
    }

    @Bean
    public Binding feedingBinding() {
        return BindingBuilder.bind(feedingQueue())
                .to(delayedExchange())
                .with(FEEDING_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding checkinBinding() {
        return BindingBuilder.bind(checkinQueue())
                .to(delayedExchange())
                .with(CHECKIN_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding feedingDlqBinding() {
        return BindingBuilder.bind(feedingDlq()).to(dlxExchange()).with("feeding.dlq");
    }

    @Bean
    public Binding checkinDlqBinding() {
        return BindingBuilder.bind(checkinDlq()).to(dlxExchange()).with("checkin.dlq");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(10);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("消息成功到达交换机: {}", correlationData);
            } else {
                log.warn("消息发送失败: {}", cause);
            }
        });
        template.setReturnsCallback(returned -> {
            log.warn("消息路由失败被退回: {}", new String(returned.getMessage().getBody()));
        });
        return template;
    }
}

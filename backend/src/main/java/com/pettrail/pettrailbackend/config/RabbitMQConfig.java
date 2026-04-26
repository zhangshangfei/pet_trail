package com.pettrail.pettrailbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public ConnectionFactory rabbitConnectionFactory(
            @Value("${RABBITMQ_HOST:localhost}") String host,
            @Value("${RABBITMQ_PORT:5672}") int port,
            @Value("${RABBITMQ_USERNAME:guest}") String username,
            @Value("${RABBITMQ_PASSWORD:guest}") String password,
            @Value("${RABBITMQ_VHOST:/}") String vhost) {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(vhost);
        log.info("RabbitMQ ConnectionFactory 创建: host={}:{}", host, port);
        return factory;
    }

    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(DELAYED_EXCHANGE, "x-delayed-message", true, false, args);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue feedingQueue() {
        return QueueBuilder.durable(FEEDING_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "feeding.dlq")
                .build();
    }

    @Bean
    public Queue checkinQueue() {
        return QueueBuilder.durable(CHECKIN_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "checkin.dlq")
                .build();
    }

    @Bean
    public Queue feedingDlq() {
        return QueueBuilder.durable(FEEDING_DLQ).build();
    }

    @Bean
    public Queue checkinDlq() {
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

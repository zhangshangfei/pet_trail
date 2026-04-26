package com.pettrail.pettrailbackend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String REMINDER_EXCHANGE = "reminder.delayed.exchange";
    public static final String FEEDING_QUEUE = "reminder.feeding.queue";
    public static final String CHECKIN_QUEUE = "reminder.checkin.queue";
    public static final String FEEDING_ROUTING_KEY = "reminder.feeding";
    public static final String CHECKIN_ROUTING_KEY = "reminder.checkin";
    public static final String DELAY_HEADER = "x-delay";

    @Bean
    public CustomExchange reminderDelayedExchange() {
        return new CustomExchange(REMINDER_EXCHANGE, "x-delayed-message",
                true, false,
                java.util.Map.of("x-delayed-type", "direct"));
    }

    @Bean
    public Queue feedingQueue() {
        return QueueBuilder.durable(FEEDING_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", FEEDING_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue checkinQueue() {
        return QueueBuilder.durable(CHECKIN_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", CHECKIN_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue feedingDlq() {
        return QueueBuilder.durable(FEEDING_QUEUE + ".dlq").build();
    }

    @Bean
    public Queue checkinDlq() {
        return QueueBuilder.durable(CHECKIN_QUEUE + ".dlq").build();
    }

    @Bean
    public Binding feedingBinding() {
        return BindingBuilder.bind(feedingQueue())
                .to(reminderDelayedExchange())
                .with(FEEDING_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding checkinBinding() {
        return BindingBuilder.bind(checkinQueue())
                .to(reminderDelayedExchange())
                .with(CHECKIN_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(10);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}

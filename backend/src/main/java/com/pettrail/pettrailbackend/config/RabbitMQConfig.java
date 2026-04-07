// RabbitMQ 配置类已注释，改用 Spring Event + 定时任务兜底
// package com.pettrail.pettrailbackend.config;
//
// import org.springframework.amqp.core.*;
// import org.springframework.amqp.rabbit.connection.ConnectionFactory;
// import org.springframework.amqp.rabbit.core.RabbitTemplate;
// import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
// import org.springframework.amqp.support.converter.MessageConverter;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
//
// /**
//  * RabbitMQ 配置
//  */
// @Configuration
// public class RabbitMQConfig {
//
//     @Value("${spring.rabbitmq.listener.simple.prefetch:10}")
//     private int prefetchCount;
//
//     // 队列名称常量
//     public static final String POST_CREATE_QUEUE = "post-create-queue";
//     public static final String POST_CREATE_EXCHANGE = "post-create-exchange";
//     public static final String POST_CREATE_ROUTING_KEY = "post-create-routing-key";
//
//     /**
//      * 创建动态队列
//      */
//     @Bean
//     public Queue postCreateQueue() {
//         return new Queue(POST_CREATE_QUEUE, true);
//     }
//
//     /**
//      * 创建动态交换机
//      */
//     @Bean
//     public DirectExchange postCreateExchange() {
//         return new DirectExchange(POST_CREATE_EXCHANGE, true, false);
//     }
//
//     /**
//      * 绑定队列到交换机
//      */
//     @Bean
//     public Binding postCreateBinding(Queue postCreateQueue, DirectExchange postCreateExchange) {
//         return BindingBuilder.bind(postCreateQueue).to(postCreateExchange).with(POST_CREATE_ROUTING_KEY);
//     }
//
//     /**
//      * 消息转换器（JSON 格式）
//      */
//     @Bean
//     public MessageConverter messageConverter() {
//         return new Jackson2JsonMessageConverter();
//     }
//
//     /**
//      * RabbitTemplate 配置
//      */
//     @Bean
//     public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
//         RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//         rabbitTemplate.setMessageConverter(messageConverter);
//         return rabbitTemplate;
//     }
// }

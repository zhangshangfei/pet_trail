// RabbitMQ 消费者已注释，改用 Spring Event + 定时任务兜底
// package com.pettrail.pettrailbackend.mq;
//
// import com.pettrail.pettrailbackend.config.RabbitMQConfig;
// import com.pettrail.pettrailbackend.dto.PostCreateMessage;
// import com.pettrail.pettrailbackend.service.ContentAuditService;
// import com.pettrail.pettrailbackend.service.PostService;
// import com.rabbitmq.client.Channel;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.amqp.core.Message;
// import org.springframework.amqp.rabbit.annotation.RabbitListener;
// import org.springframework.stereotype.Component;
//
// import java.io.IOException;
//
// /**
//  * 动态创建消息消费者
//  */
// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class PostCreateConsumer {
//
//     private final ContentAuditService contentAuditService;
//     private final PostService postService;
//
//     @RabbitListener(queues = RabbitMQConfig.POST_CREATE_QUEUE)
//     public void onMessage(PostCreateMessage message, Channel channel, Message amqpMessage) throws IOException {
//         long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
//
//         try {
//             log.info("收到动态创建消息：postId={}, userId={}", message.getPostId(), message.getUserId());
//
//             // 1. 内容审核
//             boolean auditPassed = contentAuditService.audit(message.getContent());
//             if (!auditPassed) {
//                 log.warn("动态内容审核不通过：postId={}", message.getPostId());
//                 // 拒绝消息，不重新入队
//                 channel.basicNack(deliveryTag, false, false);
//                 return;
//             }
//
//             // 2. 图片审核（如果有）
//             if (message.getImages() != null && !message.getImages().isEmpty()) {
//                 for (String imageUrl : message.getImages()) {
//                     boolean imageAuditPassed = contentAuditService.auditImage(imageUrl);
//                     if (!imageAuditPassed) {
//                         log.warn("动态图片审核不通过：postId={}, imageUrl={}",
//                             message.getPostId(), imageUrl);
//                         // 拒绝消息，不重新入队
//                         channel.basicNack(deliveryTag, false, false);
//                         return;
//                     }
//                 }
//             }
//
//             // 3. 更新动态状态为正常（如果之前是审核中）
//             // postService.updateStatus(message.getPostId(), 1);
//
//             // 4. 推送给粉丝（后续实现）
//             // notifyFansService.notify(message.getUserId(), message.getPostId());
//
//             log.info("动态处理完成：postId={}", message.getPostId());
//
//             // 手动确认消息
//             channel.basicAck(deliveryTag, false);
//
//         } catch (Exception e) {
//             log.error("处理动态消息失败：postId={}", message.getPostId(), e);
//             // 发生异常，拒绝消息并重新入队（可配置最大重试次数）
//             channel.basicNack(deliveryTag, false, true);
//         }
//     }
// }

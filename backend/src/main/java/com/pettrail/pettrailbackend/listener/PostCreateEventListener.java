package com.pettrail.pettrailbackend.listener;

import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.event.PostCreateEvent;
import com.pettrail.pettrailbackend.service.ContentAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 动态创建事件监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostCreateEventListener {

    private final ContentAuditService contentAuditService;

    /**
     * 处理动态创建事件
     * 使用 @Async 实现异步处理，模拟消息队列的效果
     */
    @Async
    @EventListener
    public void handlePostCreateEvent(PostCreateEvent event) {
        Post post = event.getPost();
        try {
            log.info("收到动态创建事件：postId={}, userId={}", post.getId(), post.getUserId());

            // 1. 内容审核
            boolean auditPassed = contentAuditService.auditText(post.getContent());
            if (!auditPassed) {
                log.warn("动态内容审核不通过：postId={}", post.getId());
                // TODO: 更新动态状态为审核不通过
                return;
            }

            // 2. 图片审核（如果有）
            if (post.getImages() != null && !post.getImages().isEmpty()) {
                // 解析图片列表
                java.util.List<String> imageList = com.alibaba.fastjson.JSON.parseArray(post.getImages(), String.class);
                for (String imageUrl : imageList) {
                    boolean imageAuditPassed = contentAuditService.auditImage(imageUrl);
                    if (!imageAuditPassed) {
                        log.warn("动态图片审核不通过：postId={}, imageUrl={}",
                            post.getId(), imageUrl);
                        // TODO: 更新动态状态为审核不通过
                        return;
                    }
                }
            }

            // 3. 审核通过，更新状态为正常
            // postService.updateStatus(post.getId(), 1);

            // 4. 推送给粉丝（后续实现）
            // notifyFansService.notify(post.getUserId(), post.getId());

            log.info("动态处理完成：postId={}", post.getId());

        } catch (Exception e) {
            log.error("处理动态事件失败：postId={}", post.getId(), e);
        }
    }
}

package com.pettrail.pettrailbackend.task;

import com.pettrail.pettrailbackend.entity.Post;
import com.pettrail.pettrailbackend.mapper.PostMapper;
import com.pettrail.pettrailbackend.service.ContentAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态审核定时任务
 * 每 6 小时执行一次，兜底处理未审核的动态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostAuditScheduledTask {

    private final PostMapper postMapper;
    private final ContentAuditService contentAuditService;

    /**
     * 每 6 小时执行一次
     * cron 表达式：0 0 *\6 * * ? 表示每 6 小时的整点执行
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    public void auditPendingPosts() {
        log.info("开始执行定时动态审核任务");

        try {
            // 查询创建时间超过 1 小时但仍未审核的动态
            // 注意：这里假设 Post 实体有 createdAt 和 status 字段
            // status: 0-待审核，1-正常，2-审核不通过
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            List<Post> pendingPosts = postMapper.selectPendingForAudit(oneHourAgo);

            if (pendingPosts == null || pendingPosts.isEmpty()) {
                log.info("没有待审核的动态");
                return;
            }

            log.info("发现 {} 条待审核的动态", pendingPosts.size());

            int successCount = 0;
            int failCount = 0;

            for (Post post : pendingPosts) {
                try {
                    boolean auditPassed = processPostAudit(post);
                    if (auditPassed) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("处理动态审核失败：postId={}", post.getId(), e);
                    failCount++;
                }
            }

            log.info("定时动态审核任务完成：成功={}, 失败={}", successCount, failCount);

        } catch (Exception e) {
            log.error("定时动态审核任务执行异常", e);
        }
    }

    /**
     * 处理单条动态审核
     *
     * @return true-审核通过，false-审核不通过
     */
    private boolean processPostAudit(Post post) {
        log.info("开始审核动态：postId={}", post.getId());

        // 1. 内容审核
        boolean contentAuditPassed = contentAuditService.audit(post.getContent());
        if (!contentAuditPassed) {
            log.warn("动态内容审核不通过：postId={}", post.getId());
            // TODO: 更新状态为审核不通过
            // postMapper.updateStatus(post.getId(), 2);
            return false;
        }

        // 2. 图片审核（如果有）
        if (post.getImages() != null && !post.getImages().isEmpty()) {
            java.util.List<String> imageList = com.alibaba.fastjson.JSON.parseArray(post.getImages(), String.class);
            for (String imageUrl : imageList) {
                boolean imageAuditPassed = contentAuditService.auditImage(imageUrl);
                if (!imageAuditPassed) {
                    log.warn("动态图片审核不通过：postId={}, imageUrl={}",
                        post.getId(), imageUrl);
                    // TODO: 更新状态为审核不通过
                    // postMapper.updateStatus(post.getId(), 2);
                    return false;
                }
            }
        }

        // 3. 审核通过，更新状态为正常
        // postMapper.updateStatus(post.getId(), 1);
        log.info("动态审核通过：postId={}", post.getId());
        return true;
    }
}

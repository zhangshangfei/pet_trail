package com.pettrail.pettrailbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 内容审核服务
 */
@Slf4j
@Service
public class ContentAuditService {

    /**
     * 审核文本内容
     * 后续接入阿里云内容安全 API
     */
    public boolean audit(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        // 简单的敏感词过滤（后续替换为阿里云内容安全）
        String[] sensitiveWords = {"敏感词 1", "敏感词 2"};
        for (String word : sensitiveWords) {
            if (content.contains(word)) {
                log.warn("内容包含敏感词：{}", word);
                return false;
            }
        }

        return true;
    }

    /**
     * 审核图片
     * 后续接入阿里云内容安全 API
     */
    public boolean auditImage(String imageUrl) {
        // TODO: 接入阿里云内容安全图片审核
        log.debug("审核图片：{}", imageUrl);
        return true;
    }
}

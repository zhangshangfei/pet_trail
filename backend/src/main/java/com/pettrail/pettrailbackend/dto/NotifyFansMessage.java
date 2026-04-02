package com.pettrail.pettrailbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通知粉丝消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyFansMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 发布者 ID
     */
    private Long userId;

    /**
     * 动态 ID
     */
    private Long postId;

    /**
     * 通知类型
     */
    private String notifyType;
}

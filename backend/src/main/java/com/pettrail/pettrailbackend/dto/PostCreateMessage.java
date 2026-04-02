package com.pettrail.pettrailbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 动态创建消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 动态 ID
     */
    private Long postId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 宠物 ID
     */
    private Long petId;

    /**
     * 内容
     */
    private String content;

    /**
     * 图片列表
     */
    private List<String> images;

    /**
     * 创建时间
     */
    private Long createTime;
}

package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {

    private Long id;

    private Long postId;

    private Long userId;

    private Long parentId;

    private Long replyToId;

    private String content;

    private Integer likeCount;

    private LocalDateTime createdAt;

    private String userName;

    private String userAvatar;

    private String replyToUserName;

    private List<CommentVO> replies;
}

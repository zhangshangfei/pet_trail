package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class CommentCreateDTO {
    private String content;
    private Long parentId;
    private Long replyToId;
}

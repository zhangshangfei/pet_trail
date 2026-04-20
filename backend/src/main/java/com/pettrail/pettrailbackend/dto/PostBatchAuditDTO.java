package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostBatchAuditDTO {
    private List<Integer> postIds;
    private Integer auditStatus;
    private String auditRemark;
}

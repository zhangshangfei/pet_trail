package com.pettrail.pettrailbackend.dto;

import lombok.Data;

@Data
public class PostAuditDTO {
    private Integer auditStatus;
    private String auditRemark;
}

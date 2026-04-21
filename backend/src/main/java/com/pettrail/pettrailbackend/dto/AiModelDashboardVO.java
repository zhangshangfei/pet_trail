package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiModelDashboardVO {
    private AiModelVO currentModel;
    private List<AiModelVO> availableModels;
    private Long totalCalls;
    private Long successCalls;
    private Long failedCalls;
    private Double avgResponseTime;
    private List<AiModelSwitchLogVO> recentSwitches;
}

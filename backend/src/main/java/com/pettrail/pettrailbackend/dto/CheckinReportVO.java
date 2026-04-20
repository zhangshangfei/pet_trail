package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CheckinReportVO {
    private String period;
    private String startDate;
    private String endDate;
    private int totalDays;
    private int checkinDays;
    private double completionRate;
    private int currentStreak;
    private int maxStreak;
    private int totalCheckins;
    private List<Map<String, Object>> itemStats;
}

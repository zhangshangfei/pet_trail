package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class HealthAnalysisVO {

    private Integer score;
    private String level;
    private List<WarningItem> warnings;
    private List<String> suggestions;
    private Map<String, String> trends;
    private AnalysisDetail detail;
    private String aiAnalysis;

    @Data
    public static class WarningItem {
        private String type;
        private String message;
        private String severity;

        public WarningItem() {
        }

        public WarningItem(String type, String message, String severity) {
            this.type = type;
            this.message = message;
            this.severity = severity;
        }
    }

    @Data
    public static class AnalysisDetail {
        private Double vaccineScore;
        private Double parasiteScore;
        private Double weightScore;
        private Double checkinScore;
        private WeightAnalysis weightAnalysis;
        private VaccineAnalysis vaccineAnalysis;
    }

    @Data
    public static class WeightAnalysis {
        private Double currentWeight;
        private Double avgWeight30d;
        private Double weightChangeRate;
        private String trend;
    }

    @Data
    public static class VaccineAnalysis {
        private Integer total;
        private Integer completed;
        private Integer overdue;
        private String nextVaccineName;
        private String nextVaccineDate;
    }
}

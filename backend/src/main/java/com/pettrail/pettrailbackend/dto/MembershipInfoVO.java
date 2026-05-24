package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MembershipInfoVO {

    private Boolean isPro;
    private String plan;
    private LocalDateTime expireDate;
    private PlansVO plans;
    private List<FeatureVO> features;

    @Data
    public static class PlansVO {
        private PlanDetailVO monthly;
        private PlanDetailVO yearly;
    }

    @Data
    public static class PlanDetailVO {
        private BigDecimal price;
        private String label;
        private String priceLabel;
        private String save;
    }

    @Data
    public static class FeatureVO {
        private String icon;
        private String title;
        private String desc;
    }
}

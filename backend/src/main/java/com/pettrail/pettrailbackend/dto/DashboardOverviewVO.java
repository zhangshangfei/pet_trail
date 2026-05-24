package com.pettrail.pettrailbackend.dto;

import lombok.Data;


@Data
public class DashboardOverviewVO {

    private Integer petCount;
    private Integer upcomingVaccineCount;
    private Integer upcomingParasiteCount;
    private Integer weightRecordCount;
}

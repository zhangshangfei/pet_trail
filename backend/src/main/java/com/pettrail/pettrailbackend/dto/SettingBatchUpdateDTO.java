package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SettingBatchUpdateDTO {
    private List<Map<String, String>> settings;
}

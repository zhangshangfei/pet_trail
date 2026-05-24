package com.pettrail.pettrailbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class CacheStatsVO {

    private long activeEntries;
    private long voCacheCount;
    private long aiCacheCount;
    private long cacheCount;
    private String hitRate;
    private long hitCount;
    private long missCount;
    private long putCount;
    private long evictCount;
    private long totalAccess;
    private long ttlMinutes;
    private long ttlHours;
    private String cachePrefix;
    private String aiCachePrefix;
    private List<String> sampleKeys;
    private String error;
    private Long currentModelId;
    private String currentModelName;
    private Integer modelCount;
    private String lastUpdated;
}

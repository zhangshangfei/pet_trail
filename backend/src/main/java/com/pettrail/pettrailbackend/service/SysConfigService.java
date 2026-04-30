package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.SysConfig;
import com.pettrail.pettrailbackend.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    private volatile long lastRefreshTime = 0;
    private static final long CACHE_TTL_MS = 60_000;

    public String getValue(String key) {
        refreshCacheIfNeeded();
        return configCache.get(key);
    }

    public boolean getBoolean(String key) {
        String value = getValue(key);
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getValue(key);
        if (value == null) return defaultValue;
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }

    public Integer getInt(String key, Integer defaultValue) {
        String value = getValue(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public SysConfig getById(Long id) {
        return sysConfigMapper.selectById(id);
    }

    public List<SysConfig> listByCategory(String category) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(SysConfig::getCategory, category);
        }
        wrapper.orderByAsc(SysConfig::getSortOrder).orderByAsc(SysConfig::getId);
        return sysConfigMapper.selectList(wrapper);
    }

    public List<SysConfig> listAll() {
        return listByCategory(null);
    }

    public List<String> listCategories() {
        List<SysConfig> all = sysConfigMapper.selectList(null);
        return all.stream()
                .map(SysConfig::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public SysConfig create(SysConfig config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        sysConfigMapper.insert(config);
        invalidateCache();
        log.info("创建系统配置: key={}, value={}", config.getConfigKey(), config.getConfigValue());
        return config;
    }

    public SysConfig update(Long id, SysConfig config) {
        SysConfig existing = sysConfigMapper.selectById(id);
        if (existing == null) {
            return null;
        }
        if (config.getConfigName() != null) existing.setConfigName(config.getConfigName());
        if (config.getConfigValue() != null) existing.setConfigValue(config.getConfigValue());
        if (config.getConfigDesc() != null) existing.setConfigDesc(config.getConfigDesc());
        if (config.getCategory() != null) existing.setCategory(config.getCategory());
        if (config.getSortOrder() != null) existing.setSortOrder(config.getSortOrder());
        existing.setUpdatedAt(LocalDateTime.now());
        sysConfigMapper.updateById(existing);
        invalidateCache();
        log.info("更新系统配置: key={}, newValue={}", existing.getConfigKey(), existing.getConfigValue());
        return existing;
    }

    public boolean delete(Long id) {
        SysConfig existing = sysConfigMapper.selectById(id);
        if (existing == null) return false;
        sysConfigMapper.deleteById(id);
        invalidateCache();
        log.info("删除系统配置: key={}", existing.getConfigKey());
        return true;
    }

    public void setValue(String key, String value) {
        SysConfig config = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (config != null) {
            config.setConfigValue(value);
            config.setUpdatedAt(LocalDateTime.now());
            sysConfigMapper.updateById(config);
        } else {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigName(key);
            config.setCategory("system");
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            sysConfigMapper.insert(config);
        }
        invalidateCache();
    }

    private synchronized void refreshCacheIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastRefreshTime < CACHE_TTL_MS && !configCache.isEmpty()) {
            return;
        }
        List<SysConfig> all = sysConfigMapper.selectList(null);
        configCache.clear();
        for (SysConfig config : all) {
            configCache.put(config.getConfigKey(), config.getConfigValue());
        }
        lastRefreshTime = now;
    }

    public void invalidateCache() {
        configCache.clear();
        lastRefreshTime = 0;
    }
}

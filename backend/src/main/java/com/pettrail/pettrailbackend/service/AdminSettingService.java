package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.SystemSetting;
import com.pettrail.pettrailbackend.mapper.SystemSettingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSettingService {

    private final SystemSettingMapper settingMapper;

    public List<SystemSetting> listAll() {
        return settingMapper.selectList(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<Map<String, String>> settings) {
        if (settings != null) {
            for (Map<String, String> item : settings) {
                String key = item.get("settingKey");
                String value = item.get("settingValue");
                if (key != null) {
                    upsertSetting(key, value);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(String key, String value) {
        upsertSetting(key, value);
    }

    private void upsertSetting(String key, String value) {
        SystemSetting existing = settingMapper.selectOne(
                new LambdaQueryWrapper<SystemSetting>().eq(SystemSetting::getSettingKey, key));
        if (existing != null) {
            existing.setSettingValue(value);
            existing.setUpdatedAt(LocalDateTime.now());
            settingMapper.updateById(existing);
        } else {
            SystemSetting newSetting = new SystemSetting();
            newSetting.setSettingKey(key);
            newSetting.setSettingValue(value);
            settingMapper.insert(newSetting);
        }
    }
}

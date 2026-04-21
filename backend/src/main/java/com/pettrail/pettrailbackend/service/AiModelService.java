package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pettrail.pettrailbackend.dto.*;
import com.pettrail.pettrailbackend.entity.AiModel;
import com.pettrail.pettrailbackend.entity.AiModelSwitchLog;
import com.pettrail.pettrailbackend.mapper.AiModelMapper;
import com.pettrail.pettrailbackend.mapper.AiModelSwitchLogMapper;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelService {

    private final AiModelMapper aiModelMapper;
    private final AiModelSwitchLogMapper switchLogMapper;
    private final ObjectMapper objectMapper;
    private final HealthAnalysisCacheService cacheService;

    private final AtomicReference<AiModel> currentActiveModel = new AtomicReference<>();
    private final Map<Long, ModelStats> modelStatsMap = new ConcurrentHashMap<>();

    private static final long SWITCH_TIMEOUT_MS = 2000;

    public AiModel getCurrentModel() {
        AiModel model = currentActiveModel.get();
        if (model != null && model.getStatus() == 1) {
            return model;
        }
        synchronized (this) {
            model = currentActiveModel.get();
            if (model != null && model.getStatus() == 1) {
                return model;
            }
            model = loadActiveModel();
            if (model != null) {
                currentActiveModel.set(model);
            }
        }
        return model;
    }

    private AiModel loadActiveModel() {
        AiModel model = aiModelMapper.selectOne(
                new LambdaQueryWrapper<AiModel>()
                        .eq(AiModel::getStatus, 1)
                        .eq(AiModel::getIsDefault, 1)
                        .orderByAsc(AiModel::getSortOrder)
                        .last("LIMIT 1")
        );
        if (model == null) {
            model = aiModelMapper.selectOne(
                    new LambdaQueryWrapper<AiModel>()
                            .eq(AiModel::getStatus, 1)
                            .orderByAsc(AiModel::getSortOrder)
                            .last("LIMIT 1")
            );
        }
        return model;
    }

    @Transactional
    public AiModelVO switchModel(Long targetModelId, String reason) {
        long startTime = System.currentTimeMillis();
        AiModel fromModel = getCurrentModel();
        AiModel toModel = aiModelMapper.selectById(targetModelId);

        if (toModel == null) {
            throw new RuntimeException("目标模型不存在");
        }
        if (toModel.getStatus() != 1) {
            throw new RuntimeException("目标模型未启用");
        }

        AiModelSwitchLog switchLog = new AiModelSwitchLog();
        switchLog.setFromModelId(fromModel != null ? fromModel.getId() : null);
        switchLog.setFromModelName(fromModel != null ? fromModel.getDisplayName() : null);
        switchLog.setToModelId(toModel.getId());
        switchLog.setToModelName(toModel.getDisplayName());
        switchLog.setSwitchType("manual");
        switchLog.setOperatorId(UserContext.getCurrentUserId());
        switchLog.setOperatorName(UserContext.getCurrentUsername());
        switchLog.setReason(reason);
        switchLog.setCreatedAt(LocalDateTime.now());

        try {
            aiModelMapper.update(null, new LambdaUpdateWrapper<AiModel>()
                    .set(AiModel::getIsDefault, 0)
                    .eq(AiModel::getIsDefault, 1));

            toModel.setIsDefault(1);
            toModel.setUpdatedAt(LocalDateTime.now());
            aiModelMapper.updateById(toModel);

            currentActiveModel.set(toModel);

            cacheService.invalidateAll();
            log.info("[模型切换] 已清除所有健康分析缓存");

            long duration = System.currentTimeMillis() - startTime;
            switchLog.setStatus("success");
            switchLog.setDuration(duration);
            switchLogMapper.insert(switchLog);

            log.info("[模型切换] 成功切换模型: {} -> {}, 耗时: {}ms",
                    fromModel != null ? fromModel.getDisplayName() : "无",
                    toModel.getDisplayName(), duration);

            return convertToVO(toModel);
        } catch (Exception e) {
            switchLog.setStatus("failed");
            switchLog.setErrorMessage(e.getMessage());
            switchLogMapper.insert(switchLog);

            log.error("[模型切换] 切换失败: {}", e.getMessage());
            throw new RuntimeException("模型切换失败: " + e.getMessage());
        }
    }

    public List<AiModelVO> listAllModels() {
        List<AiModel> models = aiModelMapper.selectList(
                new LambdaQueryWrapper<AiModel>()
                        .orderByAsc(AiModel::getSortOrder)
                        .orderByDesc(AiModel::getId)
        );
        AiModel current = getCurrentModel();
        Long currentId = current != null ? current.getId() : null;

        return models.stream().map(m -> {
            AiModelVO vo = convertToVO(m);
            vo.setIsActive(m.getId().equals(currentId));
            ModelStats stats = modelStatsMap.get(m.getId());
            if (stats != null) {
                vo.setCallCount(stats.callCount);
                vo.setSuccessCount(stats.successCount);
                vo.setFailCount(stats.failCount);
                vo.setAvgResponseTime(stats.getAvgResponseTime());
            }
            return vo;
        }).toList();
    }

    public AiModelVO getModelById(Long id) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) return null;
        AiModel current = getCurrentModel();
        AiModelVO vo = convertToVO(model);
        vo.setIsActive(model.getId().equals(current != null ? current.getId() : null));
        return vo;
    }

    @Transactional
    public AiModelVO createModel(AiModelCreateDTO dto) {
        AiModel model = new AiModel();
        model.setModelName(dto.getModelName());
        model.setDisplayName(dto.getDisplayName());
        model.setProvider(dto.getProvider());
        model.setBaseUrl(dto.getBaseUrl());
        model.setApiKey(dto.getApiKey());
        model.setModelVersion(dto.getModelVersion());
        model.setParameters(dto.getParameters());
        model.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        model.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        model.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        model.setDescription(dto.getDescription());
        model.setIcon(dto.getIcon());
        model.setCreatedAt(LocalDateTime.now());
        model.setUpdatedAt(LocalDateTime.now());

        aiModelMapper.insert(model);
        log.info("[模型管理] 创建新模型: id={}, name={}", model.getId(), model.getDisplayName());

        return convertToVO(model);
    }

    @Transactional
    public AiModelVO updateModel(Long id, AiModelUpdateDTO dto) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) {
            throw new RuntimeException("模型不存在");
        }

        if (dto.getDisplayName() != null) model.setDisplayName(dto.getDisplayName());
        if (dto.getProvider() != null) model.setProvider(dto.getProvider());
        if (dto.getBaseUrl() != null) model.setBaseUrl(dto.getBaseUrl());
        if (dto.getApiKey() != null) model.setApiKey(dto.getApiKey());
        if (dto.getModelVersion() != null) model.setModelVersion(dto.getModelVersion());
        if (dto.getParameters() != null) model.setParameters(dto.getParameters());
        if (dto.getStatus() != null) model.setStatus(dto.getStatus());
        if (dto.getIsDefault() != null) model.setIsDefault(dto.getIsDefault());
        if (dto.getSortOrder() != null) model.setSortOrder(dto.getSortOrder());
        if (dto.getDescription() != null) model.setDescription(dto.getDescription());
        if (dto.getIcon() != null) model.setIcon(dto.getIcon());
        model.setUpdatedAt(LocalDateTime.now());

        aiModelMapper.updateById(model);

        if (model.getIsDefault() == 1 || model.getId().equals(currentActiveModel.get() != null ? currentActiveModel.get().getId() : null)) {
            currentActiveModel.set(model);
        }

        log.info("[模型管理] 更新模型: id={}, name={}", model.getId(), model.getDisplayName());
        return convertToVO(model);
    }

    @Transactional
    public boolean deleteModel(Long id) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) return false;

        if (model.getIsDefault() == 1) {
            throw new RuntimeException("无法删除默认模型，请先切换到其他模型");
        }

        aiModelMapper.deleteById(id);
        modelStatsMap.remove(id);
        log.info("[模型管理] 删除模型: id={}, name={}", id, model.getDisplayName());
        return true;
    }

    @Transactional
    public AiModelVO setModelStatus(Long id, Integer status) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) {
            throw new RuntimeException("模型不存在");
        }

        if (status == 0 && model.getIsDefault() == 1) {
            throw new RuntimeException("无法禁用默认模型，请先切换到其他模型");
        }

        model.setStatus(status);
        model.setUpdatedAt(LocalDateTime.now());
        aiModelMapper.updateById(model);

        if (status == 0 && model.getId().equals(currentActiveModel.get() != null ? currentActiveModel.get().getId() : null)) {
            currentActiveModel.set(loadActiveModel());
        }

        log.info("[模型管理] 更新模型状态: id={}, status={}", id, status);
        return convertToVO(model);
    }

    public List<AiModelSwitchLogVO> getSwitchLogs(int limit) {
        List<AiModelSwitchLog> logs = switchLogMapper.selectList(
                new LambdaQueryWrapper<AiModelSwitchLog>()
                        .orderByDesc(AiModelSwitchLog::getCreatedAt)
                        .last("LIMIT " + limit)
        );
        return logs.stream().map(this::convertSwitchLogToVO).toList();
    }

    public AiModelDashboardVO getDashboard() {
        AiModelDashboardVO dashboard = new AiModelDashboardVO();

        AiModel current = getCurrentModel();
        if (current != null) {
            AiModelVO currentVO = convertToVO(current);
            currentVO.setIsActive(true);
            ModelStats stats = modelStatsMap.get(current.getId());
            if (stats != null) {
                currentVO.setCallCount(stats.callCount);
                currentVO.setSuccessCount(stats.successCount);
                currentVO.setFailCount(stats.failCount);
                currentVO.setAvgResponseTime(stats.getAvgResponseTime());
            }
            dashboard.setCurrentModel(currentVO);
        }

        dashboard.setAvailableModels(listAllModels());

        long totalCalls = modelStatsMap.values().stream().mapToLong(s -> s.callCount).sum();
        long successCalls = modelStatsMap.values().stream().mapToLong(s -> s.successCount).sum();
        long failedCalls = modelStatsMap.values().stream().mapToLong(s -> s.failCount).sum();
        double avgTime = modelStatsMap.values().stream()
                .filter(s -> s.callCount > 0)
                .mapToDouble(ModelStats::getAvgResponseTime)
                .average().orElse(0);

        dashboard.setTotalCalls(totalCalls);
        dashboard.setSuccessCalls(successCalls);
        dashboard.setFailedCalls(failedCalls);
        dashboard.setAvgResponseTime(avgTime);
        dashboard.setRecentSwitches(getSwitchLogs(10));

        return dashboard;
    }

    public void recordModelCall(Long modelId, long responseTime, boolean success) {
        modelStatsMap.computeIfAbsent(modelId, k -> new ModelStats())
                .record(responseTime, success);
    }

    public Map<String, Object> getModelParameters(Long modelId) {
        AiModel model = aiModelMapper.selectById(modelId);
        if (model == null || model.getParameters() == null) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(model.getParameters(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析模型参数失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @Transactional
    public void updateModelParameters(Long modelId, Map<String, Object> parameters) {
        AiModel model = aiModelMapper.selectById(modelId);
        if (model == null) {
            throw new RuntimeException("模型不存在");
        }
        try {
            model.setParameters(objectMapper.writeValueAsString(parameters));
            model.setUpdatedAt(LocalDateTime.now());
            aiModelMapper.updateById(model);
            log.info("[模型管理] 更新模型参数: id={}", modelId);
        } catch (Exception e) {
            throw new RuntimeException("参数序列化失败: " + e.getMessage());
        }
    }

    public void refreshCurrentModel() {
        synchronized (this) {
            currentActiveModel.set(loadActiveModel());
        }
        log.info("[模型管理] 刷新当前活动模型缓存");
    }

    private AiModelVO convertToVO(AiModel model) {
        AiModelVO vo = new AiModelVO();
        vo.setId(model.getId());
        vo.setModelName(model.getModelName());
        vo.setDisplayName(model.getDisplayName());
        vo.setProvider(model.getProvider());
        vo.setBaseUrl(model.getBaseUrl());
        vo.setApiKey(maskApiKey(model.getApiKey()));
        vo.setModelVersion(model.getModelVersion());
        vo.setParameters(model.getParameters());
        vo.setStatus(model.getStatus());
        vo.setIsDefault(model.getIsDefault());
        vo.setSortOrder(model.getSortOrder());
        vo.setDescription(model.getDescription());
        vo.setIcon(model.getIcon());
        vo.setCreatedAt(model.getCreatedAt());
        vo.setUpdatedAt(model.getUpdatedAt());
        return vo;
    }

    private AiModelSwitchLogVO convertSwitchLogToVO(AiModelSwitchLog log) {
        AiModelSwitchLogVO vo = new AiModelSwitchLogVO();
        vo.setId(log.getId());
        vo.setFromModelId(log.getFromModelId());
        vo.setFromModelName(log.getFromModelName());
        vo.setToModelId(log.getToModelId());
        vo.setToModelName(log.getToModelName());
        vo.setSwitchType(log.getSwitchType());
        vo.setOperatorId(log.getOperatorId());
        vo.setOperatorName(log.getOperatorName());
        vo.setReason(log.getReason());
        vo.setStatus(log.getStatus());
        vo.setDuration(log.getDuration());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) return "****";
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }

    private static class ModelStats {
        long callCount = 0;
        long successCount = 0;
        long failCount = 0;
        long totalResponseTime = 0;

        synchronized void record(long responseTime, boolean success) {
            callCount++;
            totalResponseTime += responseTime;
            if (success) successCount++;
            else failCount++;
        }

        double getAvgResponseTime() {
            return callCount > 0 ? (double) totalResponseTime / callCount : 0;
        }
    }
}

package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pettrail.pettrailbackend.dto.*;
import com.pettrail.pettrailbackend.entity.AiModel;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.entity.AiModelStats;
import com.pettrail.pettrailbackend.entity.AiModelSwitchLog;
import com.pettrail.pettrailbackend.mapper.AiModelMapper;
import com.pettrail.pettrailbackend.mapper.AiModelStatsMapper;
import com.pettrail.pettrailbackend.mapper.AiModelSwitchLogMapper;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final AiModelStatsMapper statsMapper;
    private final ObjectMapper objectMapper;
    private final HealthAnalysisCacheService cacheService;

    private final AtomicReference<AiModel> currentActiveModel = new AtomicReference<>();
    private final Map<Long, InMemoryStats> pendingStatsMap = new ConcurrentHashMap<>();

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
            throw new BusinessException(404, "目标模型不存在");
        }
        if (toModel.getStatus() != 1) {
            throw new BusinessException("目标模型未启用");
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

            flushPendingStats();
            cacheService.invalidateAll();

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
            throw new BusinessException("模型切换失败: " + e.getMessage());
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
            enrichWithStats(vo, m.getId());
            return vo;
        }).toList();
    }

    public AiModelVO getModelById(Long id) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) return null;
        AiModel current = getCurrentModel();
        AiModelVO vo = convertToVO(model);
        vo.setIsActive(model.getId().equals(current != null ? current.getId() : null));
        enrichWithStats(vo, id);
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
            throw new BusinessException(404, "模型不存在");
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
        if (model == null) {
            throw new BusinessException(404, "模型不存在");
        }

        if (model.getIsDefault() == 1) {
            throw new BusinessException("无法删除默认模型，请先切换到其他模型");
        }

        aiModelMapper.deleteById(id);
        pendingStatsMap.remove(id);
        log.info("[模型管理] 删除模型: id={}, name={}", id, model.getDisplayName());
        return true;
    }

    @Transactional
    public AiModelVO setModelStatus(Long id, Integer status) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException(404, "模型不存在");
        }

        if (status == 0 && model.getIsDefault() == 1) {
            throw new BusinessException("无法禁用默认模型，请先切换到其他模型");
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
            enrichWithStats(currentVO, current.getId());
            dashboard.setCurrentModel(currentVO);
        }

        dashboard.setAvailableModels(listAllModels());

        Map<String, Object> globalStats = statsMapper.selectGlobalAggregatedStats();
        if (globalStats != null) {
            dashboard.setTotalCalls(toLong(globalStats.get("total_calls")));
            dashboard.setSuccessCalls(toLong(globalStats.get("success_calls")));
            dashboard.setFailedCalls(toLong(globalStats.get("failed_calls")));
            dashboard.setAvgResponseTime(toDouble(globalStats.get("avg_response_time")));
        }

        dashboard.setRecentSwitches(getSwitchLogs(10));

        return dashboard;
    }

    public void recordModelCall(Long modelId, long responseTime, boolean success) {
        pendingStatsMap.computeIfAbsent(modelId, k -> new InMemoryStats())
                .record(responseTime, success);
    }

    public Map<String, Object> getModelStats(Long modelId) {
        Map<String, Object> dbStats = statsMapper.selectAggregatedStats(modelId);
        InMemoryStats pending = pendingStatsMap.get(modelId);
        if (pending == null || pending.callCount == 0) {
            return dbStats != null ? dbStats : new HashMap<>();
        }

        long dbCalls = toLong(dbStats != null ? dbStats.get("total_calls") : 0);
        long dbSuccess = toLong(dbStats != null ? dbStats.get("success_calls") : 0);
        long dbFailed = toLong(dbStats != null ? dbStats.get("failed_calls") : 0);
        long dbTotalTime = 0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total_calls", dbCalls + pending.callCount);
        result.put("success_calls", dbSuccess + pending.successCount);
        result.put("failed_calls", dbFailed + pending.failCount);
        long totalCalls = dbCalls + pending.callCount;
        long totalSuccess = dbSuccess + pending.successCount;
        result.put("success_rate", totalCalls > 0 ? Math.round(totalSuccess * 1000.0 / totalCalls) / 10.0 : 0);
        result.put("avg_response_time", totalCalls > 0 ? Math.round((dbTotalTime + pending.totalResponseTime) * 1.0 / totalCalls) : 0);

        return result;
    }

    public List<Map<String, Object>> getModelDailyStats(Long modelId, int limit) {
        return statsMapper.selectDailyStats(modelId, limit);
    }

    @Scheduled(fixedRate = 300000)
    public void scheduledFlushStats() {
        flushPendingStats();
    }

    @Transactional
    public void flushPendingStats() {
        if (pendingStatsMap.isEmpty()) return;

        LocalDate today = LocalDate.now();
        List<Long> flushed = new ArrayList<>();

        for (Map.Entry<Long, InMemoryStats> entry : pendingStatsMap.entrySet()) {
            Long modelId = entry.getKey();
            InMemoryStats pending = entry.getValue();

            if (pending.callCount == 0) continue;

            synchronized (pending) {
                long callCount = pending.callCount;
                long successCount = pending.successCount;
                long failCount = pending.failCount;
                long totalResponseTime = pending.totalResponseTime;
                long minResponseTime = pending.minResponseTime;
                long maxResponseTime = pending.maxResponseTime;

                pending.callCount = 0;
                pending.successCount = 0;
                pending.failCount = 0;
                pending.totalResponseTime = 0;
                pending.minResponseTime = Long.MAX_VALUE;
                pending.maxResponseTime = 0;

                AiModelStats existing = statsMapper.selectOne(
                        new LambdaQueryWrapper<AiModelStats>()
                                .eq(AiModelStats::getModelId, modelId)
                                .eq(AiModelStats::getStatsDate, today)
                );

                if (existing != null) {
                    existing.setCallCount(existing.getCallCount() + callCount);
                    existing.setSuccessCount(existing.getSuccessCount() + successCount);
                    existing.setFailCount(existing.getFailCount() + failCount);
                    existing.setTotalResponseTime(existing.getTotalResponseTime() + totalResponseTime);
                    existing.setAvgResponseTime(existing.getCallCount() > 0
                            ? Math.round(existing.getTotalResponseTime() * 100.0 / existing.getCallCount()) / 100.0 : 0);
                    existing.setSuccessRate(existing.getCallCount() > 0
                            ? Math.round(existing.getSuccessCount() * 1000.0 / existing.getCallCount()) / 10.0 : 0);
                    if (minResponseTime < (existing.getMinResponseTime() != null ? existing.getMinResponseTime() : Long.MAX_VALUE)) {
                        existing.setMinResponseTime(minResponseTime);
                    }
                    if (maxResponseTime > (existing.getMaxResponseTime() != null ? existing.getMaxResponseTime() : 0)) {
                        existing.setMaxResponseTime(maxResponseTime);
                    }
                    existing.setUpdatedAt(LocalDateTime.now());
                    statsMapper.updateById(existing);
                } else {
                    AiModelStats newStats = new AiModelStats();
                    newStats.setModelId(modelId);
                    newStats.setStatsDate(today);
                    newStats.setCallCount(callCount);
                    newStats.setSuccessCount(successCount);
                    newStats.setFailCount(failCount);
                    newStats.setTotalResponseTime(totalResponseTime);
                    newStats.setAvgResponseTime(callCount > 0 ? Math.round(totalResponseTime * 100.0 / callCount) / 100.0 : 0);
                    newStats.setSuccessRate(callCount > 0 ? Math.round(successCount * 1000.0 / callCount) / 10.0 : 0);
                    newStats.setMinResponseTime(minResponseTime != Long.MAX_VALUE ? minResponseTime : null);
                    newStats.setMaxResponseTime(maxResponseTime > 0 ? maxResponseTime : null);
                    newStats.setCreatedAt(LocalDateTime.now());
                    newStats.setUpdatedAt(LocalDateTime.now());
                    statsMapper.insert(newStats);
                }

                flushed.add(modelId);
            }
        }

        if (!flushed.isEmpty()) {
            log.info("[模型统计] 持久化完成: 模型数={}, 模型IDs={}", flushed.size(), flushed);
        }
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
            throw new BusinessException(404, "模型不存在");
        }
        try {
            model.setParameters(objectMapper.writeValueAsString(parameters));
            model.setUpdatedAt(LocalDateTime.now());
            aiModelMapper.updateById(model);
            log.info("[模型管理] 更新模型参数: id={}", modelId);
        } catch (Exception e) {
            throw new BusinessException("参数序列化失败: " + e.getMessage());
        }
    }

    public void refreshCurrentModel() {
        synchronized (this) {
            currentActiveModel.set(loadActiveModel());
        }
        log.info("[模型管理] 刷新当前活动模型缓存");
    }

    private void enrichWithStats(AiModelVO vo, Long modelId) {
        Map<String, Object> stats = getModelStats(modelId);
        if (stats != null && !stats.isEmpty()) {
            vo.setCallCount(toLong(stats.get("total_calls")));
            vo.setSuccessCount(toLong(stats.get("success_calls")));
            vo.setFailCount(toLong(stats.get("failed_calls")));
            vo.setAvgResponseTime(toDouble(stats.get("avg_response_time")));
        }
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

    private long toLong(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return 0; }
    }

    private double toDouble(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0; }
    }

    private static class InMemoryStats {
        long callCount = 0;
        long successCount = 0;
        long failCount = 0;
        long totalResponseTime = 0;
        long minResponseTime = Long.MAX_VALUE;
        long maxResponseTime = 0;

        synchronized void record(long responseTime, boolean success) {
            callCount++;
            totalResponseTime += responseTime;
            if (success) successCount++;
            else failCount++;
            if (responseTime < minResponseTime) minResponseTime = responseTime;
            if (responseTime > maxResponseTime) maxResponseTime = responseTime;
        }
    }
}

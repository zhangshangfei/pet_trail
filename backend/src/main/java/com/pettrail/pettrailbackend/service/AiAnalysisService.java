package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.dto.HealthAnalysisVO;
import com.pettrail.pettrailbackend.dto.HealthAnalysisVO.*;
import com.pettrail.pettrailbackend.entity.AiModel;
import com.pettrail.pettrailbackend.entity.Pet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    private final RestTemplate restTemplate;
    private final SysConfigService sysConfigService;
    private final AiModelService aiModelService;
    private final HealthAnalysisCacheService cacheService;

    private final AtomicLong lastFailTime = new AtomicLong(0);
    private static final long CIRCUIT_BREAKER_RESET_MS = 60000;
    private static final int MAX_CONSECUTIVE_FAILURES = 3;
    private final AtomicLong consecutiveFailures = new AtomicLong(0);

    public String generateAnalysis(Pet pet, HealthAnalysisVO analysis) {
        if (!isAiEnabled()) {
            log.info("[AI调用] AI分析未启用，跳过大模型调用");
            return null;
        }

        if (isCircuitBreakerOpen()) {
            log.warn("[AI调用] 熔断器开启，跳过本次调用");
            return null;
        }

        String cachedAnalysis = cacheService.getCachedAnalysis(pet.getId(), false);
        if (cachedAnalysis != null) {
            log.info("[AI调用] 使用缓存结果: petId={}", pet.getId());
            return cachedAnalysis;
        }

        AiModel currentModel = aiModelService.getCurrentModel();
        String effectiveApiKey;
        String effectiveModel;
        String effectiveBaseUrl;

        if (currentModel != null) {
            effectiveApiKey = currentModel.getApiKey();
            effectiveModel = currentModel.getModelName();
            effectiveBaseUrl = currentModel.getBaseUrl();
            log.info("[AI调用] 使用模型管理系统配置 === 模型ID: {}, 名称: {}, provider: {}",
                    currentModel.getId(), currentModel.getDisplayName(), currentModel.getProvider());
        } else {
            effectiveApiKey = getEffectiveApiKey();
            effectiveModel = getEffectiveModel();
            effectiveBaseUrl = getEffectiveBaseUrl();
            log.info("[AI调用] 使用系统配置文件配置 === 模型: {}", effectiveModel);
        }

        if (effectiveApiKey == null || effectiveApiKey.isEmpty()) {
            log.warn("[AI调用] API Key未配置，跳过大模型调用");
            return null;
        }

        String url = effectiveBaseUrl + (effectiveBaseUrl.endsWith("/") ? "" : "/") + "chat/completions";

        log.info("[AI调用] 开始调用 === 宠物: {}, 模型: {}, URL: {}",
                pet.getName(), effectiveModel, url);

        long startTime = System.currentTimeMillis();
        Long modelId = currentModel != null ? currentModel.getId() : null;

        try {
            String prompt = buildPrompt(pet, analysis);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(effectiveApiKey);
            headers.set("HTTP-Referer", "https://pet-trail.app");
            headers.set("X-Title", "PetTrail");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", effectiveModel);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "你是专业宠物健康顾问，根据数据给出简洁分析建议，200字以内。"),
                    Map.of("role", "user", "content", prompt)
            ));

            if (currentModel != null && currentModel.getParameters() != null) {
                applyModelParameters(body, currentModel.getParameters());
            } else {
                body.put("temperature", 0.7);
                body.put("max_tokens", 300);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, request, (Class<Map<String, Object>>)(Class<?>)Map.class);

            long elapsed = System.currentTimeMillis() - startTime;
            boolean success = false;

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> bodyResp = response.getBody();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) bodyResp.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        String content = (String) message.get("content");
                        success = true;
                        log.info("[AI调用] 调用成功 === 耗时: {}ms, 状态码: {}, 内容长度: {}, 内容摘要: {}",
                                elapsed, response.getStatusCode(),
                                content != null ? content.length() : 0,
                                content != null && content.length() > 100 ? content.substring(0, 100) + "..." : content);
                        if (modelId != null) {
                            aiModelService.recordModelCall(modelId, elapsed, true);
                        }
                        consecutiveFailures.set(0);
                        cacheService.cacheAnalysis(pet.getId(), content, false);
                        return content;
                    }
                }
                log.warn("[AI调用] 调用异常 === 耗时: {}ms, 状态码: 200, 响应体结构异常, keys: {}", elapsed, response.getBody().keySet());
            } else {
                log.warn("[AI调用] 调用失败 === 耗时: {}ms, 状态码: {}, 响应体: {}", elapsed, response.getStatusCode(), response.getBody());
            }

            if (modelId != null) {
                aiModelService.recordModelCall(modelId, elapsed, success);
            }
            return null;
        } catch (HttpClientErrorException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[AI调用] 认证/权限错误 === 耗时: {}ms, 状态码: {}, 响应体: {}", elapsed, e.getStatusCode(), e.getResponseBodyAsString());
            if (modelId != null) aiModelService.recordModelCall(modelId, elapsed, false);
            recordFailure();
            return null;
        } catch (HttpServerErrorException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[AI调用] 服务端错误 === 耗时: {}ms, 状态码: {}, 响应体: {}", elapsed, e.getStatusCode(), e.getResponseBodyAsString());
            if (modelId != null) aiModelService.recordModelCall(modelId, elapsed, false);
            recordFailure();
            return null;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[AI调用] 调用异常 === 耗时: {}ms, 异常类型: {}, 信息: {}", elapsed, e.getClass().getSimpleName(), e.getMessage());
            if (modelId != null) aiModelService.recordModelCall(modelId, elapsed, false);
            recordFailure();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void applyModelParameters(Map<String, Object> body, String parametersJson) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> params = mapper.readValue(parametersJson, Map.class);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (!body.containsKey(entry.getKey())) {
                    body.put(entry.getKey(), entry.getValue());
                }
            }
            if (!body.containsKey("temperature")) body.put("temperature", 0.7);
            if (!body.containsKey("max_tokens")) body.put("max_tokens", 300);
        } catch (Exception e) {
            log.warn("[AI调用] 解析模型参数失败，使用默认参数: {}", e.getMessage());
            body.put("temperature", 0.7);
            body.put("max_tokens", 300);
        }
    }

    private boolean isAiEnabled() {
        try {
            String dbValue = sysConfigService.getValue("ai.enabled");
            if (dbValue != null) {
                return "true".equalsIgnoreCase(dbValue) || "1".equals(dbValue);
            }
        } catch (Exception e) {
            log.debug("读取数据库AI开关失败，使用配置文件默认值: {}", e.getMessage());
        }
        return false;
    }

    private String getEffectiveApiKey() {
        String dbKey = null;
        try {
            dbKey = sysConfigService.getValue("ai.api-key");
        } catch (Exception e) {
            log.debug("读取数据库AI API Key失败，使用配置文件默认值");
        }
        if (dbKey != null && !dbKey.isEmpty()) return dbKey;
        return "";
    }

    private String getEffectiveModel() {
        String dbModel = null;
        try {
            dbModel = sysConfigService.getValue("ai.model");
        } catch (Exception e) {
            log.debug("读取数据库AI模型失败，使用配置文件默认值");
        }
        if (dbModel != null && !dbModel.isEmpty()) return dbModel;
        return "openrouter/free";
    }

    private String getEffectiveBaseUrl() {
        String dbUrl = null;
        try {
            dbUrl = sysConfigService.getValue("ai.base-url");
        } catch (Exception e) {
            log.debug("读取数据库AI Base URL失败，使用配置文件默认值");
        }
        if (dbUrl != null && !dbUrl.isEmpty()) return dbUrl;
        return "https://openrouter.ai/api/v1";
    }

    private String buildPrompt(Pet pet, HealthAnalysisVO analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("宠物名称：%s，品种：%s，年龄：%s\n",
                pet.getName(),
                pet.getBreed() != null ? pet.getBreed() : "未知",
                pet.getBirthday() != null ? java.time.Period.between(pet.getBirthday(), java.time.LocalDate.now()).getYears() + "岁" : "未知"));
        sb.append(String.format("综合健康评分：%d分（%s）\n", analysis.getScore(), analysis.getLevel()));

        if (analysis.getDetail() != null) {
            AnalysisDetail d = analysis.getDetail();
            sb.append(String.format("疫苗评分：%.1f，驱虫评分：%.1f，体重评分：%.1f，打卡评分：%.1f\n",
                    d.getVaccineScore() != null ? d.getVaccineScore() : 0,
                    d.getParasiteScore() != null ? d.getParasiteScore() : 0,
                    d.getWeightScore() != null ? d.getWeightScore() : 0,
                    d.getCheckinScore() != null ? d.getCheckinScore() : 0));

            if (d.getWeightAnalysis() != null) {
                WeightAnalysis wa = d.getWeightAnalysis();
                sb.append(String.format("当前体重：%.1fkg，趋势：%s\n",
                        wa.getCurrentWeight() != null ? wa.getCurrentWeight() : 0,
                        wa.getTrend() != null ? wa.getTrend() : "未知"));
            }
        }

        if (analysis.getWarnings() != null && !analysis.getWarnings().isEmpty()) {
            sb.append("健康预警：\n");
            for (HealthAnalysisVO.WarningItem w : analysis.getWarnings()) {
                sb.append(String.format("- [%s] %s\n", w.getSeverity(), w.getMessage()));
            }
        }

        sb.append("\n请给出专业的健康分析和建议。");
        return sb.toString();
    }

    private boolean isCircuitBreakerOpen() {
        long failures = consecutiveFailures.get();
        if (failures >= MAX_CONSECUTIVE_FAILURES) {
            long lastFail = lastFailTime.get();
            long elapsed = System.currentTimeMillis() - lastFail;
            if (elapsed < CIRCUIT_BREAKER_RESET_MS) {
                return true;
            }
            consecutiveFailures.set(0);
            return false;
        }
        return false;
    }

    private void recordFailure() {
        long count = consecutiveFailures.incrementAndGet();
        lastFailTime.set(System.currentTimeMillis());
        if (count >= MAX_CONSECUTIVE_FAILURES) {
            log.warn("[AI调用] 熔断器触发，连续失败{}次，将在{}秒后重试", count, CIRCUIT_BREAKER_RESET_MS / 1000);
        }
    }
}

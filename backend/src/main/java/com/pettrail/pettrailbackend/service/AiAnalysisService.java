package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.dto.HealthAnalysisVO;
import com.pettrail.pettrailbackend.dto.HealthAnalysisVO.*;
import com.pettrail.pettrailbackend.entity.Pet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.base-url:https://openrouter.ai/api/v1}")
    private String baseUrl;

    @Value("${ai.model:openrouter/free}")
    private String model;

    @Value("${ai.enabled:false}")
    private boolean configEnabled;

    @Value("${ai.glm.enabled:true}")
    private boolean glmEnabled;

    @Value("${ai.glm.api-key:}")
    private String glmApiKey;

    @Value("${ai.glm.base-url:https://open.bigmodel.cn/api/paas/v4}")
    private String glmBaseUrl;

    @Value("${ai.glm.model:glm-4.7-flash}")
    private String glmModel;

    private final RestTemplate restTemplate;
    private final SysConfigService sysConfigService;

    private static final long VERIFY_CACHE_TTL_MS = 300_000;
    private final Map<String, Long> modelAvailableCache = new ConcurrentHashMap<>();
    private final Map<String, Boolean> modelAvailableStatus = new ConcurrentHashMap<>();

    public String generateAnalysis(Pet pet, HealthAnalysisVO analysis) {
        if (!isAiEnabled()) {
            log.info("[AI调用] AI分析未启用，跳过大模型调用");
            return null;
        }

        String prompt = buildPrompt(pet, analysis);

        if (isGlmEnabled()) {
            String glmResult = tryCallGlm(prompt, pet.getName());
            if (glmResult != null) {
                return glmResult;
            }
            log.warn("[AI调用] GLM模型调用失败，自动切换到OpenRouter备用模型");
        }

        return tryCallOpenRouter(prompt, pet.getName());
    }

    private boolean isGlmEnabled() {
        try {
            String dbValue = sysConfigService.getValue("ai.glm.enabled");
            if (dbValue != null) {
                return "true".equalsIgnoreCase(dbValue) || "1".equals(dbValue);
            }
        } catch (Exception e) {
            log.debug("读取数据库GLM开关失败，使用配置文件默认值: {}", e.getMessage());
        }
        return glmEnabled;
    }

    private String getGlmApiKey() {
        try {
            String dbKey = sysConfigService.getValue("ai.glm.api-key");
            if (dbKey != null && !dbKey.isEmpty()) return dbKey;
        } catch (Exception e) {
            log.debug("读取数据库GLM API Key失败");
        }
        return glmApiKey;
    }

    private String getGlmBaseUrl() {
        try {
            String dbUrl = sysConfigService.getValue("ai.glm.base-url");
            if (dbUrl != null && !dbUrl.isEmpty()) return dbUrl;
        } catch (Exception e) {
            log.debug("读取数据库GLM Base URL失败");
        }
        return glmBaseUrl;
    }

    private String getGlmModel() {
        try {
            String dbModel = sysConfigService.getValue("ai.glm.model");
            if (dbModel != null && !dbModel.isEmpty()) return dbModel;
        } catch (Exception e) {
            log.debug("读取数据库GLM模型失败");
        }
        return glmModel;
    }

    private boolean isModelAvailable(String modelKey) {
        Long lastVerify = modelAvailableCache.get(modelKey);
        if (lastVerify != null && System.currentTimeMillis() - lastVerify < VERIFY_CACHE_TTL_MS) {
            return modelAvailableStatus.getOrDefault(modelKey, true);
        }
        return true;
    }

    private void markModelUnavailable(String modelKey) {
        modelAvailableCache.put(modelKey, System.currentTimeMillis());
        modelAvailableStatus.put(modelKey, false);
        log.warn("[AI调用] 模型 {} 标记为不可用，缓存{}秒", modelKey, VERIFY_CACHE_TTL_MS / 1000);
    }

    private void markModelAvailable(String modelKey) {
        modelAvailableCache.put(modelKey, System.currentTimeMillis());
        modelAvailableStatus.put(modelKey, true);
    }

    private boolean verifyGlmAvailability() {
        String key = "glm";
        if (!isModelAvailable(key)) {
            log.info("[AI调用] GLM模型在缓存中被标记为不可用，跳过验证");
            return false;
        }

        String effectiveApiKey = getGlmApiKey();
        if (effectiveApiKey == null || effectiveApiKey.isEmpty()) {
            log.warn("[AI调用] GLM API Key未配置，跳过可用性验证");
            markModelUnavailable(key);
            return false;
        }

        String effectiveBaseUrl = getGlmBaseUrl();
        String effectiveModel = getGlmModel();
        String url = effectiveBaseUrl + (effectiveBaseUrl.endsWith("/") ? "" : "/") + "chat/completions";

        log.info("[AI调用] 验证GLM模型可用性 === 模型: {}, URL: {}", effectiveModel, url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(effectiveApiKey);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", effectiveModel);
            body.put("messages", List.of(
                    Map.of("role", "user", "content", "hi")
            ));
            body.put("max_tokens", 5);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[AI调用] GLM模型可用性验证通过 === 状态码: {}", response.getStatusCode());
                markModelAvailable(key);
                return true;
            } else {
                log.warn("[AI调用] GLM模型可用性验证失败 === 状态码: {}", response.getStatusCode());
                markModelUnavailable(key);
                return false;
            }
        } catch (HttpClientErrorException e) {
            log.warn("[AI调用] GLM模型可用性验证失败(认证错误) === 状态码: {}, 响应: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            markModelUnavailable(key);
            return false;
        } catch (ResourceAccessException e) {
            log.warn("[AI调用] GLM模型可用性验证失败(网络超时) === 异常: {}", e.getMessage());
            markModelUnavailable(key);
            return false;
        } catch (Exception e) {
            log.warn("[AI调用] GLM模型可用性验证失败(未知异常) === 类型: {}, 信息: {}",
                    e.getClass().getSimpleName(), e.getMessage());
            markModelUnavailable(key);
            return false;
        }
    }

    private String tryCallGlm(String prompt, String petName) {
        if (!verifyGlmAvailability()) {
            log.info("[AI调用] GLM模型不可用，跳过调用");
            return null;
        }

        String effectiveApiKey = getGlmApiKey();
        String effectiveBaseUrl = getGlmBaseUrl();
        String effectiveModel = getGlmModel();
        String url = effectiveBaseUrl + (effectiveBaseUrl.endsWith("/") ? "" : "/") + "chat/completions";

        log.info("[AI调用] 开始调用GLM === 宠物: {}, 模型: {}, URL: {}, apiKey前缀: {}",
                petName, effectiveModel, url,
                effectiveApiKey.length() > 8 ? effectiveApiKey.substring(0, 8) + "..." : "***");

        long startTime = System.currentTimeMillis();

        try {
            return doCallModel(url, effectiveModel, effectiveApiKey, prompt, "GLM", startTime);
        } catch (HttpClientErrorException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[AI调用] GLM认证/权限错误 === 耗时: {}ms, 状态码: {}, 响应体: {}",
                    elapsed, e.getStatusCode(), e.getResponseBodyAsString());
            markModelUnavailable("glm");
            return null;
        } catch (HttpServerErrorException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[AI调用] GLM服务端错误 === 耗时: {}ms, 状态码: {}, 响应体: {}",
                    elapsed, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (ResourceAccessException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[AI调用] GLM网络超时 === 耗时: {}ms, 异常: {}", elapsed, e.getMessage());
            markModelUnavailable("glm");
            return null;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[AI调用] GLM调用异常 === 耗时: {}ms, 类型: {}, 信息: {}",
                    elapsed, e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    private String tryCallOpenRouter(String prompt, String petName) {
        String effectiveApiKey = getEffectiveApiKey();
        if (effectiveApiKey == null || effectiveApiKey.isEmpty()) {
            log.warn("[AI调用] OpenRouter API Key未配置，跳过调用");
            return null;
        }

        String effectiveModel = getEffectiveModel();
        String effectiveBaseUrl = getEffectiveBaseUrl();
        String url = effectiveBaseUrl + (effectiveBaseUrl.endsWith("/") ? "" : "/") + "chat/completions";

        log.info("[AI调用] 开始调用OpenRouter === 宠物: {}, 模型: {}, URL: {}, apiKey前缀: {}",
                petName, effectiveModel, url,
                effectiveApiKey.length() > 8 ? effectiveApiKey.substring(0, 8) + "..." : "***");

        long startTime = System.currentTimeMillis();

        try {
            return doCallModel(url, effectiveModel, effectiveApiKey, prompt, "OpenRouter", startTime);
        } catch (HttpClientErrorException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[AI调用] OpenRouter认证/权限错误 === 耗时: {}ms, 状态码: {}, 响应体: {}",
                    elapsed, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (HttpServerErrorException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[AI调用] OpenRouter服务端错误 === 耗时: {}ms, 状态码: {}, 响应体: {}",
                    elapsed, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (ResourceAccessException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[AI调用] OpenRouter网络超时 === 耗时: {}ms, 异常: {}", elapsed, e.getMessage());
            return null;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[AI调用] OpenRouter调用异常 === 耗时: {}ms, 类型: {}, 信息: {}",
                    elapsed, e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    private String doCallModel(String url, String model, String apiKey, String prompt,
                               String modelLabel, long startTime) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        if ("OpenRouter".equals(modelLabel)) {
            headers.set("HTTP-Referer", "https://pet-trail.app");
            headers.set("X-Title", "PetTrail");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", "你是专业宠物健康顾问，根据数据给出简洁分析建议，200字以内。"),
                Map.of("role", "user", "content", prompt)
        ));
        body.put("temperature", 0.7);
        body.put("max_tokens", 300);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, request, (Class<Map<String, Object>>) (Class<?>) Map.class);

        long elapsed = System.currentTimeMillis() - startTime;

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> bodyResp = response.getBody();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) bodyResp.get("choices");
            if (choices != null && !choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null) {
                    String content = (String) message.get("content");
                    log.info("[AI调用] {}调用成功 === 耗时: {}ms, 模型: {}, 状态码: {}, 内容长度: {}, 内容摘要: {}",
                            modelLabel, elapsed, model, response.getStatusCode(),
                            content != null ? content.length() : 0,
                            content != null && content.length() > 100 ? content.substring(0, 100) + "..." : content);
                    return content;
                }
            }
            log.warn("[AI调用] {}调用异常 === 耗时: {}ms, 模型: {}, 状态码: 200, 响应体结构异常, keys: {}",
                    modelLabel, elapsed, model, response.getBody().keySet());
        } else {
            log.warn("[AI调用] {}调用失败 === 耗时: {}ms, 模型: {}, 状态码: {}, 响应体: {}",
                    modelLabel, elapsed, model, response.getStatusCode(), response.getBody());
        }
        return null;
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
        return configEnabled;
    }

    private String getEffectiveApiKey() {
        try {
            String dbKey = sysConfigService.getValue("ai.api-key");
            if (dbKey != null && !dbKey.isEmpty()) return dbKey;
        } catch (Exception e) {
            log.debug("读取数据库AI API Key失败");
        }
        return apiKey;
    }

    private String getEffectiveModel() {
        try {
            String dbModel = sysConfigService.getValue("ai.model");
            if (dbModel != null && !dbModel.isEmpty()) return dbModel;
        } catch (Exception e) {
            log.debug("读取数据库AI模型失败");
        }
        return model;
    }

    private String getEffectiveBaseUrl() {
        try {
            String dbUrl = sysConfigService.getValue("ai.base-url");
            if (dbUrl != null && !dbUrl.isEmpty()) return dbUrl;
        } catch (Exception e) {
            log.debug("读取数据库AI Base URL失败");
        }
        return baseUrl;
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
}

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
import org.springframework.web.client.RestTemplate;

import java.util.*;

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

    private final RestTemplate restTemplate;
    private final SysConfigService sysConfigService;

    public String generateAnalysis(Pet pet, HealthAnalysisVO analysis) {
        if (!isAiEnabled()) {
            log.info("[AI调用] AI分析未启用，跳过大模型调用");
            return null;
        }

        String effectiveApiKey = getEffectiveApiKey();
        if (effectiveApiKey == null || effectiveApiKey.isEmpty()) {
            log.warn("[AI调用] API Key未配置，跳过大模型调用。数据库和配置文件均未找到有效Key");
            return null;
        }

        String effectiveModel = getEffectiveModel();
        String effectiveBaseUrl = getEffectiveBaseUrl();
        String url = effectiveBaseUrl + (effectiveBaseUrl.endsWith("/") ? "" : "/") + "chat/completions";

        log.info("[AI调用] 开始调用 === 宠物: {}, 模型: {}, URL: {}, apiKey前缀: {}",
                pet.getName(), effectiveModel, url,
                effectiveApiKey.length() > 8 ? effectiveApiKey.substring(0, 8) + "..." : "***");

        long startTime = System.currentTimeMillis();

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
                    Map.of("role", "system", "content", "你是一位专业的宠物健康顾问，请根据提供的宠物健康数据给出专业、温暖的分析建议。回复控制在300字以内。"),
                    Map.of("role", "user", "content", prompt)
            ));
            body.put("temperature", 0.7);
            body.put("max_tokens", 500);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            long elapsed = System.currentTimeMillis() - startTime;

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map bodyResp = response.getBody();
                List<Map> choices = (List<Map>) bodyResp.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map message = (Map) choices.get(0).get("message");
                    if (message != null) {
                        String content = (String) message.get("content");
                        log.info("[AI调用] 调用成功 === 耗时: {}ms, 状态码: {}, 内容长度: {}, 内容摘要: {}",
                                elapsed, response.getStatusCode(),
                                content != null ? content.length() : 0,
                                content != null && content.length() > 100 ? content.substring(0, 100) + "..." : content);
                        return content;
                    }
                }
                log.warn("[AI调用] 调用异常 === 耗时: {}ms, 状态码: 200, 响应体结构异常, keys: {}", elapsed, response.getBody().keySet());
            } else {
                log.warn("[AI调用] 调用失败 === 耗时: {}ms, 状态码: {}, 响应体: {}", elapsed, response.getStatusCode(), response.getBody());
            }
            return null;
        } catch (HttpClientErrorException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[AI调用] 认证/权限错误 === 耗时: {}ms, 状态码: {}, 响应体: {}", elapsed, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (HttpServerErrorException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[AI调用] 服务端错误 === 耗时: {}ms, 状态码: {}, 响应体: {}", elapsed, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[AI调用] 调用异常 === 耗时: {}ms, 异常类型: {}, 信息: {}", elapsed, e.getClass().getSimpleName(), e.getMessage());
            return null;
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
        return configEnabled;
    }

    private String getEffectiveApiKey() {
        String dbKey = null;
        try {
            dbKey = sysConfigService.getValue("ai.api-key");
        } catch (Exception e) {
            log.debug("读取数据库AI API Key失败，使用配置文件默认值");
        }
        if (dbKey != null && !dbKey.isEmpty()) {
            log.debug("使用数据库配置的API Key, 长度={}", dbKey.length());
            return dbKey;
        }
        if (apiKey != null && !apiKey.isEmpty()) {
            log.debug("使用配置文件的API Key, 长度={}", apiKey.length());
            return apiKey;
        }
        log.warn("未找到有效的AI API Key（数据库和配置文件均为空）");
        return null;
    }

    private String getEffectiveModel() {
        String dbModel = null;
        try {
            dbModel = sysConfigService.getValue("ai.model");
        } catch (Exception e) {
            log.debug("读取数据库AI模型失败，使用配置文件默认值");
        }
        if (dbModel != null && !dbModel.isEmpty()) return dbModel;
        return model;
    }

    private String getEffectiveBaseUrl() {
        String dbUrl = null;
        try {
            dbUrl = sysConfigService.getValue("ai.base-url");
        } catch (Exception e) {
            log.debug("读取数据库AI Base URL失败，使用配置文件默认值");
        }
        if (dbUrl != null && !dbUrl.isEmpty()) return dbUrl;
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

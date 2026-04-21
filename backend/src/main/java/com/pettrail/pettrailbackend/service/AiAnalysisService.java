package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.dto.HealthAnalysisVO;
import com.pettrail.pettrailbackend.dto.HealthAnalysisVO.*;
import com.pettrail.pettrailbackend.entity.Pet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${ai.model:deepseek-chat}")
    private String model;

    @Value("${ai.enabled:false}")
    private boolean enabled;

    private final RestTemplate restTemplate;

    public String generateAnalysis(Pet pet, HealthAnalysisVO analysis) {
        if (!enabled || apiKey == null || apiKey.isEmpty()) {
            log.debug("AI分析未启用，跳过大模型调用");
            return null;
        }

        try {
            String prompt = buildPrompt(pet, analysis);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("HTTP-Referer", "https://pet-trail.app");
            headers.set("X-Title", "PetTrail");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "你是一位专业的宠物健康顾问，请根据提供的宠物健康数据给出专业、温暖的分析建议。回复控制在300字以内。"),
                    Map.of("role", "user", "content", prompt)
            ));
            body.put("temperature", 0.7);
            body.put("max_tokens", 500);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            String url = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "chat/completions";

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map bodyResp = response.getBody();
                List<Map> choices = (List<Map>) bodyResp.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map message = (Map) choices.get(0).get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
            }

            log.warn("AI分析API返回异常: status={}", response.getStatusCode());
            return null;
        } catch (Exception e) {
            log.warn("AI分析API调用失败: {}", e.getMessage());
            return null;
        }
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

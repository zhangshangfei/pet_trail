package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.HealthAnalysisVO;
import com.pettrail.pettrailbackend.dto.HealthAnalysisVO.*;
import com.pettrail.pettrailbackend.entity.*;
import com.pettrail.pettrailbackend.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthAnalysisService {

    private final PetMapper petMapper;
    private final WeightRecordMapper weightRecordMapper;
    private final VaccineReminderMapper vaccineReminderMapper;
    private final ParasiteReminderMapper parasiteReminderMapper;
    private final CheckinRecordMapper checkinRecordMapper;
    private final CheckinStatsMapper checkinStatsMapper;
    private final AiAnalysisService aiAnalysisService;
    private final HealthAnalysisCacheService cacheService;

    public HealthAnalysisVO analyze(Long userId, Long petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            return null;
        }

        String cacheKey = cacheService.buildCacheKey(userId, petId);
        HealthAnalysisVO cached = cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        long startTime = System.currentTimeMillis();
        log.info("[健康分析] 开始执行分析: userId={}, petId={}", userId, petId);

        HealthAnalysisVO vo = doAnalyze(pet);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("[健康分析] 分析完成: userId={}, petId={}, 耗时={}ms, 评分={}", userId, petId, elapsed, vo.getScore());

        cacheService.put(cacheKey, vo);
        return vo;
    }

    public void invalidateCache(Long userId, Long petId) {
        cacheService.invalidate(userId, petId);
    }

    public void invalidateCacheByPetId(Long petId) {
        cacheService.invalidateByPetId(petId);
    }

    public void invalidateAllCache() {
        cacheService.invalidateAll();
    }

    public Map<String, Object> getCacheStats() {
        return cacheService.getCacheStats();
    }

    private HealthAnalysisVO doAnalyze(Pet pet) {
        Long petId = pet.getId();
        Long userId = pet.getUserId();

        HealthAnalysisVO vo = new HealthAnalysisVO();
        List<WarningItem> warnings = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        Map<String, String> trends = new LinkedHashMap<>();
        AnalysisDetail detail = new AnalysisDetail();

        double vaccineScore = analyzeVaccine(petId, warnings, suggestions, trends, detail);
        double parasiteScore = analyzeParasite(petId, warnings, suggestions);
        double weightScore = analyzeWeight(petId, pet, warnings, suggestions, trends, detail);
        double checkinScore = analyzeCheckin(userId, warnings, suggestions, trends);

        detail.setVaccineScore(Math.round(vaccineScore * 10.0) / 10.0);
        detail.setParasiteScore(Math.round(parasiteScore * 10.0) / 10.0);
        detail.setWeightScore(Math.round(weightScore * 10.0) / 10.0);
        detail.setCheckinScore(Math.round(checkinScore * 10.0) / 10.0);

        double totalScore = vaccineScore * 0.30 + parasiteScore * 0.25
                + weightScore * 0.25 + checkinScore * 0.20;
        int finalScore = (int) Math.round(totalScore);
        finalScore = Math.max(0, Math.min(100, finalScore));

        vo.setScore(finalScore);
        vo.setLevel(getLevel(finalScore));
        vo.setWarnings(warnings);
        vo.setSuggestions(suggestions);
        vo.setTrends(trends);
        vo.setDetail(detail);

        try {
            String aiText = aiAnalysisService.generateAnalysis(pet, vo);
            vo.setAiAnalysis(aiText);
        } catch (Exception e) {
            log.warn("AI深度分析失败，使用规则引擎结果: {}", e.getMessage());
            vo.setAiAnalysis(generateRuleBasedSummary(pet, vo));
        }

        return vo;
    }

    private double analyzeVaccine(Long petId, List<WarningItem> warnings,
                                   List<String> suggestions, Map<String, String> trends,
                                   AnalysisDetail detail) {
        List<VaccineReminder> all = vaccineReminderMapper.selectList(
                new LambdaQueryWrapper<VaccineReminder>().eq(VaccineReminder::getPetId, petId));

        VaccineAnalysis va = new VaccineAnalysis();
        va.setTotal(all.size());

        if (all.isEmpty()) {
            warnings.add(new WarningItem("vaccine", "暂无疫苗记录，建议尽快添加", "medium"));
            suggestions.add("建议为宠物建立完整的疫苗接种计划");
            trends.put("vaccine", "无数据");
            va.setCompleted(0);
            va.setOverdue(0);
            detail.setVaccineAnalysis(va);
            return 60;
        }

        long completed = all.stream().filter(v -> v.getStatus() != null && v.getStatus() == 1).count();
        long overdue = all.stream().filter(v -> v.getStatus() != null && v.getStatus() != 1
                && v.getNextDate() != null && v.getNextDate().isBefore(LocalDate.now())).count();

        va.setCompleted((int) completed);
        va.setOverdue((int) overdue);

        Optional<VaccineReminder> nextVaccine = all.stream()
                .filter(v -> v.getStatus() == null || v.getStatus() != 1)
                .filter(v -> v.getNextDate() != null && !v.getNextDate().isBefore(LocalDate.now()))
                .min(Comparator.comparing(VaccineReminder::getNextDate));
        nextVaccine.ifPresent(v -> {
            va.setNextVaccineName(v.getVaccineName());
            va.setNextVaccineDate(v.getNextDate().toString());
        });

        detail.setVaccineAnalysis(va);

        if (overdue > 0) {
            warnings.add(new WarningItem("vaccine",
                    String.format("有%d项疫苗已逾期，请尽快接种", overdue), "high"));
            suggestions.add("逾期疫苗可能影响宠物免疫力，建议尽快补种");
        }

        if (nextVaccine.isPresent()) {
            long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), nextVaccine.get().getNextDate());
            if (daysUntil <= 7) {
                warnings.add(new WarningItem("vaccine",
                        String.format("下次疫苗(%s)将在%d天后到期", nextVaccine.get().getVaccineName(), daysUntil), "medium"));
            }
            suggestions.add(String.format("下次疫苗接种: %s (%s)", nextVaccine.get().getVaccineName(), nextVaccine.get().getNextDate()));
        }

        trends.put("vaccine", overdue > 0 ? "需关注" : completed == all.size() ? "已完成" : "进行中");

        double base = ((double) completed / all.size()) * 100;
        base -= overdue * 15;
        return Math.max(0, Math.min(100, base));
    }

    private double analyzeParasite(Long petId, List<WarningItem> warnings,
                                    List<String> suggestions) {
        List<ParasiteReminder> all = parasiteReminderMapper.selectList(
                new LambdaQueryWrapper<ParasiteReminder>().eq(ParasiteReminder::getPetId, petId));

        if (all.isEmpty()) {
            warnings.add(new WarningItem("parasite", "暂无驱虫记录", "low"));
            suggestions.add("建议定期为宠物进行体内外驱虫");
            return 70;
        }

        long overdue = all.stream().filter(p -> p.getStatus() != null && p.getStatus() != 1
                && p.getNextDate() != null && p.getNextDate().isBefore(LocalDate.now())).count();

        if (overdue > 0) {
            warnings.add(new WarningItem("parasite",
                    String.format("有%d项驱虫已逾期", overdue), "high"));
            suggestions.add("逾期驱虫可能增加寄生虫感染风险，建议及时处理");
        }

        long completed = all.stream().filter(p -> p.getStatus() != null && p.getStatus() == 1).count();
        double base = ((double) completed / all.size()) * 100;
        base -= overdue * 12;
        return Math.max(0, Math.min(100, base));
    }

    private double analyzeWeight(Long petId, Pet pet, List<WarningItem> warnings,
                                  List<String> suggestions, Map<String, String> trends,
                                  AnalysisDetail detail) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);

        List<WeightRecord> records = weightRecordMapper.selectList(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getPetId, petId)
                        .ge(WeightRecord::getRecordDate, startDate)
                        .le(WeightRecord::getRecordDate, endDate)
                        .orderByAsc(WeightRecord::getRecordDate));

        WeightAnalysis wa = new WeightAnalysis();

        if (records.isEmpty()) {
            wa.setCurrentWeight(pet.getWeight() != null ? pet.getWeight().doubleValue() : null);
            wa.setAvgWeight30d(null);
            wa.setWeightChangeRate(null);
            wa.setTrend("无数据");
            detail.setWeightAnalysis(wa);
            warnings.add(new WarningItem("weight", "近30天无体重记录", "medium"));
            suggestions.add("建议定期记录宠物体重，以便追踪健康趋势");
            trends.put("weight", "无数据");
            return 75;
        }

        double latestWeight = records.get(records.size() - 1).getWeight().doubleValue();
        double avgWeight = records.stream()
                .mapToDouble(r -> r.getWeight().doubleValue()).average().orElse(latestWeight);

        wa.setCurrentWeight(latestWeight);
        wa.setAvgWeight30d(Math.round(avgWeight * 100.0) / 100.0);

        String weightTrend;
        if (records.size() >= 2) {
            double firstHalf = records.subList(0, records.size() / 2).stream()
                    .mapToDouble(r -> r.getWeight().doubleValue()).average().orElse(latestWeight);
            double secondHalf = records.subList(records.size() / 2, records.size()).stream()
                    .mapToDouble(r -> r.getWeight().doubleValue()).average().orElse(latestWeight);
            double diff = secondHalf - firstHalf;
            if (Math.abs(diff) < avgWeight * 0.02) {
                weightTrend = "稳定";
            } else if (diff > 0) {
                weightTrend = "上升";
            } else {
                weightTrend = "下降";
            }
        } else {
            weightTrend = "数据不足";
        }
        wa.setTrend(weightTrend);
        trends.put("weight", weightTrend);

        double changeRate = 0;
        if (records.size() >= 2) {
            double sum = 0;
            int count = 0;
            for (int i = 1; i < records.size(); i++) {
                double prev = records.get(i - 1).getWeight().doubleValue();
                double curr = records.get(i).getWeight().doubleValue();
                sum += Math.abs(curr - prev);
                count++;
            }
            double avgChange = sum / count;
            changeRate = avgChange / avgWeight;
            wa.setWeightChangeRate(Math.round(changeRate * 10000.0) / 100.0);

            double changePercent = Math.round(changeRate * 1000.0) / 10.0;
            if (changeRate > 0.15) {
                warnings.add(new WarningItem("weight",
                        String.format("体重近30天波动%.1f%%，波动较大，建议关注饮食和运动", changePercent), "high"));
                suggestions.add("体重波动较大，建议咨询兽医排除健康问题");
            } else if (changeRate > 0.10) {
                warnings.add(new WarningItem("weight",
                        String.format("体重近30天波动%.1f%%，建议关注饮食", changePercent), "medium"));
            }

            if ("上升".equals(weightTrend) && changeRate > 0.05) {
                suggestions.add("体重呈上升趋势，建议适当控制饮食并增加运动量");
            } else if ("下降".equals(weightTrend) && changeRate > 0.05) {
                suggestions.add("体重呈下降趋势，建议关注食欲和消化状况");
            }
        }

        detail.setWeightAnalysis(wa);

        if (changeRate <= 0.02) return 100;
        if (changeRate <= 0.05) return 90;
        if (changeRate <= 0.10) return 75;
        if (changeRate <= 0.15) return 60;
        return 40;
    }

    private double analyzeCheckin(Long userId, List<WarningItem> warnings,
                                   List<String> suggestions, Map<String, String> trends) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        List<CheckinRecord> weekRecords = checkinRecordMapper.selectByUserIdAndDateRange(userId, weekStart, today);
        long activeDays = weekRecords.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .map(CheckinRecord::getRecordDate)
                .distinct()
                .count();

        double score = (activeDays / 7.0) * 100;

        if (activeDays <= 2) {
            warnings.add(new WarningItem("checkin",
                    String.format("本周仅打卡%d天，活跃度较低", (int) activeDays), "medium"));
            suggestions.add("坚持每日打卡有助于养成良好养宠习惯");
        } else if (activeDays >= 6) {
            suggestions.add("打卡习惯良好，继续保持！");
        }

        trends.put("activity", activeDays >= 5 ? "活跃" : activeDays >= 3 ? "一般" : "不活跃");

        List<CheckinStats> stats = checkinStatsMapper.selectList(
                new LambdaQueryWrapper<CheckinStats>().eq(CheckinStats::getUserId, userId));
        int maxStreak = stats.stream()
                .mapToInt(s -> s.getMaxStreak() != null ? s.getMaxStreak() : 0)
                .max().orElse(0);
        if (maxStreak >= 7) {
            suggestions.add(String.format("最长连续打卡%d天，非常棒！", maxStreak));
        }

        return score;
    }

    private String getLevel(int score) {
        if (score >= 90) return "优秀";
        if (score >= 75) return "良好";
        if (score >= 60) return "一般";
        return "需关注";
    }

    private String generateRuleBasedSummary(Pet pet, HealthAnalysisVO vo) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s的健康综合评分为%d分（%s）。", pet.getName(), vo.getScore(), vo.getLevel()));

        if (vo.getWarnings() != null && !vo.getWarnings().isEmpty()) {
            sb.append("需要关注以下问题：");
            for (WarningItem w : vo.getWarnings()) {
                sb.append(w.getMessage()).append("；");
            }
        }

        if (vo.getSuggestions() != null && !vo.getSuggestions().isEmpty()) {
            sb.append("建议：");
            for (String s : vo.getSuggestions()) {
                sb.append(s).append("；");
            }
        }

        return sb.toString();
    }
}

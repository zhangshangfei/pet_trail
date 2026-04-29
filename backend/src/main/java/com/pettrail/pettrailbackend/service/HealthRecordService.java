package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.entity.VaccineReminder;
import com.pettrail.pettrailbackend.entity.ParasiteReminder;
import com.pettrail.pettrailbackend.entity.WeightRecord;
import com.pettrail.pettrailbackend.mapper.VaccineReminderMapper;
import com.pettrail.pettrailbackend.mapper.ParasiteReminderMapper;
import com.pettrail.pettrailbackend.mapper.WeightRecordMapper;
import com.pettrail.pettrailbackend.mapper.CheckinRecordMapper;
import com.pettrail.pettrailbackend.entity.CheckinRecord;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthRecordService {

    private final VaccineReminderMapper vaccineReminderMapper;
    private final ParasiteReminderMapper parasiteReminderMapper;
    private final WeightRecordMapper weightRecordMapper;
    private final CheckinRecordMapper checkinRecordMapper;

    public Map<String, Object> getDashboard(Long userId, Long petId) {
        Map<String, Object> result = new HashMap<>();
        return result;
    }

    public Map<String, Object> calculateHealthScore(Long userId, Long petId) {
        Map<String, Object> result = new HashMap<>();
        double vaccineScore = calcVaccineScore(petId);
        double parasiteScore = calcParasiteScore(petId);
        double weightScore = calcWeightScore(petId);
        double checkinScore = calcCheckinScore(userId);

        double totalScore = vaccineScore * 0.30 + parasiteScore * 0.30
                + weightScore * 0.20 + checkinScore * 0.20;
        int finalScore = (int) Math.round(totalScore);
        finalScore = Math.max(0, Math.min(100, finalScore));

        result.put("score", finalScore);
        result.put("vaccineScore", Math.round(vaccineScore));
        result.put("parasiteScore", Math.round(parasiteScore));
        result.put("weightScore", Math.round(weightScore));
        result.put("checkinScore", Math.round(checkinScore));
        result.put("level", finalScore >= 90 ? "优秀" : finalScore >= 75 ? "良好" : finalScore >= 60 ? "一般" : "需关注");
        return result;
    }

    private double calcVaccineScore(Long petId) {
        List<VaccineReminder> all = vaccineReminderMapper.selectList(
            new LambdaQueryWrapper<VaccineReminder>().eq(VaccineReminder::getPetId, petId));
        if (all.isEmpty()) return 70;
        long completed = all.stream().filter(v -> v.getStatus() != null && v.getStatus() == 1).count();
        long overdue = all.stream().filter(v -> v.getStatus() != null && v.getStatus() != 1
                && v.getNextDate() != null && v.getNextDate().isBefore(LocalDate.now())).count();
        double base = ((double) completed / all.size()) * 100;
        base -= overdue * 10;
        return Math.max(0, Math.min(100, base));
    }

    private double calcParasiteScore(Long petId) {
        List<ParasiteReminder> all = parasiteReminderMapper.selectList(
            new LambdaQueryWrapper<ParasiteReminder>().eq(ParasiteReminder::getPetId, petId));
        if (all.isEmpty()) return 70;
        long completed = all.stream().filter(p -> p.getStatus() != null && p.getStatus() == 1).count();
        long overdue = all.stream().filter(p -> p.getStatus() != null && p.getStatus() != 1
                && p.getNextDate() != null && p.getNextDate().isBefore(LocalDate.now())).count();
        double base = ((double) completed / all.size()) * 100;
        base -= overdue * 10;
        return Math.max(0, Math.min(100, base));
    }

    private double calcWeightScore(Long petId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);
        List<WeightRecord> records = weightRecordMapper.selectList(
            new LambdaQueryWrapper<WeightRecord>()
                .eq(WeightRecord::getPetId, petId)
                .ge(WeightRecord::getRecordDate, startDate)
                .le(WeightRecord::getRecordDate, endDate)
                .orderByAsc(WeightRecord::getRecordDate));
        if (records.size() < 2) return 80;
        double sum = 0;
        int count = 0;
        for (int i = 1; i < records.size(); i++) {
            double prev = records.get(i - 1).getWeight().doubleValue();
            double curr = records.get(i).getWeight().doubleValue();
            sum += Math.abs(curr - prev);
            count++;
        }
        double avgChange = sum / count;
        double avgWeight = records.stream()
                .mapToDouble(r -> r.getWeight().doubleValue()).average().orElse(1);
        double changeRate = avgChange / avgWeight;
        if (changeRate <= 0.02) return 100;
        if (changeRate <= 0.05) return 90;
        if (changeRate <= 0.10) return 75;
        if (changeRate <= 0.15) return 60;
        return 40;
    }

    private double calcCheckinScore(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        List<CheckinRecord> weekRecords = checkinRecordMapper.selectByUserIdAndDateRange(userId, weekStart, today);
        long activeDays = weekRecords.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .map(CheckinRecord::getRecordDate)
                .distinct()
                .count();
        return (activeDays / 7.0) * 100;
    }
}

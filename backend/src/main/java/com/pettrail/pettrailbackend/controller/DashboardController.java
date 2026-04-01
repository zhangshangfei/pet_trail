package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.*;
import com.pettrail.pettrailbackend.service.*;
import com.pettrail.pettrailbackend.util.JwtUtil;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final PetService petService;
    private final VaccineReminderService vaccineReminderService;
    private final ParasiteReminderService parasiteReminderService;
    private final WeightRecordService weightRecordService;
    private final JwtUtil jwtUtil;

    /**
     * 获取数据看板概览
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview(@RequestHeader("Authorization") String token) {
        log.info("获取数据看板概览");
        try {
            Long userId = getUserIdFromToken(token);
            List<Pet> pets = petService.listByUserId(userId);

            Map<String, Object> overview = new HashMap<>();
            overview.put("petCount", pets.size());
            overview.put("upcomingVaccineCount", getUpcomingVaccineCount(userId));
            overview.put("upcomingParasiteCount", getUpcomingParasiteCount(userId));
            overview.put("weightRecordCount", getWeightRecordCount(userId));

            return Result.success(overview);
        } catch (Exception e) {
            log.error("获取数据看板概览失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取宠物健康趋势（最近 30 天）
     */
    @GetMapping("/health-trend")
    public Result<Map<Long, List<WeightRecord>>> getHealthTrend(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "30") int days) {
        log.info("获取宠物健康趋势：days={}", days);
        try {
            Long userId = getUserIdFromToken(token);
            List<Pet> pets = petService.listByUserId(userId);

            Map<Long, List<WeightRecord>> petRecords = new HashMap<>();
            for (Pet pet : pets) {
                List<WeightRecord> records = weightRecordService.getTrend(pet.getId(), days);
                petRecords.put(pet.getId(), records);
            }

            return Result.success(petRecords);
        } catch (Exception e) {
            log.error("获取宠物健康趋势失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有即将到期的提醒
     */
    @GetMapping("/upcoming-reminders")
    public Result<List<Map<String, Object>>> getUpcomingReminders(@RequestHeader("Authorization") String token) {
        log.info("获取所有即将到期的提醒");
        try {
            Long userId = getUserIdFromToken(token);
            List<Pet> pets = petService.listByUserId(userId);

            List<Map<String, Object>> reminders = new ArrayList<>();
            for (Pet pet : pets) {
                // 疫苗提醒
                List<VaccineReminder> vaccineReminders = vaccineReminderService.listUpcoming(pet.getId());
                for (VaccineReminder reminder : vaccineReminders) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "vaccine");
                    item.put("petId", pet.getId());
                    item.put("petName", pet.getName());
                    item.put("name", reminder.getVaccineName());
                    item.put("date", reminder.getNextDate());
                    item.put("daysUntil", getDaysUntil(reminder.getNextDate()));
                    reminders.add(item);
                }

                // 寄生虫提醒
                List<ParasiteReminder> parasiteReminders = parasiteReminderService.listUpcoming(pet.getId());
                for (ParasiteReminder reminder : parasiteReminders) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "parasite");
                    item.put("petId", pet.getId());
                    item.put("petName", pet.getName());
                    item.put("name", reminder.getType());
                    item.put("date", reminder.getNextDate());
                    item.put("daysUntil", getDaysUntil(reminder.getNextDate()));
                    reminders.add(item);
                }
            }

            // 按日期排序
            reminders.sort(Comparator.comparing(item -> (LocalDate) item.get("date")));

            return Result.success(reminders);
        } catch (Exception e) {
            log.error("获取所有即将到期的提醒失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取宠物体重统计
     */
    @GetMapping("/weight-stats")
    public Result<Map<Long, Map<String, Object>>> getWeightStats(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "30") int days) {
        log.info("获取宠物体重统计：days={}", days);
        try {
            Long userId = getUserIdFromToken(token);
            List<Pet> pets = petService.listByUserId(userId);

            Map<Long, Map<String, Object>> stats = new HashMap<>();
            for (Pet pet : pets) {
                List<WeightRecord> records = weightRecordService.getTrend(pet.getId(), days);
                if (!records.isEmpty()) {
                    Map<String, Object> petStats = new HashMap<>();
                    petStats.put("petId", pet.getId());
                    petStats.put("petName", pet.getName());
                    petStats.put("petAvatar", pet.getAvatar());

                    // 计算统计数据
                    List<BigDecimal> weights = records.stream()
                            .map(WeightRecord::getWeight)
                            .filter(Objects::nonNull)
                            .toList();

                    if (weights != null && !weights.isEmpty()) {
                        // 1. 计算总和 (使用 stream reduce 保持 BigDecimal 精度)
                        BigDecimal sum = weights.stream()
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        // 2. 计算平均值 (使用 divide，必须指定精度和舍入模式)
                        BigDecimal avgWeight = sum.divide(
                                BigDecimal.valueOf(weights.size()),
                                2,
                                RoundingMode.HALF_UP
                        );

                        // 3. 获取最大值和最小值
                        BigDecimal maxWeight = Collections.max(weights);
                        BigDecimal minWeight = Collections.min(weights);

                        // 4. 计算变化量
                        BigDecimal firstWeight = weights.get(0);
                        BigDecimal lastWeight = weights.get(weights.size() - 1);
                        BigDecimal totalWeightChange = lastWeight.subtract(firstWeight);

                        // 5. 放入 Map
                        petStats.put("avgWeight", avgWeight);
                        petStats.put("maxWeight", maxWeight);
                        petStats.put("minWeight", minWeight);
                        petStats.put("totalWeightChange", totalWeightChange);
                        petStats.put("recordCount", weights.size());
                    }

                    stats.put(pet.getId(), petStats);
                }
            }

            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取宠物体重统计失败：{}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户 ID（从 Token 解析）
     */
    private Long getUserIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token 格式不正确");
        }
        String tokenValue = token.substring(7);
        return jwtUtil.getUserIdFromToken(tokenValue);
    }

    /**
     * 计算距离天数
     */
    private int getDaysUntil(LocalDate date) {
        LocalDate now = LocalDate.now();
        long diff = java.time.temporal.ChronoUnit.DAYS.between(now, date);
        return (int) diff;
    }

    /**
     * 获取即将到期的疫苗数量
     */
    private int getUpcomingVaccineCount(Long userId) {
        int count = 0;
        List<Pet> pets = petService.listByUserId(userId);
        for (Pet pet : pets) {
            count += vaccineReminderService.listUpcoming(pet.getId()).size();
        }
        return count;
    }

    /**
     * 获取即将到期的寄生虫数量
     */
    private int getUpcomingParasiteCount(Long userId) {
        int count = 0;
        List<Pet> pets = petService.listByUserId(userId);
        for (Pet pet : pets) {
            count += parasiteReminderService.listUpcoming(pet.getId()).size();
        }
        return count;
    }

    /**
     * 获取体重记录总数
     */
    private int getWeightRecordCount(Long userId) {
        int count = 0;
        List<Pet> pets = petService.listByUserId(userId);
        for (Pet pet : pets) {
            count += weightRecordService.listByPetId(pet.getId()).size();
        }
        return count;
    }
}

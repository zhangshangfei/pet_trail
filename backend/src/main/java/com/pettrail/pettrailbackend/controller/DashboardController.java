package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.*;
import com.pettrail.pettrailbackend.service.*;
import com.pettrail.pettrailbackend.util.JwtUtil;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController extends BaseController {

    private final PetService petService;
    private final VaccineReminderService vaccineReminderService;
    private final ParasiteReminderService parasiteReminderService;
    private final WeightRecordService weightRecordService;
    private final JwtUtil jwtUtil;

    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        List<Pet> pets = petService.listByUserId(userId);
        Map<String, Object> overview = new HashMap<>();
        overview.put("petCount", pets.size());
        overview.put("upcomingVaccineCount", getUpcomingVaccineCount(userId));
        overview.put("upcomingParasiteCount", getUpcomingParasiteCount(userId));
        overview.put("weightRecordCount", getWeightRecordCount(userId));
        return Result.success(overview);
    }

    @GetMapping("/health-trend")
    public Result<Map<Long, List<WeightRecord>>> getHealthTrend(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "30") int days) {
        Long userId = getUserIdFromToken(token);
        List<Pet> pets = petService.listByUserId(userId);
        Map<Long, List<WeightRecord>> petRecords = new HashMap<>();
        for (Pet pet : pets) {
            petRecords.put(pet.getId(), weightRecordService.getTrend(pet.getId(), days));
        }
        return Result.success(petRecords);
    }

    @GetMapping("/upcoming-reminders")
    public Result<List<Map<String, Object>>> getUpcomingReminders(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        List<Pet> pets = petService.listByUserId(userId);
        List<Map<String, Object>> reminders = new ArrayList<>();

        for (Pet pet : pets) {
            for (VaccineReminder r : vaccineReminderService.listUpcoming(pet.getId())) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "vaccine");
                item.put("petId", pet.getId());
                item.put("petName", pet.getName());
                item.put("name", r.getVaccineName());
                item.put("date", r.getNextDate());
                item.put("daysUntil", getDaysUntil(r.getNextDate()));
                reminders.add(item);
            }
            for (ParasiteReminder r : parasiteReminderService.listUpcoming(pet.getId())) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "parasite");
                item.put("petId", pet.getId());
                item.put("petName", pet.getName());
                item.put("name", r.getType());
                item.put("date", r.getNextDate());
                item.put("daysUntil", getDaysUntil(r.getNextDate()));
                reminders.add(item);
            }
        }
        reminders.sort(Comparator.comparing(item -> (LocalDate) item.get("date")));
        return Result.success(reminders);
    }

    @GetMapping("/weight-stats")
    public Result<Map<Long, Map<String, Object>>> getWeightStats(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "30") int days) {
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

                List<BigDecimal> weights = records.stream()
                        .map(WeightRecord::getWeight)
                        .filter(Objects::nonNull)
                        .toList();

                if (weights != null && !weights.isEmpty()) {
                    BigDecimal sum = weights.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal avgWeight = sum.divide(BigDecimal.valueOf(weights.size()), 2, RoundingMode.HALF_UP);
                    BigDecimal maxWeight = Collections.max(weights);
                    BigDecimal minWeight = Collections.min(weights);
                    BigDecimal totalWeightChange = weights.get(weights.size() - 1).subtract(weights.get(0));

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
    }

    private Long getUserIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token 格式不正确");
        }
        return jwtUtil.getUserIdFromToken(token.substring(7));
    }

    private int getDaysUntil(LocalDate date) {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    private int getUpcomingVaccineCount(Long userId) {
        int count = 0;
        for (Pet pet : petService.listByUserId(userId)) {
            count += vaccineReminderService.listUpcoming(pet.getId()).size();
        }
        return count;
    }

    private int getUpcomingParasiteCount(Long userId) {
        int count = 0;
        for (Pet pet : petService.listByUserId(userId)) {
            count += parasiteReminderService.listUpcoming(pet.getId()).size();
        }
        return count;
    }

    private int getWeightRecordCount(Long userId) {
        int count = 0;
        for (Pet pet : petService.listByUserId(userId)) {
            count += weightRecordService.listByPetId(pet.getId()).size();
        }
        return count;
    }
}

package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.*;
import com.pettrail.pettrailbackend.entity.*;
import com.pettrail.pettrailbackend.service.*;
import com.pettrail.pettrailbackend.util.JwtUtil;
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
    public Result<DashboardOverviewVO> getOverview(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setPetCount(petService.listByUserId(userId).size());
        vo.setUpcomingVaccineCount(getUpcomingVaccineCount(userId));
        vo.setUpcomingParasiteCount(getUpcomingParasiteCount(userId));
        vo.setWeightRecordCount(getWeightRecordCount(userId));
        return Result.success(vo);
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
    public Result<List<ReminderItemVO>> getUpcomingReminders(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        List<Pet> pets = petService.listByUserId(userId);
        List<ReminderItemVO> reminders = new ArrayList<>();

        for (Pet pet : pets) {
            for (VaccineReminder r : vaccineReminderService.listUpcoming(pet.getId())) {
                ReminderItemVO item = new ReminderItemVO();
                item.setType("vaccine");
                item.setPetId(pet.getId());
                item.setPetName(pet.getName());
                item.setName(r.getVaccineName());
                item.setDate(r.getNextDate());
                item.setDaysUntil(getDaysUntil(r.getNextDate()));
                reminders.add(item);
            }
            for (ParasiteReminderVO r : parasiteReminderService.listUpcoming(pet.getId())) {
                ReminderItemVO item = new ReminderItemVO();
                item.setType("parasite");
                item.setPetId(pet.getId());
                item.setPetName(pet.getName());
                item.setName(r.getTypeName());
                item.setDate(r.getNextDate());
                item.setDaysUntil(getDaysUntil(r.getNextDate()));
                reminders.add(item);
            }
        }
        reminders.sort(Comparator.comparing(ReminderItemVO::getDate));
        return Result.success(reminders);
    }

    @GetMapping("/weight-stats")
    public Result<Map<Long, PetWeightStatsVO>> getWeightStats(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "30") int days) {
        Long userId = getUserIdFromToken(token);
        List<Pet> pets = petService.listByUserId(userId);
        Map<Long, PetWeightStatsVO> stats = new HashMap<>();

        for (Pet pet : pets) {
            List<WeightRecord> records = weightRecordService.getTrend(pet.getId(), days);
            if (!records.isEmpty()) {
                PetWeightStatsVO petStats = new PetWeightStatsVO();
                petStats.setPetId(pet.getId());
                petStats.setPetName(pet.getName());
                petStats.setPetAvatar(pet.getAvatar());

                List<BigDecimal> weights = records.stream()
                        .map(WeightRecord::getWeight)
                        .filter(Objects::nonNull)
                        .toList();

                if (!weights.isEmpty()) {
                    BigDecimal sum = weights.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal avgWeight = sum.divide(BigDecimal.valueOf(weights.size()), 2, RoundingMode.HALF_UP);
                    BigDecimal maxWeight = Collections.max(weights);
                    BigDecimal minWeight = Collections.min(weights);
                    BigDecimal totalWeightChange = weights.get(weights.size() - 1).subtract(weights.get(0));

                    petStats.setAvgWeight(avgWeight);
                    petStats.setMaxWeight(maxWeight);
                    petStats.setMinWeight(minWeight);
                    petStats.setTotalWeightChange(totalWeightChange);
                    petStats.setRecordCount(weights.size());
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

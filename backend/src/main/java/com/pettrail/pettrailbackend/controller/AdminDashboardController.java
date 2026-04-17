package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.mapper.*;
import com.pettrail.pettrailbackend.entity.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin-仪表盘", description = "后台数据统计")
public class AdminDashboardController {

    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final PostMapper postMapper;
    private final PostCommentMapper commentMapper;
    private final ReportMapper reportMapper;
    private final NotificationMapper notificationMapper;

    @GetMapping("/stats")
    @Operation(summary = "获取总览统计数据")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        Long totalUsers = userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getStatus, 1));
        Long totalPets = petMapper.selectCount(null);
        Long totalPosts = postMapper.selectCount(null);
        Long totalComments = commentMapper.selectCount(null);
        Long pendingReports = reportMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Report>()
                        .eq(Report::getStatus, 0));

        stats.put("totalUsers", totalUsers);
        stats.put("totalPets", totalPets);
        stats.put("totalPosts", totalPosts);
        stats.put("totalComments", totalComments);
        stats.put("pendingReports", pendingReports);

        return Result.success(stats);
    }

    @GetMapping("/today")
    @Operation(summary = "获取今日数据")
    public Result<Map<String, Object>> getTodayStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        Long todayNewUsers = userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .ge(User::getCreatedAt, todayStart));
        Long todayNewPosts = postMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Post>()
                        .ge(Post::getCreatedAt, todayStart));

        stats.put("todayNewUsers", todayNewUsers);
        stats.put("todayNewPosts", todayNewPosts);

        return Result.success(stats);
    }
}

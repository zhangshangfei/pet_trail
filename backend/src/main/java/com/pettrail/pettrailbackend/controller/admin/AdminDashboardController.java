package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.*;
import com.pettrail.pettrailbackend.mapper.*;
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
public class AdminDashboardController extends BaseAdminController {

    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final PostMapper postMapper;
    private final PostCommentMapper commentMapper;
    private final ReportMapper reportMapper;

    @GetMapping("/stats")
    @Operation(summary = "获取总览统计数据")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, 1)));
        stats.put("totalPets", petMapper.selectCount(null));
        stats.put("totalPosts", postMapper.selectCount(null));
        stats.put("totalComments", commentMapper.selectCount(null));
        stats.put("pendingReports", reportMapper.selectCount(new LambdaQueryWrapper<Report>().eq(Report::getStatus, 0)));
        return Result.success(stats);
    }

    @GetMapping("/today")
    @Operation(summary = "获取今日数据")
    public Result<Map<String, Object>> getTodayStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        stats.put("todayNewUsers", userMapper.selectCount(new LambdaQueryWrapper<User>().ge(User::getCreatedAt, todayStart)));
        stats.put("todayNewPosts", postMapper.selectCount(new LambdaQueryWrapper<Post>().ge(Post::getCreatedAt, todayStart)));
        return Result.success(stats);
    }

    @GetMapping("/trend")
    @Operation(summary = "获取趋势数据")
    public Result<Map<String, Object>> getTrend(@RequestParam(defaultValue = "30") int days) {
        Map<String, Object> result = new HashMap<>();
        java.util.List<String> dates = new java.util.ArrayList<>();
        java.util.List<Long> newUsers = new java.util.ArrayList<>();
        java.util.List<Long> newPosts = new java.util.ArrayList<>();
        java.util.List<Long> activeUsers = new java.util.ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.toString());
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
            newUsers.add(userMapper.selectCount(new LambdaQueryWrapper<User>().ge(User::getCreatedAt, dayStart).le(User::getCreatedAt, dayEnd)));
            newPosts.add(postMapper.selectCount(new LambdaQueryWrapper<Post>().ge(Post::getCreatedAt, dayStart).le(Post::getCreatedAt, dayEnd)));
            activeUsers.add(postMapper.selectCount(new LambdaQueryWrapper<Post>().ge(Post::getCreatedAt, dayStart).le(Post::getCreatedAt, dayEnd)));
        }

        result.put("dates", dates);
        result.put("newUsers", newUsers);
        result.put("newPosts", newPosts);
        result.put("activeUsers", activeUsers);
        return Result.success(result);
    }

    @GetMapping("/audit-stats")
    @Operation(summary = "获取审核统计")
    public Result<Map<String, Object>> getAuditStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getAuditStatus, 0)));
        stats.put("approved", postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getAuditStatus, 1)));
        stats.put("rejected", postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getAuditStatus, 2)));
        return Result.success(stats);
    }
}

package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.*;
import com.pettrail.pettrailbackend.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final PostMapper postMapper;
    private final PostCommentMapper commentMapper;
    private final ReportMapper reportMapper;
    private final PostLikeMapper postLikeMapper;
    private final FollowMapper followMapper;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, 1)));
        stats.put("totalPets", petMapper.selectCount(null));
        stats.put("totalPosts", postMapper.selectCount(null));
        stats.put("totalComments", commentMapper.selectCount(null));
        stats.put("pendingReports", reportMapper.selectCount(new LambdaQueryWrapper<Report>().eq(Report::getStatus, 0)));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        stats.put("todayActiveUsers", countActiveUsers(todayStart, LocalDateTime.now()));

        return stats;
    }

    public Map<String, Object> getTodayStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        stats.put("todayNewUsers", userMapper.selectCount(new LambdaQueryWrapper<User>().ge(User::getCreatedAt, todayStart)));
        stats.put("todayNewPosts", postMapper.selectCount(new LambdaQueryWrapper<Post>().ge(Post::getCreatedAt, todayStart)));
        stats.put("todayActiveUsers", countActiveUsers(todayStart, LocalDateTime.now()));
        return stats;
    }

    private long countActiveUsers(LocalDateTime start, LocalDateTime end) {
        List<Long> userIds = new ArrayList<>();

        postMapper.selectList(new LambdaQueryWrapper<Post>()
                        .select(Post::getUserId)
                        .ge(Post::getCreatedAt, start)
                        .le(Post::getCreatedAt, end))
                .forEach(p -> { if (!userIds.contains(p.getUserId())) userIds.add(p.getUserId()); });

        commentMapper.selectList(new LambdaQueryWrapper<PostComment>()
                        .select(PostComment::getUserId)
                        .ge(PostComment::getCreatedAt, start)
                        .le(PostComment::getCreatedAt, end))
                .forEach(c -> { if (!userIds.contains(c.getUserId())) userIds.add(c.getUserId()); });

        postLikeMapper.selectList(new LambdaQueryWrapper<PostLike>()
                        .select(PostLike::getUserId)
                        .ge(PostLike::getCreatedAt, start)
                        .le(PostLike::getCreatedAt, end))
                .forEach(l -> { if (!userIds.contains(l.getUserId())) userIds.add(l.getUserId()); });

        return userIds.size();
    }

    public Map<String, Object> getTrend(int days) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Long> newUsers = new ArrayList<>();
        List<Long> newPosts = new ArrayList<>();
        List<Long> activeUsers = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.toString());
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
            newUsers.add(userMapper.selectCount(new LambdaQueryWrapper<User>().ge(User::getCreatedAt, dayStart).le(User::getCreatedAt, dayEnd)));
            newPosts.add(postMapper.selectCount(new LambdaQueryWrapper<Post>().ge(Post::getCreatedAt, dayStart).le(Post::getCreatedAt, dayEnd)));
            activeUsers.add(countActiveUsers(dayStart, dayEnd));
        }

        result.put("dates", dates);
        result.put("newUsers", newUsers);
        result.put("newPosts", newPosts);
        result.put("activeUsers", activeUsers);
        return result;
    }

    public Map<String, Object> getAuditStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getAuditStatus, 0)));
        stats.put("approved", postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getAuditStatus, 1)));
        stats.put("rejected", postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getAuditStatus, 2)));
        return stats;
    }

    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("petCount", petMapper.selectCount(new LambdaQueryWrapper<Pet>().eq(Pet::getUserId, userId)));
        stats.put("postCount", postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getUserId, userId)));
        stats.put("likeCount", postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>().eq(PostLike::getUserId, userId)));
        stats.put("followerCount", followMapper.selectCount(new LambdaQueryWrapper<Follow>().eq(Follow::getFolloweeId, userId)));
        stats.put("followingCount", followMapper.selectCount(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowerId, userId)));
        return stats;
    }
}

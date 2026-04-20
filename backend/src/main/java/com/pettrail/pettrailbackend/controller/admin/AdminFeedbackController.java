package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.FeedbackReplyDTO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.dto.StatusDTO;
import com.pettrail.pettrailbackend.entity.Feedback;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.FeedbackMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Admin-反馈管理", description = "后台反馈管理")
public class AdminFeedbackController extends BaseAdminController {

    private final FeedbackMapper feedbackMapper;
    private final UserMapper userMapper;

    @GetMapping
    @Operation(summary = "分页查询反馈列表")
    public Result<Page<Feedback>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type) {
        Page<Feedback> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Feedback::getStatus, status);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Feedback::getType, type);
        }
        wrapper.orderByDesc(Feedback::getCreatedAt);
        Page<Feedback> result = feedbackMapper.selectPage(pageParam, wrapper);
        fillFeedbackUserNickname(result);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取反馈详情")
    public Result<Feedback> getDetail(@PathVariable Long id) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            return Result.error(404, "反馈不存在");
        }
        if (feedback.getUserId() != null) {
            User user = userMapper.selectById(feedback.getUserId());
            if (user != null) {
                feedback.setUserNickname(user.getNickname());
            }
        }
        return Result.success(feedback);
    }

    @PutMapping("/{id}/reply")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "feedback", action = "reply", detail = "回复反馈")
    @Operation(summary = "回复反馈")
    public Result<Void> reply(@PathVariable Long id, @RequestBody FeedbackReplyDTO dto) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            return Result.error(404, "反馈不存在");
        }
        String replyContent = dto.getReply();
        if (replyContent == null || replyContent.trim().isEmpty()) {
            return Result.error(400, "回复内容不能为空");
        }
        feedback.setReply(replyContent.trim());
        feedback.setStatus(2);
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.updateById(feedback);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "feedback", action = "update_status", detail = "更新反馈状态")
    @Operation(summary = "更新反馈状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusDTO dto) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            return Result.error(404, "反馈不存在");
        }
        Integer newStatus = dto.getStatus();
        if (newStatus == null) {
            return Result.error(400, "状态不能为空");
        }
        feedback.setStatus(newStatus);
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.updateById(feedback);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "feedback", action = "delete", detail = "删除反馈")
    @Operation(summary = "删除反馈")
    public Result<Void> delete(@PathVariable Long id) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            return Result.error(404, "反馈不存在");
        }
        feedbackMapper.deleteById(id);
        return Result.success(null);
    }

    private void fillFeedbackUserNickname(Page<Feedback> result) {
        Set<Long> userIds = result.getRecords().stream()
                .map(Feedback::getUserId)
                .filter(uid -> uid != null)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        Map<Long, String> nicknameMap = buildNicknameMap(userIds, userMapper);
        for (Feedback f : result.getRecords()) {
            if (f.getUserId() != null) {
                f.setUserNickname(nicknameMap.getOrDefault(f.getUserId(), "未知用户"));
            }
        }
    }
}

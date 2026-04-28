package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.entity.Feedback;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.FeedbackMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final UserMapper userMapper;

    public Page<Feedback> adminListFeedbacks(int page, int size, Integer status, String type) {
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
        return result;
    }

    public Feedback adminGetFeedbackDetail(Long id) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException(404, "反馈不存在");
        }
        if (feedback.getUserId() != null) {
            User user = userMapper.selectById(feedback.getUserId());
            if (user != null) {
                feedback.setUserNickname(user.getNickname());
            }
        }
        return feedback;
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminReplyFeedback(Long id, String replyContent) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException(404, "反馈不存在");
        }
        if (replyContent == null || replyContent.trim().isEmpty()) {
            throw new BusinessException(400, "回复内容不能为空");
        }
        feedback.setReply(replyContent.trim());
        feedback.setStatus(2);
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.updateById(feedback);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateFeedbackStatus(Long id, Integer newStatus, String reply) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException(404, "反馈不存在");
        }
        if (newStatus == null) {
            throw new BusinessException(400, "状态不能为空");
        }
        feedback.setStatus(newStatus);
        if (reply != null && !reply.trim().isEmpty()) {
            feedback.setReply(reply.trim());
        }
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.updateById(feedback);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteFeedback(Long id) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException(404, "反馈不存在");
        }
        feedbackMapper.deleteById(id);
    }

    private void fillFeedbackUserNickname(Page<Feedback> result) {
        Set<Long> userIds = result.getRecords().stream()
                .map(Feedback::getUserId)
                .filter(uid -> uid != null)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        Map<Long, String> nicknameMap = buildNicknameMap(userIds);
        for (Feedback f : result.getRecords()) {
            if (f.getUserId() != null) {
                f.setUserNickname(nicknameMap.getOrDefault(f.getUserId(), "未知用户"));
            }
        }
    }

    private Map<Long, String> buildNicknameMap(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId,
                        u -> u.getNickname() != null ? u.getNickname() : "用户" + u.getId(),
                        (a, b) -> a));
    }
}

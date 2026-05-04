package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.FeedbackAdminVO;
import com.pettrail.pettrailbackend.entity.Feedback;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.dto.FeedbackCreateDTO;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.FeedbackMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final UserMapper userMapper;

    public Page<FeedbackAdminVO> adminListFeedbacks(int page, int size, Integer status, String type) {
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

        Set<Long> userIds = result.getRecords().stream()
                .map(Feedback::getUserId)
                .filter(uid -> uid != null)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        Page<FeedbackAdminVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(f -> {
            FeedbackAdminVO vo = new FeedbackAdminVO();
            vo.setId(f.getId());
            vo.setUserId(f.getUserId());
            vo.setType(f.getType());
            vo.setContent(f.getContent());
            vo.setContact(f.getContact());
            vo.setImages(f.getImages());
            vo.setStatus(f.getStatus());
            vo.setReply(f.getReply());
            vo.setCreatedAt(f.getCreatedAt());
            vo.setUpdatedAt(f.getUpdatedAt());

            User user = f.getUserId() != null ? userMap.get(f.getUserId()) : null;
            vo.setUserNickname(user != null ? (user.getNickname() != null ? user.getNickname() : "用户" + f.getUserId()) : "未知用户");
            vo.setUserAvatar(user != null && user.getAvatar() != null ? user.getAvatar() : "");
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    public FeedbackAdminVO adminGetFeedbackDetail(Long id) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException(404, "反馈不存在");
        }
        FeedbackAdminVO vo = new FeedbackAdminVO();
        vo.setId(feedback.getId());
        vo.setUserId(feedback.getUserId());
        vo.setType(feedback.getType());
        vo.setContent(feedback.getContent());
        vo.setContact(feedback.getContact());
        vo.setImages(feedback.getImages());
        vo.setStatus(feedback.getStatus());
        vo.setReply(feedback.getReply());
        vo.setCreatedAt(feedback.getCreatedAt());
        vo.setUpdatedAt(feedback.getUpdatedAt());

        if (feedback.getUserId() != null) {
            User user = userMapper.selectById(feedback.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname() != null ? user.getNickname() : "用户" + feedback.getUserId());
                vo.setUserAvatar(user.getAvatar() != null ? user.getAvatar() : "");
            } else {
                vo.setUserNickname("未知用户");
                vo.setUserAvatar("");
            }
        }
        return vo;
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

    @Transactional(rollbackFor = Exception.class)
    public void submitFeedback(Long userId, FeedbackCreateDTO dto) {
        String type = dto.getType() != null ? dto.getType() : "other";
        String content = dto.getContent() != null ? dto.getContent() : "";

        if (content.trim().isEmpty()) {
            throw new BusinessException(400, "反馈内容不能为空");
        }

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(type);
        feedback.setContent(content.trim());
        feedback.setContact(dto.getContact());
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            feedback.setImages(JSON.toJSONString(dto.getImages()));
        }
        feedback.setStatus(0);
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.insert(feedback);
    }

    public List<Feedback> getMyFeedbackList(Long userId) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getUserId, userId).orderByDesc(Feedback::getCreatedAt);
        return feedbackMapper.selectList(wrapper);
    }
}

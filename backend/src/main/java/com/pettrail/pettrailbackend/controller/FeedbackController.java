package com.pettrail.pettrailbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Feedback;
import com.pettrail.pettrailbackend.mapper.FeedbackMapper;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackMapper feedbackMapper;

    @PostMapping
    public Result<Void> submitFeedback(@RequestBody java.util.Map<String, Object> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        String type = body.get("type") != null ? body.get("type").toString() : "other";
        String content = body.get("content") != null ? body.get("content").toString() : "";
        String contact = body.get("contact") != null ? body.get("contact").toString() : null;
        Object imagesObj = body.get("images");

        if (content.trim().isEmpty()) {
            return Result.error(400, "反馈内容不能为空");
        }

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(type);
        feedback.setContent(content.trim());
        feedback.setContact(contact);
        if (imagesObj != null) {
            feedback.setImages(imagesObj.toString());
        }
        feedback.setStatus(0);
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());

        try {
            feedbackMapper.insert(feedback);
            return Result.success();
        } catch (Exception e) {
            log.error("提交反馈失败: {}", e.getMessage(), e);
            return Result.error("提交失败，请稍后重试");
        }
    }

    @GetMapping("/my")
    public Result<List<Feedback>> getMyFeedbackList() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        try {
            LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Feedback::getUserId, userId)
                    .orderByDesc(Feedback::getCreatedAt);
            List<Feedback> list = feedbackMapper.selectList(wrapper);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取反馈列表失败: {}", e.getMessage(), e);
            return Result.error("获取反馈列表失败");
        }
    }
}

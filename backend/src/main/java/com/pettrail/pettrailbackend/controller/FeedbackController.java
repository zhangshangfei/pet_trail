package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Feedback;
import com.pettrail.pettrailbackend.mapper.FeedbackMapper;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
}

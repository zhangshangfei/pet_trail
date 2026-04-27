package com.pettrail.pettrailbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.FeedbackCreateDTO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Feedback;
import com.pettrail.pettrailbackend.mapper.FeedbackMapper;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController extends BaseController {

    private final FeedbackMapper feedbackMapper;

    @PostMapping
    public Result<Void> submitFeedback(@RequestBody FeedbackCreateDTO dto) {
        Long userId = requireLogin();
        String type = dto.getType() != null ? dto.getType() : "other";
        String content = dto.getContent() != null ? dto.getContent() : "";
        String contact = dto.getContact();

        if (content.trim().isEmpty()) {
            return Result.error(400, "反馈内容不能为空");
        }

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(type);
        feedback.setContent(content.trim());
        feedback.setContact(contact);
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            feedback.setImages(JSON.toJSONString(dto.getImages()));
        }
        feedback.setStatus(0);
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.insert(feedback);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<Feedback>> getMyFeedbackList() {
        Long userId = requireLogin();
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getUserId, userId).orderByDesc(Feedback::getCreatedAt);
        return Result.success(feedbackMapper.selectList(wrapper));
    }
}

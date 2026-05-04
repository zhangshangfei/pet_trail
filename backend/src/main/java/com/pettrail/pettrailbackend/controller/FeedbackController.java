package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.FeedbackCreateDTO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Feedback;
import com.pettrail.pettrailbackend.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController extends BaseController {

    private final FeedbackService feedbackService;

    @PostMapping
    public Result<Void> submitFeedback(@RequestBody FeedbackCreateDTO dto) {
        Long userId = requireLogin();
        feedbackService.submitFeedback(userId, dto);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<Feedback>> getMyFeedbackList() {
        Long userId = requireLogin();
        return Result.success(feedbackService.getMyFeedbackList(userId));
    }
}

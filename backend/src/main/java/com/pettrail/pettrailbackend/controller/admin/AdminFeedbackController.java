package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.FeedbackReplyDTO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.dto.StatusDTO;
import com.pettrail.pettrailbackend.entity.Feedback;
import com.pettrail.pettrailbackend.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Admin-反馈管理", description = "后台反馈管理")
public class AdminFeedbackController extends BaseAdminController {

    private final FeedbackService feedbackService;

    @GetMapping
    @Operation(summary = "分页查询反馈列表")
    public Result<Page<Feedback>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type) {
        return Result.success(feedbackService.adminListFeedbacks(page, size, status, type));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取反馈详情")
    public Result<Feedback> getDetail(@PathVariable Long id) {
        return Result.success(feedbackService.adminGetFeedbackDetail(id));
    }

    @PutMapping("/{id}/reply")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "feedback", action = "reply", detail = "回复反馈")
    @Operation(summary = "回复反馈")
    public Result<Void> reply(@PathVariable Long id, @RequestBody FeedbackReplyDTO dto) {
        feedbackService.adminReplyFeedback(id, dto.getReply());
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "feedback", action = "update_status", detail = "更新反馈状态")
    @Operation(summary = "更新反馈状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusDTO dto) {
        feedbackService.adminUpdateFeedbackStatus(id, dto.getStatus(), dto.getReply());
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "feedback", action = "delete", detail = "删除反馈")
    @Operation(summary = "删除反馈")
    public Result<Void> delete(@PathVariable Long id) {
        feedbackService.adminDeleteFeedback(id);
        return Result.success(null);
    }
}

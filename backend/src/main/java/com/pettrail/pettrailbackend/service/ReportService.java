package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.entity.Report;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportMapper reportMapper;

    public Report createReport(Long reporterId, Long targetId, String targetType, String reason, String description) {
        int existing = reportMapper.countByReporterAndTarget(reporterId, targetId, targetType);
        if (existing > 0) {
            throw new BusinessException(409, "您已举报过该内容，请勿重复举报");
        }

        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetId(targetId);
        report.setTargetType(targetType);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus(0);
        reportMapper.insert(report);

        log.info("用户举报：reporterId={}, targetId={}, targetType={}, reason={}", reporterId, targetId, targetType, reason);
        return report;
    }

    public Page<Report> adminListReports(int page, int size, Integer status, String targetType) {
        Page<Report> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(Report::getStatus, status);
        if (targetType != null && !targetType.isEmpty()) wrapper.eq(Report::getTargetType, targetType);
        wrapper.orderByDesc(Report::getCreatedAt);
        return reportMapper.selectPage(pageParam, wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminHandleReport(Long id, Integer status, String result) {
        Report report = reportMapper.selectById(id);
        if (report == null) throw new BusinessException(404, "举报不存在");
        report.setStatus(status);
        if (result != null) report.setResult(result);
        reportMapper.updateById(report);
    }
}

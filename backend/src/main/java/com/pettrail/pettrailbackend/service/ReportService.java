package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.entity.Report;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}

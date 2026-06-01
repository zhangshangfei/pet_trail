package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.ReportVO;
import com.pettrail.pettrailbackend.entity.Report;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.ReportMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportMapper reportMapper;
    private final UserMapper userMapper;

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

    public Page<ReportVO> adminListReports(int page, int size, Integer status, String targetType) {
        Page<Report> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(Report::getStatus, status);
        if (targetType != null && !targetType.isEmpty()) wrapper.eq(Report::getTargetType, targetType);
        wrapper.orderByDesc(Report::getCreatedAt);
        Page<Report> reportPage = reportMapper.selectPage(pageParam, wrapper);
        Page<ReportVO> voPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        voPage.setRecords(reportPage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    public List<ReportVO> getMyReportList(Long reporterId) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReporterId, reporterId);
        wrapper.orderByDesc(Report::getCreatedAt);
        List<Report> reports = reportMapper.selectList(wrapper);
        return reports.stream().map(this::toVO).collect(Collectors.toList());
    }

    private ReportVO toVO(Report report) {
        ReportVO vo = new ReportVO();
        vo.setId(report.getId());
        vo.setReporterId(report.getReporterId());
        vo.setTargetId(report.getTargetId());
        vo.setTargetType(report.getTargetType());
        vo.setReason(report.getReason());
        vo.setDescription(report.getDescription());
        vo.setStatus(report.getStatus());
        vo.setResult(report.getResult());
        vo.setCreatedAt(report.getCreatedAt());
        try {
            User reporter = userMapper.selectById(report.getReporterId());
            if (reporter != null) {
                vo.setReporterNickname(reporter.getNickname());
                vo.setReporterAvatar(reporter.getAvatar());
            }
        } catch (Exception e) {
            log.warn("查询举报人信息失败: reporterId={}", report.getReporterId());
        }
        if ("user".equals(report.getTargetType()) && report.getTargetId() != null) {
            try {
                User targetUser = userMapper.selectById(report.getTargetId());
                if (targetUser != null) {
                    vo.setTargetNickname(targetUser.getNickname());
                    vo.setTargetAvatar(targetUser.getAvatar());
                }
            } catch (Exception e) {
                log.warn("查询被举报用户昵称失败: targetId={}", report.getTargetId());
            }
        }
        return vo;
    }

    public boolean hasReported(Long reporterId, Long targetId, String targetType) {
        return reportMapper.countByReporterAndTarget(reporterId, targetId, targetType) > 0;
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

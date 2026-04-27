package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.entity.AdminOperationLog;
import com.pettrail.pettrailbackend.mapper.AdminOperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOperationLogService {

    private final AdminOperationLogMapper logMapper;

    public Page<AdminOperationLog> adminListLogs(int page, int size, String adminName, String module,
                                                   String startDate, String endDate) {
        Page<AdminOperationLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (adminName != null && !adminName.isEmpty()) {
            wrapper.like(AdminOperationLog::getAdminName, adminName);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(AdminOperationLog::getModule, module);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(AdminOperationLog::getCreatedAt, startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(AdminOperationLog::getCreatedAt, endDate + " 23:59:59");
        }
        wrapper.orderByDesc(AdminOperationLog::getCreatedAt);
        return logMapper.selectPage(pageParam, wrapper);
    }
}

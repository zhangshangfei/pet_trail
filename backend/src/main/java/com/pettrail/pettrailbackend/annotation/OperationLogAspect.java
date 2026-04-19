package com.pettrail.pettrailbackend.annotation;

import com.pettrail.pettrailbackend.entity.AdminOperationLog;
import com.pettrail.pettrailbackend.mapper.AdminOperationLogMapper;
import com.pettrail.pettrailbackend.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final AdminOperationLogMapper logMapper;

    @Around("@annotation(operationLog)")
    public Object recordLog(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            AdminOperationLog logEntry = new AdminOperationLog();
            logEntry.setAdminId(UserContext.getCurrentUserId());
            logEntry.setAdminName(UserContext.getCurrentUsername());
            logEntry.setModule(operationLog.module());
            logEntry.setAction(operationLog.action());
            logEntry.setDetail(operationLog.detail());
            logEntry.setCreatedAt(LocalDateTime.now());

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                logEntry.setIp(getClientIp(request));
            }

            logMapper.insert(logEntry);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }

        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

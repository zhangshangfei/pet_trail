package com.pettrail.pettrailbackend.annotation;

import com.pettrail.pettrailbackend.exception.ForbiddenException;
import com.pettrail.pettrailbackend.service.AdminService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequireButtonAspect {

    private final AdminService adminService;

    @Around("@annotation(requireButton)")
    public Object checkButton(ProceedingJoinPoint joinPoint, RequireButton requireButton) throws Throwable {
        String buttonCode = requireButton.value();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("未登录");
        }

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) {
            return joinPoint.proceed();
        }

        Long adminId = UserContext.getCurrentUserId();
        if (adminId == null) {
            throw new ForbiddenException("未登录");
        }

        if (!adminService.hasButtonPermission(adminId, buttonCode)) {
            log.warn("按钮权限不足: 需要 {}, 管理员ID: {}", buttonCode, adminId);
            throw new ForbiddenException("权限不足，需要 " + buttonCode + " 权限");
        }

        return joinPoint.proceed();
    }
}

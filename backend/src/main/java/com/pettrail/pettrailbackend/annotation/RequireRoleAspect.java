package com.pettrail.pettrailbackend.annotation;

import com.pettrail.pettrailbackend.dto.Result;
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
public class RequireRoleAspect {


    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        String requiredRole = requireRole.value();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Result.error(401, "未登录");
        }

        boolean hasRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + requiredRole));

        if ("ADMIN".equals(requiredRole)) {
            hasRole = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        }

        if (!hasRole) {
            log.warn("权限不足: 需要角色 {}, 当前角色: {}", requiredRole, auth.getAuthorities());
            return Result.error(403, "权限不足，需要 " + requiredRole + " 角色");
        }

        return joinPoint.proceed();
    }
}

package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.exception.ForbiddenException;
import com.pettrail.pettrailbackend.exception.UnauthorizedException;
import com.pettrail.pettrailbackend.service.AdminService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BaseController {

    @Autowired
    private AdminService adminService;

    protected Long requireLogin() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("用户未登录");
        }
        return userId;
    }

    protected void requireExportPermission() {
        log.info("requireExportPermission called");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("未认证");
            throw new ForbiddenException("未登录");
        }

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        log.info("isSuperAdmin: {}, authorities: {}", isSuperAdmin, auth.getAuthorities());
        if (isSuperAdmin) return;

        Long adminId = UserContext.getCurrentUserId();
        log.info("adminId from UserContext: {}", adminId);
        if (adminId == null) {
            throw new ForbiddenException("未登录");
        }

        if (!adminService.hasExportPermission(adminId)) {
            log.warn("未找到export权限，adminId={}", adminId);
            throw new ForbiddenException("权限不足，需要导出权限");
        }
    }
}

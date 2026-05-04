package com.pettrail.pettrailbackend.controller.admin;

import com.pettrail.pettrailbackend.exception.ForbiddenException;
import com.pettrail.pettrailbackend.service.AdminService;
import com.pettrail.pettrailbackend.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class BaseAdminController {

    @Autowired
    private AdminService adminService;

    protected Long requireLogin() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new com.pettrail.pettrailbackend.exception.UnauthorizedException("用户未登录");
        }
        return userId;
    }

    protected void requireExportPermission(Long menuId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("未登录");
        }

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) {
            return;
        }

        Long adminId = UserContext.getCurrentUserId();
        if (adminId == null) {
            throw new ForbiddenException("未登录");
        }

        if (!adminService.hasExportPermission(adminId, menuId)) {
            throw new ForbiddenException("权限不足，需要导出权限");
        }
    }
}

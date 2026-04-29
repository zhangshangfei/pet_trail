package com.pettrail.pettrailbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.Admin;
import com.pettrail.pettrailbackend.entity.SysRoleMenu;
import com.pettrail.pettrailbackend.exception.ForbiddenException;
import com.pettrail.pettrailbackend.exception.UnauthorizedException;
import com.pettrail.pettrailbackend.mapper.AdminMapper;
import com.pettrail.pettrailbackend.mapper.SysRoleMenuMapper;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class BaseController implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
        log.info("BaseController applicationContext initialized: {}", context != null);
    }

    @PostConstruct
    public void init() {
        log.info("BaseController init called, applicationContext: {}", applicationContext);
    }

    private static AdminMapper getAdminMapper() {
        if (applicationContext == null) {
            log.error("applicationContext is NULL!");
            throw new IllegalStateException("applicationContext not initialized");
        }
        return applicationContext.getBean(AdminMapper.class);
    }

    private static SysRoleMenuMapper getSysRoleMenuMapper() {
        if (applicationContext == null) {
            log.error("applicationContext is NULL!");
            throw new IllegalStateException("applicationContext not initialized");
        }
        return applicationContext.getBean(SysRoleMenuMapper.class);
    }

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

        AdminMapper adminMapper = getAdminMapper();
        Admin admin = adminMapper.selectById(adminId);
        log.info("admin from DB: id={}, roleId={}", admin != null ? admin.getId() : null, admin != null ? admin.getRoleId() : null);
        if (admin == null || admin.getRoleId() == null) {
            throw new ForbiddenException("权限不足");
        }

        SysRoleMenuMapper sysRoleMenuMapper = getSysRoleMenuMapper();
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, admin.getRoleId()));
        log.info("roleMenus count: {}, roleId: {}", roleMenus.size(), admin.getRoleId());

        for (SysRoleMenu rm : roleMenus) {
            log.info("  menuId={}, buttons={}", rm.getMenuId(), rm.getButtons());
            if (rm.getButtons() != null && !rm.getButtons().isEmpty()) {
                for (String b : rm.getButtons().split(",")) {
                    if ("export".equals(b.trim())) {
                        log.info("找到export权限");
                        return;
                    }
                }
            }
        }

        log.warn("未找到export权限，roleId={}", admin.getRoleId());
        throw new ForbiddenException("权限不足，需要导出权限");
    }
}

package com.pettrail.pettrailbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.Admin;
import com.pettrail.pettrailbackend.entity.SysRoleMenu;
import com.pettrail.pettrailbackend.exception.ForbiddenException;
import com.pettrail.pettrailbackend.exception.UnauthorizedException;
import com.pettrail.pettrailbackend.mapper.AdminMapper;
import com.pettrail.pettrailbackend.mapper.SysRoleMenuMapper;
import com.pettrail.pettrailbackend.util.UserContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class BaseController implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    @PostConstruct
    public void init() {
        // 触发 applicationContext 初始化
    }

    private static AdminMapper getAdminMapper() {
        return applicationContext.getBean(AdminMapper.class);
    }

    private static SysRoleMenuMapper getSysRoleMenuMapper() {
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("未登录");
        }

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) return;

        Long adminId = UserContext.getCurrentUserId();
        if (adminId == null) {
            throw new ForbiddenException("未登录");
        }

        AdminMapper adminMapper = getAdminMapper();
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null || admin.getRoleId() == null) {
            throw new ForbiddenException("权限不足");
        }

        SysRoleMenuMapper sysRoleMenuMapper = getSysRoleMenuMapper();
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, admin.getRoleId()));

        for (SysRoleMenu rm : roleMenus) {
            if (rm.getButtons() != null && !rm.getButtons().isEmpty()) {
                for (String b : rm.getButtons().split(",")) {
                    if ("export".equals(b.trim())) return;
                }
            }
        }

        throw new ForbiddenException("权限不足，需要导出权限");
    }
}

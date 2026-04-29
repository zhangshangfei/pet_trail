package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.Admin;
import com.pettrail.pettrailbackend.entity.SysRoleMenu;
import com.pettrail.pettrailbackend.exception.ForbiddenException;
import com.pettrail.pettrailbackend.mapper.AdminMapper;
import com.pettrail.pettrailbackend.mapper.SysRoleMenuMapper;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class BaseAdminController {

    @Autowired
    protected AdminMapper adminMapper;

    @Autowired
    protected SysRoleMenuMapper sysRoleMenuMapper;

    @PostConstruct
    public void init() {
        log.info("BaseAdminController init, adminMapper: {}, sysRoleMenuMapper: {}",
                adminMapper != null, sysRoleMenuMapper != null);
    }

    protected Long requireLogin() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new com.pettrail.pettrailbackend.exception.UnauthorizedException("用户未登录");
        }
        return userId;
    }

    protected void requireExportPermission(Long menuId) {
        log.info("requireExportPermission menuId: {}", menuId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("未认证");
            throw new ForbiddenException("未登录");
        }

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        log.info("isSuperAdmin: {}", isSuperAdmin);
        if (isSuperAdmin) {
            log.info("超级管理员，放行");
            return;
        }

        Long adminId = UserContext.getCurrentUserId();
        log.info("adminId from UserContext: {}", adminId);
        if (adminId == null) {
            throw new ForbiddenException("未登录");
        }

        Admin admin = adminMapper.selectById(adminId);
        log.info("admin from DB: id={}, roleId={}", admin != null ? admin.getId() : null, admin != null ? admin.getRoleId() : null);
        if (admin == null || admin.getRoleId() == null) {
            throw new ForbiddenException("权限不足");
        }

        SysRoleMenu roleMenu = sysRoleMenuMapper.selectOne(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, admin.getRoleId())
                        .eq(SysRoleMenu::getMenuId, menuId));
        log.info("roleMenu: menuId={}, buttons={}", roleMenu != null ? roleMenu.getMenuId() : null, roleMenu != null ? roleMenu.getButtons() : null);

        if (roleMenu == null || roleMenu.getButtons() == null || roleMenu.getButtons().isEmpty()) {
            log.warn("菜单 {} 无任何按钮权限", menuId);
            throw new ForbiddenException("权限不足，需要导出权限");
        }

        boolean hasExport = false;
        for (String b : roleMenu.getButtons().split(",")) {
            if ("export".equals(b.trim())) {
                hasExport = true;
                break;
            }
        }

        if (!hasExport) {
            log.warn("菜单 {} 没有export权限", menuId);
            throw new ForbiddenException("权限不足，需要导出权限");
        }

        log.info("菜单 {} 有export权限，放行", menuId);
    }
}

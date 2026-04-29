package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.Admin;
import com.pettrail.pettrailbackend.entity.SysRoleMenu;
import com.pettrail.pettrailbackend.exception.ForbiddenException;
import com.pettrail.pettrailbackend.mapper.AdminMapper;
import com.pettrail.pettrailbackend.mapper.SysRoleMenuMapper;
import com.pettrail.pettrailbackend.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class BaseAdminController {

    @Autowired
    protected AdminMapper adminMapper;

    @Autowired
    protected SysRoleMenuMapper sysRoleMenuMapper;

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

        Admin admin = adminMapper.selectById(adminId);
        if (admin == null || admin.getRoleId() == null) {
            throw new ForbiddenException("权限不足");
        }

        SysRoleMenu roleMenu = sysRoleMenuMapper.selectOne(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, admin.getRoleId())
                        .eq(SysRoleMenu::getMenuId, menuId));

        if (roleMenu == null || roleMenu.getButtons() == null || roleMenu.getButtons().isEmpty()) {
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
            throw new ForbiddenException("权限不足，需要导出权限");
        }
    }
}

package com.pettrail.pettrailbackend.annotation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Admin;
import com.pettrail.pettrailbackend.entity.SysRoleMenu;
import com.pettrail.pettrailbackend.mapper.AdminMapper;
import com.pettrail.pettrailbackend.mapper.SysRoleMenuMapper;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequireButtonAspect {

    private final AdminMapper adminMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    @Around("@annotation(requireButton)")
    public Object checkButton(ProceedingJoinPoint joinPoint, RequireButton requireButton) throws Throwable {
        String buttonCode = requireButton.value();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Result.error(401, "未登录");
        }

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) {
            return joinPoint.proceed();
        }

        Long adminId = UserContext.getCurrentUserId();
        if (adminId == null) {
            return Result.error(401, "未登录");
        }

        Admin admin = adminMapper.selectById(adminId);
        if (admin == null || admin.getRoleId() == null) {
            return Result.error(403, "权限不足，需要 " + buttonCode + " 权限");
        }

        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, admin.getRoleId()));

        boolean hasButton = false;
        for (SysRoleMenu rm : roleMenus) {
            if (rm.getButtons() != null && !rm.getButtons().isEmpty()) {
                for (String b : rm.getButtons().split(",")) {
                    if (b.trim().equals(buttonCode)) {
                        hasButton = true;
                        break;
                    }
                }
            }
            if (hasButton) break;
        }

        if (!hasButton) {
            log.warn("按钮权限不足: 需要 {}, 管理员ID: {}", buttonCode, adminId);
            return Result.error(403, "权限不足，需要 " + buttonCode + " 权限");
        }

        return joinPoint.proceed();
    }
}

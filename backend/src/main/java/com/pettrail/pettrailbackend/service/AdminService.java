package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.AdminVO;
import com.pettrail.pettrailbackend.entity.*;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.AdminMapper;
import com.pettrail.pettrailbackend.mapper.MerchantMapper;
import com.pettrail.pettrailbackend.mapper.SysRoleMapper;
import com.pettrail.pettrailbackend.mapper.SysRoleMenuMapper;
import com.pettrail.pettrailbackend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper adminMapper;
    private final MerchantMapper merchantMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Map<String, Object> login(String username, String password) {
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, username)
                        .eq(Admin::getStatus, 1)
        );

        if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        admin.setLastLoginAt(LocalDateTime.now());
        adminMapper.updateById(admin);

        SysRole role = admin.getRoleId() != null ? sysRoleMapper.selectById(admin.getRoleId()) : null;
        String roleCode = role != null ? role.getCode() : "";
        String token = jwtUtil.generateToken(admin.getId(), "admin:" + roleCode);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("admin", convertToVO(admin));
        return result;
    }

    public AdminVO getProfile(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        return convertToVO(admin);
    }

    public void initDefaultAdmin() {
        Long count = adminMapper.selectCount(null);
        if (count == 0) {
            SysRole superAdminRole = sysRoleMapper.selectOne(
                    new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, "SUPER_ADMIN"));
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("超级管理员");
            admin.setRoleId(superAdminRole != null ? superAdminRole.getId() : 1L);
            admin.setStatus(1);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            adminMapper.insert(admin);
            log.info("初始化默认管理员账号: admin / admin123");
        }
    }

    private AdminVO convertToVO(Admin admin) {
        AdminVO vo = new AdminVO();
        vo.setId(admin.getId());
        vo.setUsername(admin.getUsername());
        vo.setNickname(admin.getNickname());
        vo.setAvatar(admin.getAvatar());
        vo.setRoleId(admin.getRoleId());
        vo.setMerchantId(admin.getMerchantId());
        vo.setStatus(admin.getStatus());
        vo.setLastLoginAt(admin.getLastLoginAt());
        vo.setCreatedAt(admin.getCreatedAt());

        if (admin.getRoleId() != null) {
            SysRole role = sysRoleMapper.selectById(admin.getRoleId());
            if (role != null) {
                vo.setRoleName(role.getName());
                vo.setRoleCode(role.getCode());
            }
            List<String> perms = new ArrayList<>();
            List<String> btns = new ArrayList<>();
            List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                    new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, admin.getRoleId()));
            for (SysRoleMenu rm : roleMenus) {
                if (rm.getButtons() != null && !rm.getButtons().isEmpty()) {
                    for (String b : rm.getButtons().split(",")) {
                        String trimmed = b.trim();
                        if (!trimmed.isEmpty()) {
                            btns.add(trimmed);
                            perms.add(trimmed);
                        }
                    }
                }
            }
            vo.setPermissions(perms);
            vo.setButtons(btns);
        }

        if (admin.getMerchantId() != null) {
            Merchant merchant = merchantMapper.selectById(admin.getMerchantId());
            if (merchant != null) {
                vo.setMerchantName(merchant.getName());
            }
        }

        return vo;
    }

    public Page<Admin> adminListAdmins(int page, int size) {
        Page<Admin> pageParam = new Page<>(page, size);
        return adminMapper.selectPage(pageParam, new LambdaQueryWrapper<Admin>().orderByDesc(Admin::getCreatedAt));
    }

    @Transactional(rollbackFor = Exception.class)
    public Admin adminCreateAdmin(Admin admin) {
        Long count = adminMapper.selectCount(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, admin.getUsername()));
        if (count > 0) {
            throw new BusinessException(409, "用户名已存在");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword() != null ? admin.getPassword() : "admin123"));
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        adminMapper.insert(admin);
        return admin;
    }

    @Transactional(rollbackFor = Exception.class)
    public Admin adminUpdateAdmin(Long id, Admin admin) {
        Admin existing = adminMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        admin.setId(id);
        if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        } else {
            admin.setPassword(existing.getPassword());
        }
        admin.setUpdatedAt(LocalDateTime.now());
        adminMapper.updateById(admin);
        return admin;
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateAdminStatus(Long id, Integer status) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        admin.setStatus(status);
        admin.setUpdatedAt(LocalDateTime.now());
        adminMapper.updateById(admin);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminResetPassword(Long id, String newPassword) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        admin.setPassword(passwordEncoder.encode(newPassword != null ? newPassword : "admin123"));
        admin.setUpdatedAt(LocalDateTime.now());
        adminMapper.updateById(admin);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminChangePassword(Long id, String oldPassword, String newPassword) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
        admin.setUpdatedAt(LocalDateTime.now());
        adminMapper.updateById(admin);
    }
}

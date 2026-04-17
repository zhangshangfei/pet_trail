package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.AdminVO;
import com.pettrail.pettrailbackend.entity.Admin;
import com.pettrail.pettrailbackend.mapper.AdminMapper;
import com.pettrail.pettrailbackend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Map<String, Object> login(String username, String password) {
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, username)
                        .eq(Admin::getStatus, 1)
        );

        if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        admin.setLastLoginAt(LocalDateTime.now());
        adminMapper.updateById(admin);

        String token = jwtUtil.generateToken(admin.getId(), "admin:" + admin.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("admin", convertToVO(admin));
        return result;
    }

    public AdminVO getProfile(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        return convertToVO(admin);
    }

    public void initDefaultAdmin() {
        Long count = adminMapper.selectCount(null);
        if (count == 0) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("超级管理员");
            admin.setRole("SUPER_ADMIN");
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
        vo.setRole(admin.getRole());
        vo.setStatus(admin.getStatus());
        vo.setLastLoginAt(admin.getLastLoginAt());
        vo.setCreatedAt(admin.getCreatedAt());
        return vo;
    }
}

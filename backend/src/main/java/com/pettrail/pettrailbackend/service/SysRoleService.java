package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.entity.SysRole;
import com.pettrail.pettrailbackend.entity.SysRoleMenu;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.SysRoleMapper;
import com.pettrail.pettrailbackend.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    public List<SysRole> listAll() {
        return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
    }

    public Page<SysRole> listPage(int page, int size) {
        return sysRoleMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
    }

    public SysRole getById(Long id) {
        return sysRoleMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysRole create(SysRole role) {
        Long count = sysRoleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, role.getCode()));
        if (count > 0) {
            throw new BusinessException(409, "角色编码已存在");
        }
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        sysRoleMapper.insert(role);
        return role;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysRole update(Long id, SysRole role) {
        SysRole existing = sysRoleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "角色不存在");
        }
        role.setId(id);
        role.setUpdatedAt(LocalDateTime.now());
        sysRoleMapper.updateById(role);
        return role;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        sysRoleMapper.deleteById(id);
        sysRoleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        role.setStatus(status);
        role.setUpdatedAt(LocalDateTime.now());
        sysRoleMapper.updateById(role);
    }

    public List<SysRoleMenu> getRoleMenus(Long roleId) {
        return sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveRoleMenus(Long roleId, List<SysRoleMenu> menuPermissions) {
        sysRoleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        java.util.Set<Long> insertedMenuIds = new java.util.HashSet<>();
        for (SysRoleMenu rm : menuPermissions) {
            if (rm.getMenuId() == null || !insertedMenuIds.add(rm.getMenuId())) {
                continue;
            }
            rm.setRoleId(roleId);
            rm.setId(null);
            sysRoleMenuMapper.insert(rm);
        }
    }
}

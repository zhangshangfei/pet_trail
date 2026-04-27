package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.SysMenu;
import com.pettrail.pettrailbackend.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper sysMenuMapper;

    public List<SysMenu> getAllMenus() {
        return sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, 1)
                        .orderByAsc(SysMenu::getSortOrder)
        );
    }

    public List<Map<String, Object>> getMenuTree() {
        List<SysMenu> all = getAllMenus();
        return buildTree(all, 0L);
    }

    public List<Map<String, Object>> getUserMenuTree(String permissions) {
        List<SysMenu> all = getAllMenus();
        List<String> permList = permissions != null && !permissions.isEmpty()
                ? List.of(permissions.split(",")) : List.of();

        List<SysMenu> filtered = all.stream()
                .filter(m -> m.getPermission() == null || m.getPermission().isEmpty() || permList.contains(m.getPermission()))
                .collect(Collectors.toList());

        List<SysMenu> visible = new ArrayList<>();
        for (SysMenu menu : filtered) {
            visible.add(menu);
            addParents(all, menu.getParentId(), visible);
        }

        List<SysMenu> deduped = visible.stream()
                .distinct()
                .collect(Collectors.toList());

        return buildTree(deduped, 0L);
    }

    private void addParents(List<SysMenu> all, Long parentId, List<SysMenu> visible) {
        if (parentId == null || parentId == 0) return;
        for (SysMenu m : all) {
            if (m.getId().equals(parentId)) {
                if (!visible.contains(m)) {
                    visible.add(m);
                }
                addParents(all, m.getParentId(), visible);
                break;
            }
        }
    }

    private List<Map<String, Object>> buildTree(List<SysMenu> menus, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                Map<String, Object> node = Map.of(
                        "id", menu.getId(),
                        "name", menu.getName() != null ? menu.getName() : "",
                        "path", menu.getPath() != null ? menu.getPath() : "",
                        "icon", menu.getIcon() != null ? menu.getIcon() : "",
                        "permission", menu.getPermission() != null ? menu.getPermission() : "",
                        "sortOrder", menu.getSortOrder() != null ? menu.getSortOrder() : 0,
                        "children", buildTree(menus, menu.getId())
                );
                tree.add(node);
            }
        }
        return tree;
    }

    public SysMenu createMenu(SysMenu menu) {
        menu.setCreatedAt(java.time.LocalDateTime.now());
        menu.setUpdatedAt(java.time.LocalDateTime.now());
        sysMenuMapper.insert(menu);
        return menu;
    }

    public SysMenu updateMenu(Long id, SysMenu menu) {
        menu.setId(id);
        menu.setUpdatedAt(java.time.LocalDateTime.now());
        sysMenuMapper.updateById(menu);
        return menu;
    }

    public void deleteMenu(Long id) {
        sysMenuMapper.deleteById(id);
    }

    public void updateMenuStatus(Long id, Integer status) {
        SysMenu menu = sysMenuMapper.selectById(id);
        if (menu != null) {
            menu.setStatus(status);
            menu.setUpdatedAt(java.time.LocalDateTime.now());
            sysMenuMapper.updateById(menu);
        }
    }
}

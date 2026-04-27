package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.SysMenu;
import com.pettrail.pettrailbackend.entity.SysRoleMenu;
import com.pettrail.pettrailbackend.mapper.SysMenuMapper;
import com.pettrail.pettrailbackend.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

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

    public List<Map<String, Object>> getUserMenuTree(Long roleId) {
        if (roleId == null) return List.of();

        SysRoleMenu check = sysRoleMenuMapper.selectOne(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId).last("LIMIT 1"));
        boolean isSuperAdmin = false;
        if (roleId != null) {
            List<SysRoleMenu> allRoleMenus = sysRoleMenuMapper.selectList(
                    new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
            Set<Long> menuIds = allRoleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
            List<SysMenu> all = getAllMenus();
            isSuperAdmin = all.stream().allMatch(m -> menuIds.contains(m.getId()));
        }

        List<SysMenu> all = getAllMenus();
        if (isSuperAdmin) {
            return buildTreeWithButtons(all, 0L, Collections.emptyMap());
        }

        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        Map<Long, String> menuButtonMap = new HashMap<>();
        Set<Long> menuIds = new HashSet<>();
        for (SysRoleMenu rm : roleMenus) {
            menuIds.add(rm.getMenuId());
            menuButtonMap.put(rm.getMenuId(), rm.getButtons() != null ? rm.getButtons() : "");
        }

        List<SysMenu> visible = new ArrayList<>();
        for (SysMenu menu : all) {
            if (menuIds.contains(menu.getId())) {
                visible.add(menu);
            }
            addParents(all, menu.getParentId(), visible, menuIds);
        }

        List<SysMenu> deduped = visible.stream().distinct().collect(Collectors.toList());
        return buildTreeWithButtons(deduped, 0L, menuButtonMap);
    }

    private void addParents(List<SysMenu> all, Long parentId, List<SysMenu> visible, Set<Long> menuIds) {
        if (parentId == null || parentId == 0) return;
        for (SysMenu m : all) {
            if (m.getId().equals(parentId)) {
                if (!visible.contains(m)) {
                    visible.add(m);
                }
                addParents(all, m.getParentId(), visible, menuIds);
                break;
            }
        }
    }

    private List<Map<String, Object>> buildTree(List<SysMenu> menus, Long parentId) {
        return buildTreeWithButtons(menus, parentId, Collections.emptyMap());
    }

    private List<Map<String, Object>> buildTreeWithButtons(List<SysMenu> menus, Long parentId, Map<Long, String> menuButtonMap) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                List<String> btnList = new ArrayList<>();
                if (menu.getButtons() != null && !menu.getButtons().isEmpty()) {
                    btnList = Arrays.stream(menu.getButtons().split(","))
                            .map(String::trim).filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                }

                String roleButtons = menuButtonMap.getOrDefault(menu.getId(), null);
                List<String> activeButtons = new ArrayList<>();
                if (roleButtons != null && !roleButtons.isEmpty()) {
                    activeButtons = Arrays.stream(roleButtons.split(","))
                            .map(String::trim).filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                } else if (menuButtonMap.isEmpty()) {
                    activeButtons = btnList;
                }

                Map<String, Object> node = new LinkedHashMap<>();
                node.put("id", menu.getId());
                node.put("name", menu.getName() != null ? menu.getName() : "");
                node.put("path", menu.getPath() != null ? menu.getPath() : "");
                node.put("icon", menu.getIcon() != null ? menu.getIcon() : "");
                node.put("permission", menu.getPermission() != null ? menu.getPermission() : "");
                node.put("buttons", btnList);
                node.put("activeButtons", activeButtons);
                node.put("sortOrder", menu.getSortOrder() != null ? menu.getSortOrder() : 0);
                node.put("children", buildTreeWithButtons(menus, menu.getId(), menuButtonMap));
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

    public void batchSort(List<Map<String, Object>> sortList) {
        for (Map<String, Object> item : sortList) {
            Long id = Long.valueOf(item.get("id").toString());
            Integer sortOrder = Integer.valueOf(item.get("sortOrder").toString());
            Long parentId = item.containsKey("parentId") && item.get("parentId") != null
                    ? Long.valueOf(item.get("parentId").toString()) : null;
            SysMenu menu = sysMenuMapper.selectById(id);
            if (menu != null) {
                menu.setSortOrder(sortOrder);
                if (parentId != null) {
                    menu.setParentId(parentId);
                }
                menu.setUpdatedAt(java.time.LocalDateTime.now());
                sysMenuMapper.updateById(menu);
            }
        }
    }
}

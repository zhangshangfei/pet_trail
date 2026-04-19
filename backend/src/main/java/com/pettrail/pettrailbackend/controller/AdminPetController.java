package com.pettrail.pettrailbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.mapper.PetMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/pets")
@RequiredArgsConstructor
@Tag(name = "Admin-宠物管理", description = "后台宠物管理")
public class AdminPetController {

    private final PetMapper petMapper;
    private final UserMapper userMapper;

    @GetMapping
    @Operation(summary = "分页查询宠物列表")
    public Result<Page<Pet>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Long userId) {
        Page<Pet> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Pet::getName, keyword);
        }
        if (category != null) {
            wrapper.eq(Pet::getCategory, category);
        }
        if (userId != null) {
            wrapper.eq(Pet::getUserId, userId);
        }
        wrapper.orderByDesc(Pet::getCreatedAt);

        Page<Pet> result = petMapper.selectPage(pageParam, wrapper);
        fillUserNickname(result);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取宠物详情")
    public Result<Pet> getDetail(@PathVariable Long id) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            return Result.error(404, "宠物不存在");
        }
        if (pet.getUserId() != null) {
            User user = userMapper.selectById(pet.getUserId());
            if (user != null) {
                pet.setUserNickname(user.getNickname());
            }
        }
        return Result.success(pet);
    }

    @DeleteMapping("/{id}")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "pet", action = "delete", detail = "删除宠物")
    @Operation(summary = "删除宠物")
    @RequireRole("SUPER_ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            return Result.error(404, "宠物不存在");
        }
        petMapper.deleteById(id);
        return Result.success(null);
    }

    private void fillUserNickname(Page<Pet> result) {
        Set<Long> userIds = result.getRecords().stream()
                .map(Pet::getUserId)
                .filter(uid -> uid != null)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        Map<Long, String> nicknameMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u.getNickname() != null ? u.getNickname() : "用户" + u.getId(), (a, b) -> a));
        for (Pet pet : result.getRecords()) {
            if (pet.getUserId() != null) {
                pet.setUserNickname(nicknameMap.getOrDefault(pet.getUserId(), "未知用户"));
            }
        }
    }
}

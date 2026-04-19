package com.pettrail.pettrailbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.mapper.PetMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pets")
@RequiredArgsConstructor
@Tag(name = "Admin-宠物管理", description = "后台宠物管理")
public class AdminPetController {

    private final PetMapper petMapper;

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

        return Result.success(petMapper.selectPage(pageParam, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取宠物详情")
    public Result<Pet> getDetail(@PathVariable Long id) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            return Result.error(404, "宠物不存在");
        }
        return Result.success(pet);
    }

    @DeleteMapping("/{id}")
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
}

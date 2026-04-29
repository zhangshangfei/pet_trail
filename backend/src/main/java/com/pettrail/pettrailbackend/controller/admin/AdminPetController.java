package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.annotation.RequireRole;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pets")
@RequiredArgsConstructor
@Tag(name = "Admin-宠物管理", description = "后台宠物管理")
public class AdminPetController extends BaseAdminController {

    private final PetService petService;

    @GetMapping
    @Operation(summary = "分页查询宠物列表")
    public Result<Page<Pet>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Long userId) {
        return Result.success(petService.adminListPets(page, size, keyword, category, userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取宠物详情")
    public Result<Pet> getDetail(@PathVariable Long id) {
        return Result.success(petService.adminGetPetDetail(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新宠物信息")
    public Result<Pet> update(@PathVariable Long id, @RequestBody Pet pet) {
        return Result.success(petService.adminUpdatePet(id, pet));
    }

    @DeleteMapping("/{id}")
    @com.pettrail.pettrailbackend.annotation.OperationLog(module = "pet", action = "delete", detail = "删除宠物")
    @Operation(summary = "删除宠物")
    @RequireRole("SUPER_ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        petService.adminDeletePet(id);
        return Result.success(null);
    }
}

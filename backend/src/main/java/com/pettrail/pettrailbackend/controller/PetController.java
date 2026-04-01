package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.exception.ForbiddenException;
import com.pettrail.pettrailbackend.exception.NotFoundException;
import com.pettrail.pettrailbackend.service.PetService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    /**
     * 获取用户所有宠物
     */
    @GetMapping
    public Result<List<Pet>> listPets() {
        log.info("获取宠物列表");
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("用户未登录");
        }
        List<Pet> pets = petService.listByUserId(userId);
        return Result.success(pets);
    }

    /**
     * 创建宠物
     */
    @PostMapping
    public Result<Pet> createPet(
            @RequestParam String name,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) Integer sterilized,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) LocalDate birthday,
            @RequestParam(required = false) String avatar,
            @RequestParam(required = false) BigDecimal weight,
            @RequestParam(required = false) String color) {
        log.info("创建宠物：name={}", name);
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("用户未登录");
        }
        Pet pet = petService.createPet(userId, name, breed, gender, sterilized, category, birthday, avatar, weight, color);
        return Result.success(pet);
    }

    /**
     * 获取宠物详情
     */
    @GetMapping("/{id}")
    public Result<Pet> getPet(@PathVariable Long id) {
        log.info("获取宠物详情：id={}", id);
        validatePetOwnership(id);
        Pet pet = petService.getPetDetail(id);
        return Result.success(pet);
    }

    /**
     * 更新宠物信息
     */
    @PutMapping("/{id}")
    public Result<Pet> updatePet(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) Integer sterilized,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) LocalDate birthday,
            @RequestParam(required = false) String avatar,
            @RequestParam(required = false) BigDecimal weight,
            @RequestParam(required = false) String color) {
        log.info("更新宠物信息：id={}", id);
        validatePetOwnership(id);
        Pet pet = petService.updatePet(id, name, breed, gender, sterilized, category, birthday, avatar, weight, color);
        return Result.success(pet);
    }

    /**
     * 删除宠物
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePet(@PathVariable Long id) {
        log.info("删除宠物：id={}", id);
        validatePetOwnership(id);
        petService.deletePet(id);
        return Result.success();
    }

    /**
     * 更新宠物体重
     */
    @PutMapping("/{id}/weight")
    public Result<Pet> updatePetWeight(
            @PathVariable Long id,
            @RequestParam BigDecimal weight) {
        log.info("更新宠物体重：id={}, weight={}", id, weight);
        validatePetOwnership(id);
        Pet pet = petService.updatePetWeight(id, weight);
        return Result.success(pet);
    }

    /**
     * 校验当前登录用户是否拥有该宠物
     */
    private void validatePetOwnership(Long petId) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("用户未登录");
        }
        Pet pet = petService.getPetDetail(petId);
        if (pet == null) {
            throw new NotFoundException("宠物不存在");
        }
        if (!currentUserId.equals(pet.getUserId())) {
            throw new ForbiddenException("无权限操作该宠物");
        }
    }
}

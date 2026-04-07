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
    public Result<Pet> createPet(@RequestBody Pet pet) {
        log.info("创建宠物：name={}", pet.getName());
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("用户未登录");
        }
        Pet createdPet = petService.createPet(
            userId, 
            pet.getName(), 
            pet.getBreed(), 
            pet.getGender(), 
            pet.getSterilized(), 
            pet.getCategory(), 
            pet.getBirthday(), 
            pet.getAvatar(), 
            pet.getWeight(), 
            pet.getColor()
        );
        return Result.success(createdPet);
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
            @RequestBody Pet pet) {
        log.info("更新宠物信息：id={}", id);
        validatePetOwnership(id);
        Pet updatedPet = petService.updatePet(
            id, 
            pet.getName(), 
            pet.getBreed(), 
            pet.getGender(), 
            pet.getSterilized(), 
            pet.getCategory(), 
            pet.getBirthday(), 
            pet.getAvatar(), 
            pet.getWeight(), 
            pet.getColor()
        );
        return Result.success(updatedPet);
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
            @RequestBody java.util.Map<String, BigDecimal> requestBody) {
        log.info("更新宠物体重：id={}, weight={}", id, requestBody.get("weight"));
        validatePetOwnership(id);
        BigDecimal weight = requestBody.get("weight");
        if (weight == null) {
            throw new IllegalArgumentException("体重参数不能为空");
        }
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

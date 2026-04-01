package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.service.PetService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Map<String, Object> listPets() {
        log.info("获取宠物列表");
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = UserContext.getCurrentUserId();
            List<Pet> pets = petService.listByUserId(userId);
            result.put("success", true);
            result.put("data", pets);
            result.put("message", "获取成功");
        } catch (Exception e) {
            log.error("获取宠物列表失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 创建宠物
     */
    @PostMapping
    public Map<String, Object> createPet(
            @RequestParam String name,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) Integer sterilized,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) LocalDate birthday,
            @RequestParam(required = false) String avatar,
            @RequestParam(required = false) BigDecimal weight,
            @RequestParam(required = false) String color) {
        log.info("创建宠物: name={}", name);
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = UserContext.getCurrentUserId();
            Pet pet = petService.createPet(userId, name, breed, gender, sterilized, category, birthday, avatar, weight, color);
            result.put("success", true);
            result.put("data", pet);
            result.put("message", "创建成功");
        } catch (Exception e) {
            log.error("创建宠物失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取宠物详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getPet(
            @PathVariable Long id) {
        log.info("获取宠物详情: id={}", id);
        Map<String, Object> result = new HashMap<>();
        try {
            validatePetOwnership(id);
            Pet pet = petService.getPetDetail(id);
            result.put("success", true);
            result.put("data", pet);
            result.put("message", "获取成功");
        } catch (Exception e) {
            log.error("获取宠物详情失败: id={}, error={}", id, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新宠物信息
     */
    @PutMapping("/{id}")
    public Map<String, Object> updatePet(
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
        log.info("更新宠物信息: id={}", id);
        Map<String, Object> result = new HashMap<>();
        try {
            validatePetOwnership(id);
            Pet pet = petService.updatePet(id, name, breed, gender, sterilized, category, birthday, avatar, weight, color);
            result.put("success", true);
            result.put("data", pet);
            result.put("message", "更新成功");
        } catch (Exception e) {
            log.error("更新宠物信息失败: id={}, error={}", id, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除宠物
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deletePet(
            @PathVariable Long id) {
        log.info("删除宠物: id={}", id);
        Map<String, Object> result = new HashMap<>();
        try {
            validatePetOwnership(id);
            petService.deletePet(id);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            log.error("删除宠物失败: id={}, error={}", id, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新宠物体重
     */
    @PutMapping("/{id}/weight")
    public Map<String, Object> updatePetWeight(
            @PathVariable Long id,
            @RequestParam BigDecimal weight) {
        log.info("更新宠物体重: id={}, weight={}", id, weight);
        Map<String, Object> result = new HashMap<>();
        try {
            validatePetOwnership(id);
            Pet pet = petService.updatePetWeight(id, weight);
            result.put("success", true);
            result.put("data", pet);
            result.put("message", "更新成功");
        } catch (Exception e) {
            log.error("更新宠物体重失败: id={}, error={}", id, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 校验当前登录用户是否拥有该宠物
     */
    private void validatePetOwnership(Long petId) {
        Long currentUserId = UserContext.getCurrentUserId();
        Pet pet = petService.getPetDetail(petId);
        if (!currentUserId.equals(pet.getUserId())) {
            throw new RuntimeException("无权限操作该宠物");
        }
    }

}

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
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController extends BaseController {

    private final PetService petService;

    @GetMapping
    public Result<List<Pet>> listPets() {
        Long userId = requireLogin();
        return Result.success(petService.listByUserId(userId));
    }

    @PostMapping
    public Result<Pet> createPet(@RequestBody Pet pet) {
        Long userId = requireLogin();
        Pet createdPet = petService.createPet(
            userId, pet.getName(), pet.getBreed(), pet.getGender(), pet.getSterilized(),
            pet.getCategory(), pet.getBirthday(), pet.getAvatar(), pet.getWeight(), pet.getColor());
        return Result.success(createdPet);
    }

    @GetMapping("/{id}")
    public Result<Pet> getPet(@PathVariable Long id) {
        validatePetOwnership(id);
        return Result.success(petService.getPetDetail(id));
    }

    @PutMapping("/{id}")
    public Result<Pet> updatePet(@PathVariable Long id, @RequestBody Pet pet) {
        validatePetOwnership(id);
        Pet updatedPet = petService.updatePet(
            id, pet.getName(), pet.getBreed(), pet.getGender(), pet.getSterilized(),
            pet.getCategory(), pet.getBirthday(), pet.getAvatar(), pet.getWeight(), pet.getColor());
        return Result.success(updatedPet);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePet(@PathVariable Long id) {
        validatePetOwnership(id);
        petService.deletePet(id);
        return Result.success();
    }

    @PutMapping("/{id}/weight")
    public Result<Pet> updatePetWeight(@PathVariable Long id, @RequestBody java.util.Map<String, BigDecimal> requestBody) {
        validatePetOwnership(id);
        BigDecimal weight = requestBody.get("weight");
        if (weight == null) {
            throw new IllegalArgumentException("体重参数不能为空");
        }
        return Result.success(petService.updatePetWeight(id, weight));
    }

    private void validatePetOwnership(Long petId) {
        Long currentUserId = requireLogin();
        Pet pet = petService.getPetDetail(petId);
        if (pet == null) {
            throw new NotFoundException("宠物不存在");
        }
        if (!currentUserId.equals(pet.getUserId())) {
            throw new ForbiddenException("无权限操作该宠物");
        }
    }
}

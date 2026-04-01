package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetService extends ServiceImpl<PetMapper, Pet> {

    /**
     * 获取用户的宠物列表
     */
    public List<Pet> listByUserId(Long userId) {
        LambdaQueryWrapper<Pet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Pet::getUserId, userId);
        queryWrapper.orderByDesc(Pet::getCreatedAt);
        return this.list(queryWrapper);
    }

    /**
     * 创建宠物
     */
    @Transactional(rollbackFor = Exception.class)
    public Pet createPet(Long userId, String name, String breed, Integer gender,
                         Integer sterilized, Integer category,
                         LocalDate birthday, String avatar, BigDecimal weight, String color) {
        Pet pet = new Pet();
        pet.setUserId(userId);
        pet.setName(name);
        pet.setBreed(breed);
        pet.setGender(gender);
        pet.setSterilized(sterilized == null ? 0 : sterilized);
        pet.setCategory(category == null ? 0 : category);
        pet.setBirthday(birthday);
        pet.setAvatar(avatar);
        pet.setWeight(weight);
        pet.setColor(color);
        pet.setCreatedAt(LocalDateTime.now());
        pet.setUpdatedAt(LocalDateTime.now());

        this.save(pet);
        log.info("创建宠物成功: id={}, userId={}, name={}", pet.getId(), userId, name);
        return pet;
    }

    /**
     * 获取宠物详情
     */
    public Pet getPetById(Long petId) {
        Pet pet = this.getById(petId);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        return pet;
    }

    /**
     * 获取宠物的详细信息（包括用户ID）
     */
    public Pet getPetDetail(Long petId) {
        Pet pet = this.getById(petId);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        return pet;
    }

    /**
     * 更新宠物信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Pet updatePet(Long petId, String name, String breed, Integer gender,
                         Integer sterilized, Integer category,
                         LocalDate birthday, String avatar, BigDecimal weight, String color) {
        Pet pet = this.getById(petId);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }

        if (name != null) {
            pet.setName(name);
        }
        if (breed != null) {
            pet.setBreed(breed);
        }
        if (gender != null) {
            pet.setGender(gender);
        }
        if (sterilized != null) {
            pet.setSterilized(sterilized);
        }
        if (category != null) {
            pet.setCategory(category);
        }
        if (birthday != null) {
            pet.setBirthday(birthday);
        }
        if (avatar != null) {
            pet.setAvatar(avatar);
        }
        if (weight != null) {
            pet.setWeight(weight);
        }
        if (color != null) {
            pet.setColor(color);
        }

        pet.setUpdatedAt(LocalDateTime.now());
        this.updateById(pet);
        log.info("更新宠物信息成功: petId={}", petId);
        return pet;
    }

    /**
     * 删除宠物
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePet(Long petId) {
        Pet pet = this.getById(petId);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        this.removeById(petId);
        log.info("删除宠物成功: petId={}", petId);
    }

    /**
     * 更新宠物体重
     */
    @Transactional(rollbackFor = Exception.class)
    public Pet updatePetWeight(Long petId, BigDecimal weight) {
        Pet pet = this.getById(petId);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }
        pet.setWeight(weight);
        pet.setUpdatedAt(LocalDateTime.now());
        this.updateById(pet);
        log.info("更新宠物体重成功: petId={}, weight={}", petId, weight);
        return pet;
    }

}

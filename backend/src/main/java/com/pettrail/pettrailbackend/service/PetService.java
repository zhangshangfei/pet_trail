package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetService extends ServiceImpl<PetMapper, Pet> {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PET_DETAIL_PREFIX = "pet:detail:";
    private static final String PET_LIST_PREFIX = "pet:list:";
    private static final long PET_CACHE_TTL_MINUTES = 20;
    private static final long LIST_CACHE_TTL_MINUTES = 5;

    public List<Pet> listByUserId(Long userId) {
        String cacheKey = PET_LIST_PREFIX + userId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                List<Pet> result = (List<Pet>) cached;
                return result;
            }
        } catch (Exception e) {
            log.warn("宠物列表缓存读取异常: {}", e.getMessage());
        }

        LambdaQueryWrapper<Pet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Pet::getUserId, userId);
        queryWrapper.orderByDesc(Pet::getCreatedAt);
        List<Pet> pets = this.list(queryWrapper);

        try {
            redisTemplate.opsForValue().set(cacheKey, pets, LIST_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("宠物列表缓存写入异常: {}", e.getMessage());
        }

        return pets;
    }

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

        evictPetListCache(userId);
        return pet;
    }

    public Pet getPetById(Long petId) {
        String cacheKey = PET_DETAIL_PREFIX + petId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null && cached instanceof Pet) {
                return (Pet) cached;
            }
        } catch (Exception e) {
            log.warn("宠物详情缓存读取异常: {}", e.getMessage());
        }

        Pet pet = this.getById(petId);
        if (pet == null) {
            throw new BusinessException(404, "宠物不存在");
        }

        try {
            redisTemplate.opsForValue().set(cacheKey, pet, PET_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("宠物详情缓存写入异常: {}", e.getMessage());
        }

        return pet;
    }

    public Pet getPetDetail(Long petId) {
        return getPetById(petId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Pet updatePet(Long petId, String name, String breed, Integer gender,
                         Integer sterilized, Integer category,
                         LocalDate birthday, String avatar, BigDecimal weight, String color) {
        Pet pet = this.getById(petId);
        if (pet == null) {
            throw new BusinessException(404, "宠物不存在");
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

        try {
            redisTemplate.opsForValue().set(PET_DETAIL_PREFIX + petId, pet, PET_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            evictPetListCache(pet.getUserId());
        } catch (Exception e) {
            log.warn("宠物缓存更新异常: {}", e.getMessage());
        }

        return pet;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePet(Long petId) {
        Pet pet = this.getById(petId);
        if (pet == null) {
            throw new BusinessException(404, "宠物不存在");
        }
        this.removeById(petId);
        log.info("删除宠物成功: petId={}", petId);

        try {
            redisTemplate.delete(PET_DETAIL_PREFIX + petId);
            evictPetListCache(pet.getUserId());
        } catch (Exception e) {
            log.warn("宠物缓存清除异常: {}", e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Pet updatePetWeight(Long petId, BigDecimal weight) {
        Pet pet = this.getById(petId);
        if (pet == null) {
            throw new BusinessException(404, "宠物不存在");
        }
        pet.setWeight(weight);
        pet.setUpdatedAt(LocalDateTime.now());
        this.updateById(pet);
        log.info("更新宠物体重成功: petId={}, weight={}", petId, weight);

        try {
            redisTemplate.opsForValue().set(PET_DETAIL_PREFIX + petId, pet, PET_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("宠物缓存更新异常: {}", e.getMessage());
        }

        return pet;
    }

    private void evictPetListCache(Long userId) {
        try {
            redisTemplate.delete(PET_LIST_PREFIX + userId);
        } catch (Exception e) {
            log.warn("宠物列表缓存清除异常: {}", e.getMessage());
        }
    }
}

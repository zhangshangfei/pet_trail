package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.entity.Merchant;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantMapper merchantMapper;

    public Merchant adminGetMerchantDetail(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(404, "商户不存在");
        }
        return merchant;
    }

    public Page<Merchant> adminListMerchants(int page, int size, String keyword, String type, Integer status) {
        Page<Merchant> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Merchant::getName, keyword).or().like(Merchant::getContactName, keyword));
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Merchant::getType, type);
        }
        if (status != null) {
            wrapper.eq(Merchant::getStatus, status);
        }
        wrapper.orderByDesc(Merchant::getCreatedAt);
        return merchantMapper.selectPage(pageParam, wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public Merchant adminCreateMerchant(Merchant merchant) {
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.insert(merchant);
        return merchant;
    }

    @Transactional(rollbackFor = Exception.class)
    public Merchant adminUpdateMerchant(Long id, Merchant merchant) {
        Merchant existing = merchantMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "商户不存在");
        }
        merchant.setId(id);
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);
        return merchant;
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateMerchantStatus(Long id, Integer status) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(404, "商户不存在");
        }
        merchant.setStatus(status);
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteMerchant(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(404, "商户不存在");
        }
        merchantMapper.deleteById(id);
    }
}

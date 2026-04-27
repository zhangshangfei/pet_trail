package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.WxSubscribeAuthorization;
import com.pettrail.pettrailbackend.mapper.WxSubscribeAuthorizationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WxSubscribeAuthorizationService {

    private final WxSubscribeAuthorizationMapper authorizationMapper;

    @Transactional(rollbackFor = Exception.class)
    public void addCredits(Long userId, String templateType, int count) {
        WxSubscribeAuthorization auth = getOrCreate(userId, templateType);
        auth.setCredits(auth.getCredits() + count);
        auth.setUpdatedAt(LocalDateTime.now());
        authorizationMapper.updateById(auth);
        log.info("订阅消息授权积分增加: userId={}, templateType={}, +{}, 当前剩余={}",
                userId, templateType, count, auth.getCredits());
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean consumeCredit(Long userId, String templateType) {
        WxSubscribeAuthorization auth = findByUserAndType(userId, templateType);
        if (auth == null || auth.getCredits() <= 0) {
            log.debug("订阅消息授权积分不足: userId={}, templateType={}", userId, templateType);
            return false;
        }
        int rows = authorizationMapper.updateCredits(userId, templateType, -1);
        if (rows > 0) {
            authorizationMapper.updateUsedCredits(userId, templateType);
            log.info("订阅消息授权积分消费: userId={}, templateType={}", userId, templateType);
            return true;
        }
        return false;
    }

    public int getCredits(Long userId, String templateType) {
        WxSubscribeAuthorization auth = findByUserAndType(userId, templateType);
        return auth != null ? auth.getCredits() : 0;
    }

    public List<WxSubscribeAuthorization> getUserAuthorizations(Long userId) {
        return authorizationMapper.selectList(
                new LambdaQueryWrapper<WxSubscribeAuthorization>()
                        .eq(WxSubscribeAuthorization::getUserId, userId));
    }

    private WxSubscribeAuthorization findByUserAndType(Long userId, String templateType) {
        return authorizationMapper.selectOne(
                new LambdaQueryWrapper<WxSubscribeAuthorization>()
                        .eq(WxSubscribeAuthorization::getUserId, userId)
                        .eq(WxSubscribeAuthorization::getTemplateType, templateType));
    }

    private WxSubscribeAuthorization getOrCreate(Long userId, String templateType) {
        WxSubscribeAuthorization auth = findByUserAndType(userId, templateType);
        if (auth != null) {
            return auth;
        }
        auth = new WxSubscribeAuthorization();
        auth.setUserId(userId);
        auth.setTemplateType(templateType);
        auth.setCredits(0);
        auth.setUsedCredits(0);
        auth.setCreatedAt(LocalDateTime.now());
        auth.setUpdatedAt(LocalDateTime.now());
        authorizationMapper.insert(auth);
        return auth;
    }
}

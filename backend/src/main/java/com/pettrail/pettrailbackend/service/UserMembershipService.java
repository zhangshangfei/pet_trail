package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.entity.UserMembership;
import com.pettrail.pettrailbackend.mapper.UserMembershipMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMembershipService {

    private final UserMembershipMapper userMembershipMapper;
    private final UserMapper userMapper;

    private static final BigDecimal MONTHLY_PRICE = new BigDecimal("9.90");
    private static final BigDecimal YEARLY_PRICE = new BigDecimal("99.00");

    public Map<String, Object> getMembershipInfo(Long userId) {
        Map<String, Object> result = new HashMap<>();

        UserMembership active = getActiveMembership(userId);
        result.put("isPro", active != null);
        result.put("plan", active != null ? active.getPlan() : "free");
        result.put("expireDate", active != null ? active.getExpireDate() : null);

        Map<String, Object> plans = new HashMap<>();
        Map<String, Object> monthly = new HashMap<>();
        monthly.put("price", MONTHLY_PRICE);
        monthly.put("label", "月度会员");
        monthly.put("priceLabel", "¥9.9/月");
        plans.put("monthly", monthly);

        Map<String, Object> yearly = new HashMap<>();
        yearly.put("price", YEARLY_PRICE);
        yearly.put("label", "年度会员");
        yearly.put("priceLabel", "¥99/年");
        yearly.put("save", "省¥19.8");
        plans.put("yearly", yearly);
        result.put("plans", plans);

        result.put("features", java.util.Arrays.asList(
            Map.of("icon", "📸", "title", "无限相册", "desc", "成长相册不限数量"),
            Map.of("icon", "📊", "title", "高级统计", "desc", "详细健康数据分析"),
            Map.of("icon", "🎨", "title", "主题定制", "desc", "更多主题皮肤选择"),
            Map.of("icon", "🔔", "title", "智能提醒", "desc", "AI智能喂食建议"),
            Map.of("icon", "📋", "title", "数据导出", "desc", "健康数据导出PDF"),
            Map.of("icon", "🏥", "title", "医院优先", "desc", "宠物医院预约优先")
        ));

        return result;
    }

    public UserMembership getActiveMembership(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return userMembershipMapper.selectOne(
            new LambdaQueryWrapper<UserMembership>()
                .eq(UserMembership::getUserId, userId)
                .eq(UserMembership::getStatus, 1)
                .le(UserMembership::getStartDate, now)
                .ge(UserMembership::getExpireDate, now)
                .orderByDesc(UserMembership::getExpireDate)
                .last("LIMIT 1")
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public UserMembership subscribe(Long userId, String plan) {
        BigDecimal amount;
        int months;
        if ("yearly".equals(plan)) {
            amount = YEARLY_PRICE;
            months = 12;
        } else {
            amount = MONTHLY_PRICE;
            months = 1;
        }

        UserMembership active = getActiveMembership(userId);
        LocalDateTime startDate = active != null ? active.getExpireDate() : LocalDateTime.now();
        LocalDateTime expireDate = startDate.plusMonths(months);

        UserMembership membership = new UserMembership();
        membership.setUserId(userId);
        membership.setPlan(plan);
        membership.setAmount(amount);
        membership.setStartDate(startDate);
        membership.setExpireDate(expireDate);
        membership.setStatus(1);
        membership.setOrderNo("PT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        membership.setCreatedAt(LocalDateTime.now());
        membership.setUpdatedAt(LocalDateTime.now());
        userMembershipMapper.insert(membership);

        if (active != null) {
            active.setStatus(2);
            userMembershipMapper.updateById(active);
        }

        return membership;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelSubscription(Long userId) {
        UserMembership active = getActiveMembership(userId);
        if (active == null) return;
        active.setStatus(0);
        active.setUpdatedAt(LocalDateTime.now());
        userMembershipMapper.updateById(active);
    }

    public boolean isPro(Long userId) {
        return getActiveMembership(userId) != null;
    }
}

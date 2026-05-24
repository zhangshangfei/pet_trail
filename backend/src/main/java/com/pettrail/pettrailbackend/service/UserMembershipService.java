package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.MembershipInfoVO;
import com.pettrail.pettrailbackend.entity.UserMembership;
import com.pettrail.pettrailbackend.mapper.UserMembershipMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMembershipService {

    private final UserMembershipMapper userMembershipMapper;

    private static final BigDecimal MONTHLY_PRICE = new BigDecimal("9.90");
    private static final BigDecimal YEARLY_PRICE = new BigDecimal("99.00");

    public MembershipInfoVO getMembershipInfo(Long userId) {
        MembershipInfoVO result = new MembershipInfoVO();

        UserMembership active = getActiveMembership(userId);
        result.setIsPro(active != null);
        result.setPlan(active != null ? active.getPlan() : "free");
        result.setExpireDate(active != null ? active.getExpireDate() : null);

        MembershipInfoVO.PlansVO plans = new MembershipInfoVO.PlansVO();

        MembershipInfoVO.PlanDetailVO monthly = new MembershipInfoVO.PlanDetailVO();
        monthly.setPrice(MONTHLY_PRICE);
        monthly.setLabel("月度会员");
        monthly.setPriceLabel("¥9.9/月");
        plans.setMonthly(monthly);

        MembershipInfoVO.PlanDetailVO yearly = new MembershipInfoVO.PlanDetailVO();
        yearly.setPrice(YEARLY_PRICE);
        yearly.setLabel("年度会员");
        yearly.setPriceLabel("¥99/年");
        yearly.setSave("省¥19.8");
        plans.setYearly(yearly);

        result.setPlans(plans);

        result.setFeatures(List.of(
            buildFeature("📸", "无限相册", "成长相册不限数量"),
            buildFeature("📊", "高级统计", "详细健康数据分析"),
            buildFeature("🎨", "主题定制", "更多主题皮肤选择"),
            buildFeature("🔔", "智能提醒", "AI智能喂食建议"),
            buildFeature("📋", "数据导出", "健康数据导出PDF"),
            buildFeature("🏥", "医院优先", "宠物医院预约优先")
        ));

        return result;
    }

    private MembershipInfoVO.FeatureVO buildFeature(String icon, String title, String desc) {
        MembershipInfoVO.FeatureVO feature = new MembershipInfoVO.FeatureVO();
        feature.setIcon(icon);
        feature.setTitle(title);
        feature.setDesc(desc);
        return feature;
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

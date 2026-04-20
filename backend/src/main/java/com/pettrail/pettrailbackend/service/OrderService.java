package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.OrderVO;
import com.pettrail.pettrailbackend.entity.Order;
import com.pettrail.pettrailbackend.entity.UserMembership;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.exception.NotFoundException;
import com.pettrail.pettrailbackend.mapper.OrderMapper;
import com.pettrail.pettrailbackend.mapper.UserMembershipMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final UserMembershipMapper userMembershipMapper;
    private final UserMembershipService userMembershipService;

    private static final BigDecimal MONTHLY_PRICE = new BigDecimal("9.90");
    private static final BigDecimal YEARLY_PRICE = new BigDecimal("99.00");

    @Value("${wechat.miniapp.app-id:}")
    private String appId;

    @Value("${wechat.miniapp.mch-id:}")
    private String mchId;

    @Value("${wechat.miniapp.api-key:}")
    private String apiKey;

    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, String plan) {
        if (!"monthly".equals(plan) && !"yearly".equals(plan)) {
            throw new BusinessException("无效的订阅计划");
        }

        BigDecimal amount = "yearly".equals(plan) ? YEARLY_PRICE : MONTHLY_PRICE;
        String orderNo = "PT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setPlan(plan);
        order.setAmount(amount);
        order.setStatus(0);
        order.setCreatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        log.info("创建订单: userId={}, orderNo={}, plan={}, amount={}", userId, orderNo, plan, amount);

        OrderVO vo = convertToVO(order);

        if (appId != null && !appId.isEmpty() && mchId != null && !mchId.isEmpty()) {
            vo.setPayParams(buildPayParams(order));
        }

        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderVO handlePayCallback(String orderNo, String wxTransactionId) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new NotFoundException("订单不存在");
        }
        if (order.getStatus() != 0) {
            log.warn("订单已处理: orderNo={}, status={}", orderNo, order.getStatus());
            return convertToVO(order);
        }

        order.setStatus(1);
        order.setWxTransactionId(wxTransactionId);
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);

        int months = "yearly".equals(order.getPlan()) ? 12 : 1;
        UserMembership active = userMembershipService.getActiveMembership(order.getUserId());
        LocalDateTime startDate = active != null ? active.getExpireDate() : LocalDateTime.now();
        LocalDateTime expireDate = startDate.plusMonths(months);

        if (active != null) {
            active.setStatus(2);
            userMembershipMapper.updateById(active);
        }

        UserMembership membership = new UserMembership();
        membership.setUserId(order.getUserId());
        membership.setPlan(order.getPlan());
        membership.setAmount(order.getAmount());
        membership.setStartDate(startDate);
        membership.setExpireDate(expireDate);
        membership.setStatus(1);
        membership.setOrderNo(orderNo);
        membership.setTransactionId(wxTransactionId);
        membership.setCreatedAt(LocalDateTime.now());
        membership.setUpdatedAt(LocalDateTime.now());
        userMembershipMapper.insert(membership);

        log.info("支付回调处理完成: orderNo={}, userId={}, expireDate={}", orderNo, order.getUserId(), expireDate);
        return convertToVO(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderVO simulatePay(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new NotFoundException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单已处理");
        }

        return handlePayCallback(order.getOrderNo(), "SIM_" + System.currentTimeMillis());
    }

    public OrderVO getOrderDetail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new NotFoundException("订单不存在");
        }
        return convertToVO(order);
    }

    public List<OrderVO> getUserOrders(Long userId) {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreatedAt));
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new NotFoundException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单已处理，无法取消");
        }
        order.setStatus(3);
        orderMapper.updateById(order);
        log.info("取消订单: orderId={}, userId={}", orderId, userId);
    }

    private String buildPayParams(Order order) {
        return "{\"appId\":\"" + appId + "\",\"timeStamp\":\"" + System.currentTimeMillis() / 1000
                + "\",\"nonceStr\":\"" + UUID.randomUUID().toString().substring(0, 16)
                + "\",\"package\":\"prepay_id=placeholder\",\"signType\":\"RSA\",\"paySign\":\"placeholder\"}";
    }

    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPlan(order.getPlan());
        vo.setAmount(order.getAmount());
        vo.setStatus(order.getStatus());
        vo.setWxTransactionId(order.getWxTransactionId());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setPaidAt(order.getPaidAt());
        return vo;
    }
}

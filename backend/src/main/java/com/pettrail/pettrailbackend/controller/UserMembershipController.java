package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.MembershipOrderDTO;
import com.pettrail.pettrailbackend.dto.OrderVO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.service.OrderService;
import com.pettrail.pettrailbackend.service.UserMembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
@Tag(name = "会员系统", description = "会员订阅与订单管理")
public class UserMembershipController extends BaseController {

    private final UserMembershipService userMembershipService;
    private final OrderService orderService;

    @GetMapping("/info")
    @Operation(summary = "获取会员信息")
    public Result<Map<String, Object>> getMembershipInfo() {
        Long userId = requireLogin();
        return Result.success(userMembershipService.getMembershipInfo(userId));
    }

    @PostMapping("/orders")
    @Operation(summary = "创建订阅订单")
    public Result<OrderVO> createOrder(@RequestBody MembershipOrderDTO dto) {
        Long userId = requireLogin();
        String plan = dto.getPlan() != null ? dto.getPlan() : "monthly";
        return Result.success(orderService.createOrder(userId, plan));
    }

    @PostMapping("/orders/{id}/pay")
    @Operation(summary = "模拟支付（开发环境）")
    public Result<OrderVO> simulatePay(@PathVariable Long id) {
        Long userId = requireLogin();
        return Result.success(orderService.simulatePay(userId, id));
    }

    @GetMapping("/orders")
    @Operation(summary = "获取订单列表")
    public Result<List<OrderVO>> getOrders() {
        Long userId = requireLogin();
        return Result.success(orderService.getUserOrders(userId));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "获取订单详情")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        Long userId = requireLogin();
        return Result.success(orderService.getOrderDetail(userId, id));
    }

    @DeleteMapping("/orders/{id}")
    @Operation(summary = "取消订单")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        Long userId = requireLogin();
        orderService.cancelOrder(userId, id);
        return Result.success();
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消订阅")
    public Result<Void> cancelSubscription() {
        Long userId = requireLogin();
        userMembershipService.cancelSubscription(userId);
        return Result.success();
    }

    @GetMapping("/check")
    @Operation(summary = "检查是否Pro会员")
    public Result<Map<String, Object>> checkPro() {
        Long userId = requireLogin();
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("isPro", userMembershipService.isPro(userId));
        return Result.success(result);
    }
}

package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Order;
import com.pettrail.pettrailbackend.entity.Product;
import com.pettrail.pettrailbackend.mapper.OrderMapper;
import com.pettrail.pettrailbackend.mapper.ProductMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin-商城管理", description = "后台商品与订单管理")
public class AdminProductController extends BaseAdminController {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;

    @GetMapping
    @Operation(summary = "分页查询商品列表")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) wrapper.eq(Product::getCategory, category);
        if (status != null) wrapper.eq(Product::getStatus, status);
        wrapper.orderByDesc(Product::getCreatedAt);
        return Result.success(productMapper.selectPage(pageParam, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productMapper.selectById(id));
    }

    @PostMapping
    @Operation(summary = "创建商品")
    public Result<Product> create(@RequestBody Product product) {
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setSalesCount(0);
        productMapper.insert(product);
        return Result.success(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        Product existing = productMapper.selectById(id);
        if (existing == null) return Result.error(404, "商品不存在");
        product.setId(id);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        return Result.success(product);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public Result<String> delete(@PathVariable Long id) {
        productMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新商品状态(上架/下架)")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Product product = productMapper.selectById(id);
        if (product == null) return Result.error(404, "商品不存在");
        product.setStatus(body.get("status"));
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        return Result.success("状态更新成功");
    }

    @GetMapping("/stats")
    @Operation(summary = "获取商城统计")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        long totalProducts = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1));
        long totalOrders = orderMapper.selectCount(null);
        long paidOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1));
        stats.put("totalProducts", totalProducts);
        stats.put("totalOrders", totalOrders);
        stats.put("paidOrders", paidOrders);
        return Result.success(stats);
    }

    @GetMapping("/orders")
    @Operation(summary = "分页查询订单列表")
    public Result<Page<Order>> orders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(Order::getStatus, status);
        wrapper.orderByDesc(Order::getCreatedAt);
        return Result.success(orderMapper.selectPage(pageParam, wrapper));
    }
}

package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Order;
import com.pettrail.pettrailbackend.entity.Product;
import com.pettrail.pettrailbackend.service.OrderService;
import com.pettrail.pettrailbackend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin-商城管理", description = "后台商品与订单管理")
public class AdminProductController extends BaseAdminController {

    private final ProductService productService;
    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "分页查询商品列表")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        return Result.success(productService.adminListProducts(page, size, category, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    @PostMapping
    @Operation(summary = "创建商品")
    public Result<Product> create(@RequestBody Product product) {
        return Result.success(productService.adminCreateProduct(product));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        return Result.success(productService.adminUpdateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public Result<String> delete(@PathVariable Long id) {
        productService.adminDeleteProduct(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新商品状态(上架/下架)")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        productService.adminUpdateProductStatus(id, body.get("status"));
        return Result.success("状态更新成功");
    }

    @GetMapping("/stats")
    @Operation(summary = "获取商城统计")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>(productService.adminGetProductStats());
        stats.put("totalOrders", orderService.adminCountOrders());
        stats.put("paidOrders", orderService.adminCountPaidOrders());
        return Result.success(stats);
    }

    @GetMapping("/orders")
    @Operation(summary = "分页查询订单列表")
    public Result<Page<Order>> orders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success(orderService.adminListOrders(page, size, status));
    }
}

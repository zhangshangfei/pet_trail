package com.pettrail.pettrailbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.Product;
import com.pettrail.pettrailbackend.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "宠物商城", description = "商品相关接口")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<Page<Product>> listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer petType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(productService.listProducts(category, petType, page, size));
    }

    @GetMapping("/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    @GetMapping("/recommend")
    public Result<List<Product>> getRecommendProducts(
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = null;
        try {
            userId = new BaseController() {{ }}.requireLogin();
        } catch (Exception ignored) {
        }
        return Result.success(productService.getRecommendProducts(userId, limit));
    }

    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        return Result.success(productService.getCategories());
    }
}

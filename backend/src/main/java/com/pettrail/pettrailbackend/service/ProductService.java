package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.entity.Product;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final long CACHE_TTL_HOURS = 2;

    public Page<Product> listProducts(String category, Integer petType, int page, int size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Product::getCategory, category);
        }
        if (petType != null && petType > 0) {
            wrapper.and(w -> w.eq(Product::getPetType, petType).or().eq(Product::getPetType, 0));
        }
        wrapper.orderByDesc(Product::getSalesCount);

        return productMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public Product getProductDetail(Long productId) {
        String cacheKey = PRODUCT_CACHE_PREFIX + productId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null && cached instanceof Product) {
                return (Product) cached;
            }
        } catch (Exception e) {
            log.warn("商品缓存读取异常: {}", e.getMessage());
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        try {
            redisTemplate.opsForValue().set(cacheKey, product, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("商品缓存写入异常: {}", e.getMessage());
        }

        return product;
    }

    public List<Product> getRecommendProducts(Long userId, int limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        wrapper.orderByDesc(Product::getRating);
        wrapper.last("LIMIT " + limit);
        return productMapper.selectList(wrapper);
    }

    public List<String> getCategories() {
        return List.of("food", "toy", "health", "supplies");
    }
}

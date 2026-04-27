package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.entity.Merchant;
import com.pettrail.pettrailbackend.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/merchants")
@RequiredArgsConstructor
@Tag(name = "Admin-商户管理", description = "商户管理接口")
public class AdminMerchantController extends BaseAdminController {

    private final MerchantService merchantService;

    @GetMapping
    @Operation(summary = "商户列表")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        Page<Merchant> result = merchantService.adminListMerchants(page, size, keyword, type, status);
        return Map.of("success", true, "data", Map.of(
                "records", result.getRecords(),
                "total", result.getTotal(),
                "pages", result.getPages(),
                "current", result.getCurrent()
        ));
    }

    @PostMapping
    @Operation(summary = "创建商户")
    public Map<String, Object> create(@RequestBody Merchant merchant) {
        Merchant created = merchantService.adminCreateMerchant(merchant);
        return Map.of("success", true, "data", created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商户")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Merchant merchant) {
        Merchant updated = merchantService.adminUpdateMerchant(id, merchant);
        return Map.of("success", true, "data", updated);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新商户状态")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        merchantService.adminUpdateMerchantStatus(id, body.get("status"));
        return Map.of("success", true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商户")
    public Map<String, Object> delete(@PathVariable Long id) {
        merchantService.adminDeleteMerchant(id);
        return Map.of("success", true);
    }
}

package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.dto.VetAppointmentVO;
import com.pettrail.pettrailbackend.entity.VetClinic;
import com.pettrail.pettrailbackend.service.VetClinicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/vet-clinics")
@RequiredArgsConstructor
@Tag(name = "Admin-医院管理", description = "后台宠物医院信息管理")
public class AdminVetClinicController extends BaseAdminController {

    private final VetClinicService vetClinicService;

    @GetMapping
    @Operation(summary = "分页查询医院列表")
    public Result<Page<VetClinic>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean isPartner) {
        return Result.success(vetClinicService.adminListClinics(page, size, status, isPartner));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取医院详情")
    public Result<VetClinic> detail(@PathVariable Long id) {
        return Result.success(vetClinicService.getClinicDetail(id));
    }

    @PostMapping
    @Operation(summary = "创建医院")
    public Result<VetClinic> create(@RequestBody VetClinic clinic) {
        return Result.success(vetClinicService.adminCreateClinic(clinic));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新医院信息")
    public Result<VetClinic> update(@PathVariable Long id, @RequestBody VetClinic clinic) {
        return Result.success(vetClinicService.adminUpdateClinic(id, clinic));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除医院")
    public Result<String> delete(@PathVariable Long id) {
        vetClinicService.adminDeleteClinic(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新医院状态")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        vetClinicService.adminUpdateClinicStatus(id, body.get("status"));
        return Result.success("状态更新成功");
    }

    @PutMapping("/{id}/partner")
    @Operation(summary = "设置合作医院")
    public Result<String> setPartner(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        vetClinicService.adminSetPartner(id, body.get("isPartner"));
        return Result.success("更新成功");
    }

    @GetMapping("/stats")
    @Operation(summary = "获取医院统计")
    public Result<Map<String, Object>> stats() {
        return Result.success(vetClinicService.adminGetClinicStats());
    }

    @GetMapping("/appointments")
    @Operation(summary = "分页查询预约列表")
    public Result<Page<VetAppointmentVO>> appointments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long clinicId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword) {
        return Result.success(vetClinicService.adminListAppointments(page, size, status, clinicId, userId, keyword));
    }

    @PutMapping("/appointments/{id}/status")
    @Operation(summary = "更新预约状态")
    public Result<String> updateAppointmentStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        vetClinicService.adminUpdateAppointmentStatus(id, body.get("status"));
        return Result.success("状态更新成功");
    }
}

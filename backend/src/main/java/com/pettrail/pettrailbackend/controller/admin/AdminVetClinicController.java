package com.pettrail.pettrailbackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.VetAppointment;
import com.pettrail.pettrailbackend.entity.VetClinic;
import com.pettrail.pettrailbackend.mapper.VetAppointmentMapper;
import com.pettrail.pettrailbackend.mapper.VetClinicMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/vet-clinics")
@RequiredArgsConstructor
@Tag(name = "Admin-医院管理", description = "后台宠物医院信息管理")
public class AdminVetClinicController extends BaseAdminController {

    private final VetClinicMapper clinicMapper;
    private final VetAppointmentMapper appointmentMapper;

    @GetMapping
    @Operation(summary = "分页查询医院列表")
    public Result<Page<VetClinic>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean isPartner) {
        Page<VetClinic> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<VetClinic> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(VetClinic::getStatus, status);
        if (isPartner != null) wrapper.eq(VetClinic::getIsPartner, isPartner);
        wrapper.orderByDesc(VetClinic::getCreatedAt);
        return Result.success(clinicMapper.selectPage(pageParam, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取医院详情")
    public Result<VetClinic> detail(@PathVariable Long id) {
        return Result.success(clinicMapper.selectById(id));
    }

    @PostMapping
    @Operation(summary = "创建医院")
    public Result<VetClinic> create(@RequestBody VetClinic clinic) {
        clinic.setCreatedAt(LocalDateTime.now());
        clinic.setUpdatedAt(LocalDateTime.now());
        clinicMapper.insert(clinic);
        return Result.success(clinic);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新医院信息")
    public Result<VetClinic> update(@PathVariable Long id, @RequestBody VetClinic clinic) {
        VetClinic existing = clinicMapper.selectById(id);
        if (existing == null) return Result.error(404, "医院不存在");
        clinic.setId(id);
        clinic.setUpdatedAt(LocalDateTime.now());
        clinicMapper.updateById(clinic);
        return Result.success(clinic);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除医院")
    public Result<String> delete(@PathVariable Long id) {
        clinicMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新医院状态")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        VetClinic clinic = clinicMapper.selectById(id);
        if (clinic == null) return Result.error(404, "医院不存在");
        clinic.setStatus(body.get("status"));
        clinic.setUpdatedAt(LocalDateTime.now());
        clinicMapper.updateById(clinic);
        return Result.success("状态更新成功");
    }

    @PutMapping("/{id}/partner")
    @Operation(summary = "设置合作医院")
    public Result<String> setPartner(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        VetClinic clinic = clinicMapper.selectById(id);
        if (clinic == null) return Result.error(404, "医院不存在");
        clinic.setIsPartner(body.get("isPartner"));
        clinic.setUpdatedAt(LocalDateTime.now());
        clinicMapper.updateById(clinic);
        return Result.success("更新成功");
    }

    @GetMapping("/stats")
    @Operation(summary = "获取医院统计")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        long totalClinics = clinicMapper.selectCount(
                new LambdaQueryWrapper<VetClinic>().eq(VetClinic::getStatus, 1));
        long partnerClinics = clinicMapper.selectCount(
                new LambdaQueryWrapper<VetClinic>().eq(VetClinic::getIsPartner, true));
        long totalAppointments = appointmentMapper.selectCount(null);
        long pendingAppointments = appointmentMapper.selectCount(
                new LambdaQueryWrapper<VetAppointment>().eq(VetAppointment::getStatus, 0));
        stats.put("totalClinics", totalClinics);
        stats.put("partnerClinics", partnerClinics);
        stats.put("totalAppointments", totalAppointments);
        stats.put("pendingAppointments", pendingAppointments);
        return Result.success(stats);
    }

    @GetMapping("/appointments")
    @Operation(summary = "分页查询预约列表")
    public Result<Page<VetAppointment>> appointments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        Page<VetAppointment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<VetAppointment> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(VetAppointment::getStatus, status);
        wrapper.orderByDesc(VetAppointment::getCreatedAt);
        return Result.success(appointmentMapper.selectPage(pageParam, wrapper));
    }

    @PutMapping("/appointments/{id}/status")
    @Operation(summary = "更新预约状态")
    public Result<String> updateAppointmentStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        VetAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) return Result.error(404, "预约不存在");
        appointment.setStatus(body.get("status"));
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
        return Result.success("状态更新成功");
    }
}

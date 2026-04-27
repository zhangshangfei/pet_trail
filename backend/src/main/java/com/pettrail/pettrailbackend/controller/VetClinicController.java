package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.VetAppointment;
import com.pettrail.pettrailbackend.entity.VetClinic;
import com.pettrail.pettrailbackend.service.VetClinicService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vet")
@Tag(name = "宠物医院", description = "医院预约相关接口")
@RequiredArgsConstructor
public class VetClinicController extends BaseController {

    private final VetClinicService vetClinicService;

    @GetMapping("/clinics")
    public Result<List<VetClinic>> listClinics(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(vetClinicService.listClinics(latitude, longitude, page, size));
    }

    @GetMapping("/clinics/{id}")
    public Result<VetClinic> getClinicDetail(@PathVariable Long id) {
        return Result.success(vetClinicService.getClinicDetail(id));
    }

    @PostMapping("/appointments")
    public Result<VetAppointment> createAppointment(@RequestBody Map<String, Object> body) {
        Long userId = requireLogin();
        Long clinicId = Long.valueOf(body.get("clinicId").toString());
        Long petId = body.get("petId") != null ? Long.valueOf(body.get("petId").toString()) : null;
        LocalDate date = LocalDate.parse(body.get("appointmentDate").toString());
        String time = body.get("appointmentTime").toString();
        String symptom = body.get("symptom") != null ? body.get("symptom").toString() : null;
        return Result.success(vetClinicService.createAppointment(userId, clinicId, petId, date, time, symptom));
    }

    @GetMapping("/appointments")
    public Result<List<VetAppointment>> myAppointments() {
        Long userId = requireLogin();
        return Result.success(vetClinicService.getUserAppointments(userId));
    }

    @DeleteMapping("/appointments/{id}")
    public Result<String> cancelAppointment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        Long userId = requireLogin();
        vetClinicService.cancelAppointment(id, userId, reason);
        return Result.success("取消成功");
    }
}

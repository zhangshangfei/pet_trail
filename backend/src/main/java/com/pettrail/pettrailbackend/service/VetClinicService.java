package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pettrail.pettrailbackend.dto.VetAppointmentVO;
import com.pettrail.pettrailbackend.entity.Pet;
import com.pettrail.pettrailbackend.entity.User;
import com.pettrail.pettrailbackend.entity.VetAppointment;
import com.pettrail.pettrailbackend.entity.VetClinic;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.PetMapper;
import com.pettrail.pettrailbackend.mapper.UserMapper;
import com.pettrail.pettrailbackend.mapper.VetAppointmentMapper;
import com.pettrail.pettrailbackend.mapper.VetClinicMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VetClinicService {

    private final VetClinicMapper clinicMapper;
    private final VetAppointmentMapper appointmentMapper;
    private final PetMapper petMapper;
    private final UserMapper userMapper;

    public List<VetClinic> listClinics(Double latitude, Double longitude, int page, int size) {
        LambdaQueryWrapper<VetClinic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VetClinic::getStatus, 1);
        if (latitude != null && longitude != null) {
            wrapper.last("ORDER BY ABS(latitude - " + latitude + ") + ABS(longitude - " + longitude + ") ASC LIMIT " + (page - 1) * size + ", " + size);
        } else {
            wrapper.orderByDesc(VetClinic::getRating);
            wrapper.last("LIMIT " + (page - 1) * size + ", " + size);
        }
        return clinicMapper.selectList(wrapper);
    }

    public VetClinic getClinicDetail(Long clinicId) {
        VetClinic clinic = clinicMapper.selectById(clinicId);
        if (clinic == null) {
            throw new BusinessException(404, "医院不存在");
        }
        return clinic;
    }

    @Transactional(rollbackFor = Exception.class)
    public VetAppointment createAppointment(Long userId, Long clinicId, Long petId,
                                             LocalDate appointmentDate, String appointmentTime,
                                             String symptom) {
        VetClinic clinic = clinicMapper.selectById(clinicId);
        if (clinic == null) {
            throw new BusinessException(404, "医院不存在");
        }

        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new BusinessException("预约日期不能早于今天");
        }

        long existingCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<VetAppointment>()
                        .eq(VetAppointment::getClinicId, clinicId)
                        .eq(VetAppointment::getAppointmentDate, appointmentDate)
                        .eq(VetAppointment::getAppointmentTime, appointmentTime)
                        .in(VetAppointment::getStatus, 0, 1));
        if (existingCount >= 5) {
            throw new BusinessException("该时段已约满，请选择其他时段");
        }

        VetAppointment appointment = new VetAppointment();
        appointment.setUserId(userId);
        appointment.setClinicId(clinicId);
        appointment.setPetId(petId);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setSymptom(symptom);
        appointment.setStatus(0);
        appointment.setCreatedAt(LocalDateTime.now());
        appointmentMapper.insert(appointment);

        log.info("创建预约: userId={}, clinicId={}, date={}, time={}", userId, clinicId, appointmentDate, appointmentTime);
        return appointment;
    }

    public List<VetAppointmentVO> getUserAppointments(Long userId) {
        List<VetAppointment> appointments = appointmentMapper.selectList(
                new LambdaQueryWrapper<VetAppointment>()
                        .eq(VetAppointment::getUserId, userId)
                        .orderByDesc(VetAppointment::getCreatedAt));
        if (appointments.isEmpty()) {
            return List.of();
        }
        List<Long> clinicIds = appointments.stream()
                .map(VetAppointment::getClinicId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Long> petIds = appointments.stream()
                .map(VetAppointment::getPetId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> clinicNameMap = clinicIds.isEmpty() ? Map.of()
                : clinicMapper.selectBatchIds(clinicIds).stream()
                        .collect(Collectors.toMap(VetClinic::getId, VetClinic::getName, (a, b) -> a));
        Map<Long, String> clinicPhoneMap = clinicIds.isEmpty() ? Map.of()
                : clinicMapper.selectBatchIds(clinicIds).stream()
                        .collect(Collectors.toMap(VetClinic::getId, VetClinic::getPhone, (a, b) -> a));
        Map<Long, String> petNameMap = petIds.isEmpty() ? Map.of()
                : petMapper.selectBatchIds(petIds).stream()
                        .collect(Collectors.toMap(Pet::getId, Pet::getName, (a, b) -> a));
        return appointments.stream().map(a -> {
            VetAppointmentVO vo = new VetAppointmentVO();
            vo.setId(a.getId());
            vo.setUserId(a.getUserId());
            vo.setClinicId(a.getClinicId());
            vo.setPetId(a.getPetId());
            vo.setAppointmentDate(a.getAppointmentDate());
            vo.setAppointmentTime(a.getAppointmentTime());
            vo.setSymptom(a.getSymptom());
            vo.setStatus(a.getStatus());
            vo.setCancelReason(a.getCancelReason());
            vo.setCreatedAt(a.getCreatedAt());
            vo.setUpdatedAt(a.getUpdatedAt());
            if (a.getClinicId() != null) {
                vo.setClinicName(clinicNameMap.get(a.getClinicId()));
                vo.setClinicPhone(clinicPhoneMap.get(a.getClinicId()));
            }
            if (a.getPetId() != null) {
                vo.setPetName(petNameMap.get(a.getPetId()));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelAppointment(Long appointmentId, Long userId, String reason) {
        VetAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(404, "预约不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException("无权取消他人预约");
        }
        if (appointment.getStatus() == 1) {
            throw new BusinessException("已确认的预约需联系医院取消");
        }
        if (appointment.getStatus() >= 2) {
            throw new BusinessException("预约已完成或已取消");
        }
        appointment.setStatus(3);
        appointment.setCancelReason(reason);
        appointmentMapper.updateById(appointment);
        log.info("取消预约: appointmentId={}, userId={}", appointmentId, userId);
    }

    public Page<VetClinic> adminListClinics(int page, int size, Integer status, Boolean isPartner) {
        Page<VetClinic> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<VetClinic> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(VetClinic::getStatus, status);
        if (isPartner != null) wrapper.eq(VetClinic::getIsPartner, isPartner);
        wrapper.orderByDesc(VetClinic::getCreatedAt);
        return clinicMapper.selectPage(pageParam, wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public VetClinic adminCreateClinic(VetClinic clinic) {
        clinic.setCreatedAt(LocalDateTime.now());
        clinic.setUpdatedAt(LocalDateTime.now());
        clinicMapper.insert(clinic);
        return clinic;
    }

    @Transactional(rollbackFor = Exception.class)
    public VetClinic adminUpdateClinic(Long id, VetClinic clinic) {
        VetClinic existing = clinicMapper.selectById(id);
        if (existing == null) throw new BusinessException(404, "医院不存在");
        clinic.setId(id);
        clinic.setUpdatedAt(LocalDateTime.now());
        clinicMapper.updateById(clinic);
        return clinic;
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteClinic(Long id) {
        clinicMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateClinicStatus(Long id, Integer status) {
        VetClinic clinic = clinicMapper.selectById(id);
        if (clinic == null) throw new BusinessException(404, "医院不存在");
        clinic.setStatus(status);
        clinic.setUpdatedAt(LocalDateTime.now());
        clinicMapper.updateById(clinic);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminSetPartner(Long id, Boolean isPartner) {
        VetClinic clinic = clinicMapper.selectById(id);
        if (clinic == null) throw new BusinessException(404, "医院不存在");
        clinic.setIsPartner(isPartner);
        clinic.setUpdatedAt(LocalDateTime.now());
        clinicMapper.updateById(clinic);
    }

    public Map<String, Object> adminGetClinicStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClinics", clinicMapper.selectCount(new LambdaQueryWrapper<VetClinic>().eq(VetClinic::getStatus, 1)));
        stats.put("partnerClinics", clinicMapper.selectCount(new LambdaQueryWrapper<VetClinic>().eq(VetClinic::getIsPartner, true)));
        stats.put("totalAppointments", appointmentMapper.selectCount(null));
        stats.put("pendingAppointments", appointmentMapper.selectCount(new LambdaQueryWrapper<VetAppointment>().eq(VetAppointment::getStatus, 0)));
        return stats;
    }

    public Page<VetAppointmentVO> adminListAppointments(int page, int size, Integer status, Long clinicId, Long userId, String keyword) {
        Page<VetAppointment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<VetAppointment> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(VetAppointment::getStatus, status);
        if (clinicId != null) wrapper.eq(VetAppointment::getClinicId, clinicId);
        if (userId != null) wrapper.eq(VetAppointment::getUserId, userId);
        if (keyword != null && !keyword.isBlank()) wrapper.like(VetAppointment::getSymptom, keyword);
        wrapper.orderByDesc(VetAppointment::getCreatedAt);
        Page<VetAppointment> result = appointmentMapper.selectPage(pageParam, wrapper);

        Map<Long, String> userNameMap = buildNameMap(result.getRecords(), VetAppointment::getUserId, userMapper);
        Map<Long, String> clinicNameMap = buildNameMap(result.getRecords(), VetAppointment::getClinicId, clinicMapper);
        Map<Long, String> petNameMap = buildNameMap(result.getRecords(), VetAppointment::getPetId, petMapper);

        Page<VetAppointmentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<VetAppointmentVO> voList = result.getRecords().stream().map(a -> {
            VetAppointmentVO vo = new VetAppointmentVO();
            vo.setId(a.getId());
            vo.setUserId(a.getUserId());
            vo.setClinicId(a.getClinicId());
            vo.setPetId(a.getPetId());
            vo.setAppointmentDate(a.getAppointmentDate());
            vo.setAppointmentTime(a.getAppointmentTime());
            vo.setSymptom(a.getSymptom());
            vo.setStatus(a.getStatus());
            vo.setCancelReason(a.getCancelReason());
            vo.setCreatedAt(a.getCreatedAt());
            vo.setUpdatedAt(a.getUpdatedAt());
            vo.setUserName(a.getUserId() != null ? userNameMap.getOrDefault(a.getUserId(), "未知用户") : null);
            vo.setClinicName(a.getClinicId() != null ? clinicNameMap.getOrDefault(a.getClinicId(), "未知医院") : null);
            vo.setPetName(a.getPetId() != null ? petNameMap.getOrDefault(a.getPetId(), "未知宠物") : null);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateAppointmentStatus(Long id, Integer status) {
        VetAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) throw new BusinessException(404, "预约不存在");
        appointment.setStatus(status);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentMapper.updateById(appointment);
    }

    @FunctionalInterface
    private interface IdExtractor<T> { Long extract(T t); }

    private <T> Map<Long, String> buildNameMap(List<VetAppointment> appointments, IdExtractor<VetAppointment> extractor, Object mapper) {
        Set<Long> ids = appointments.stream().map(extractor::extract).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> map = new HashMap<>();
        if (ids.isEmpty()) return map;
        if (mapper instanceof UserMapper) {
            ((UserMapper) mapper).selectBatchIds(ids).forEach(u -> map.put(u.getId(), u.getNickname()));
        } else if (mapper instanceof VetClinicMapper) {
            ((VetClinicMapper) mapper).selectBatchIds(ids).forEach(c -> map.put(c.getId(), c.getName()));
        } else if (mapper instanceof PetMapper) {
            ((PetMapper) mapper).selectBatchIds(ids).forEach(p -> map.put(p.getId(), p.getName()));
        }
        return map;
    }
}

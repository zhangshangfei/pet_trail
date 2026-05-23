package com.pettrail.pettrailbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pettrail.pettrailbackend.dto.PetAlbumVO;
import com.pettrail.pettrailbackend.entity.PetAlbum;
import com.pettrail.pettrailbackend.exception.BusinessException;
import com.pettrail.pettrailbackend.mapper.PetAlbumMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetAlbumService {

    private final PetAlbumMapper petAlbumMapper;

    public List<PetAlbumVO> getPetAlbum(Long petId, Integer year, Integer month) {
        LambdaQueryWrapper<PetAlbum> wrapper = new LambdaQueryWrapper<PetAlbum>()
                .eq(PetAlbum::getPetId, petId);
        if (year != null && month != null) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            wrapper.ge(PetAlbum::getRecordDate, start)
                    .le(PetAlbum::getRecordDate, end);
        }
        wrapper.orderByDesc(PetAlbum::getRecordDate)
                .orderByDesc(PetAlbum::getCreatedAt);
        return petAlbumMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public PetAlbumVO addPhoto(Long userId, Long petId, String imageUrl,
                              String title, String note, LocalDate recordDate) {
        PetAlbum photo = new PetAlbum();
        photo.setUserId(userId);
        photo.setPetId(petId);
        photo.setImageUrl(imageUrl);
        photo.setTitle(title);
        photo.setNote(note);
        photo.setRecordDate(recordDate != null ? recordDate : LocalDate.now());
        photo.setCreatedAt(LocalDateTime.now());
        petAlbumMapper.insert(photo);
        return toVO(photo);
    }

    @Transactional(rollbackFor = Exception.class)
    public PetAlbumVO updatePhoto(Long userId, Long photoId, String title, String note) {
        PetAlbum photo = petAlbumMapper.selectById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new BusinessException("照片不存在或无权修改");
        }
        if (title != null) photo.setTitle(title);
        if (note != null) photo.setNote(note);
        petAlbumMapper.updateById(photo);
        return toVO(photo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePhoto(Long userId, Long photoId) {
        PetAlbum photo = petAlbumMapper.selectById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new BusinessException("照片不存在或无权删除");
        }
        petAlbumMapper.deleteById(photoId);
    }

    private PetAlbumVO toVO(PetAlbum photo) {
        PetAlbumVO vo = new PetAlbumVO();
        vo.setId(photo.getId());
        vo.setPetId(photo.getPetId());
        vo.setImageUrl(photo.getImageUrl());
        vo.setTitle(photo.getTitle());
        vo.setNote(photo.getNote());
        vo.setRecordDate(photo.getRecordDate());
        vo.setCreatedAt(photo.getCreatedAt());
        return vo;
    }
}

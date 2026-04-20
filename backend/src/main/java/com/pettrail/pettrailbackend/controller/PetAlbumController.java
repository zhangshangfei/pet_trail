package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.PetAlbumDTO;
import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.PetAlbum;
import com.pettrail.pettrailbackend.service.PetAlbumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pets/{petId}/album")
@RequiredArgsConstructor
public class PetAlbumController extends BaseController {

    private final PetAlbumService petAlbumService;

    @GetMapping
    public Result<List<PetAlbum>> getAlbum(
            @PathVariable Long petId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return Result.success(petAlbumService.getPetAlbum(petId, year, month));
    }

    @PostMapping
    public Result<PetAlbum> addPhoto(@PathVariable Long petId, @RequestBody PetAlbumDTO dto) {
        Long userId = requireLogin();
        String imageUrl = dto.getImageUrl();
        String title = dto.getTitle();
        String note = dto.getNote();
        String recordDateStr = dto.getRecordDate();
        LocalDate recordDate = (recordDateStr != null && !recordDateStr.isEmpty()) ? LocalDate.parse(recordDateStr) : null;

        if (imageUrl == null || imageUrl.isEmpty()) {
            return Result.error(400, "请上传照片");
        }

        return Result.success(petAlbumService.addPhoto(userId, petId, imageUrl, title, note, recordDate));
    }

    @PutMapping("/{id}")
    public Result<PetAlbum> updatePhoto(@PathVariable Long petId, @PathVariable Long id, @RequestBody PetAlbumDTO dto) {
        Long userId = requireLogin();
        String title = dto.getTitle();
        String note = dto.getNote();
        return Result.success(petAlbumService.updatePhoto(userId, id, title, note));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePhoto(@PathVariable Long petId, @PathVariable Long id) {
        Long userId = requireLogin();
        petAlbumService.deletePhoto(userId, id);
        return Result.success();
    }
}

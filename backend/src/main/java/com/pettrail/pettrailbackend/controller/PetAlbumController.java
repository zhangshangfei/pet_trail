package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.entity.PetAlbum;
import com.pettrail.pettrailbackend.service.PetAlbumService;
import com.pettrail.pettrailbackend.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pets/{petId}/album")
@RequiredArgsConstructor
public class PetAlbumController {

    private final PetAlbumService petAlbumService;

    @GetMapping
    public Result<List<PetAlbum>> getAlbum(
            @PathVariable Long petId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        List<PetAlbum> photos = petAlbumService.getPetAlbum(petId, year, month);
        return Result.success(photos);
    }

    @PostMapping
    public Result<PetAlbum> addPhoto(
            @PathVariable Long petId, @RequestBody Map<String, Object> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        String imageUrl = body.get("imageUrl") != null ? body.get("imageUrl").toString() : null;
        String title = body.get("title") != null ? body.get("title").toString() : null;
        String note = body.get("note") != null ? body.get("note").toString() : null;
        String recordDateStr = body.get("recordDate") != null ? body.get("recordDate").toString() : null;
        java.time.LocalDate recordDate = null;
        if (recordDateStr != null && !recordDateStr.isEmpty()) {
            recordDate = java.time.LocalDate.parse(recordDateStr);
        }

        if (imageUrl == null || imageUrl.isEmpty()) {
            return Result.error(400, "请上传照片");
        }

        try {
            PetAlbum photo = petAlbumService.addPhoto(userId, petId, imageUrl, title, note, recordDate);
            return Result.success(photo);
        } catch (Exception e) {
            log.error("添加相册照片失败: {}", e.getMessage(), e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<PetAlbum> updatePhoto(
            @PathVariable Long petId, @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        String title = body.get("title") != null ? body.get("title").toString() : null;
        String note = body.get("note") != null ? body.get("note").toString() : null;

        try {
            PetAlbum photo = petAlbumService.updatePhoto(userId, id, title, note);
            return Result.success(photo);
        } catch (Exception e) {
            log.error("更新相册照片失败: {}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePhoto(@PathVariable Long petId, @PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        try {
            petAlbumService.deletePhoto(userId, id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除相册照片失败: {}", e.getMessage(), e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}

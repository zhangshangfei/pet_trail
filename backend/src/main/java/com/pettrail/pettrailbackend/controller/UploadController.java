package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.service.CosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import java.io.ByteArrayInputStream;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController extends BaseController {

    private final CosService cosService;

    @PostMapping
    public Result<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        String fileUrl = cosService.uploadFile(file);
        Map<String, Object> data = new HashMap<>();
        data.put("url", fileUrl);
        data.put("originalName", file.getOriginalFilename());
        data.put("size", file.getSize());
        data.put("contentType", file.getContentType());
        return Result.success(data);
    }

    @PostMapping("/base64")
    public Result<Map<String, Object>> uploadFileBase64(@RequestBody Map<String, String> request) throws IOException {
        String fileBase64 = request.get("fileBase64");
        String fileName = request.get("fileName");
        String contentType = request.get("contentType");

        if (fileBase64 == null || fileBase64.isEmpty()) {
            return Result.error(400, "文件内容不能为空");
        }

        byte[] fileBytes = Base64.getDecoder().decode(fileBase64);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
        String fileUrl = cosService.uploadFileBase64(inputStream, fileName, contentType, fileBytes.length);

        Map<String, Object> data = new HashMap<>();
        data.put("url", fileUrl);
        data.put("originalName", fileName);
        data.put("size", fileBytes.length);
        data.put("contentType", contentType);
        return Result.success(data);
    }

    @PostMapping("/batch")
    public Result<Map<String, Object>> uploadFiles(@RequestParam("files") MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            return Result.error(400, "文件不能为空");
        }

        java.util.List<String> urls = new java.util.ArrayList<>();
        long totalSize = 0;
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                urls.add(cosService.uploadFile(file));
                totalSize += file.getSize();
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("urls", urls);
        data.put("count", urls.size());
        data.put("totalSize", totalSize);
        return Result.success(data);
    }
}

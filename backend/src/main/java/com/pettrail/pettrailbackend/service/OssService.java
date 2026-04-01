package com.pettrail.pettrailbackend.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.pettrail.pettrailbackend.config.OssConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssService {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    /**
     * 上传文件到OSS
     * @param file 文件
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        String objectName = "images/" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + "/" + fileName;

        PutObjectRequest request = new PutObjectRequest(
            ossConfig.getBucketName(),
            objectName,
            file.getInputStream()
        );

        PutObjectResult result = ossClient.putObject(request);

        log.info("文件上传成功: {} -> {}", fileName, result);

        return ossConfig.getDomain() + "/" + objectName;
    }

    /**
     * 删除文件
     * @param fileUrl 文件URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        // 从URL中提取objectName
        String objectName = fileUrl.replace(ossConfig.getDomain() + "/", "");

        ossClient.deleteObject(ossConfig.getBucketName(), objectName);

        log.info("文件删除成功: {}", objectName);
    }

    /**
     * 生成唯一文件名
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + extension;
    }
}

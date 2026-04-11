package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.config.CosConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
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
public class CosService {


    private final COSClient cosClient;
    private final CosConfig cosConfig;

    /**
     * 上传文件到微信云托管存储
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // 1. 生成唯一的文件名 (防止重名覆盖)
        String originalFilename = file.getOriginalFilename();
        String fileName = generateFileName(originalFilename);

        // 2. 构建对象路径 (Key)
        // 格式: images/2026-04-11/uuid.png
        String datePath = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String objectName = "images/" + datePath + "/" + fileName;

        // 3. 设置元数据
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        // 4. 创建上传请求
        PutObjectRequest request = new PutObjectRequest(
                cosConfig.getBucketName(),
                objectName,
                file.getInputStream(),
                metadata
        );

        // 5. 执行上传
        PutObjectResult result = cosClient.putObject(request);
        log.info("文件上传成功: {} -> ETag: {}", objectName, result.getETag());

        // 6. 拼接并返回默认访问 URL
        // 格式: https://<BucketName>.cos.<Region>.myqcloud.com/<ObjectName>
        String defaultDomain = String.format("https://%s.cos.%s.myqcloud.com",
                cosConfig.getBucketName(),
                cosConfig.getRegion());

        String fileUrl = defaultDomain + "/" + objectName;

        return fileUrl;
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
        // URL格式: https://<BucketName>.cos.<Region>.myqcloud.com/<ObjectName>
        String defaultDomain = String.format("https://%s.cos.%s.myqcloud.com",
                cosConfig.getBucketName(),
                cosConfig.getRegion());
        String objectName = fileUrl.replace(defaultDomain + "/", "");

        cosClient.deleteObject(cosConfig.getBucketName(), objectName);

        log.info("文件删除成功: {}", objectName);
    }

    /**
     * 生成唯一文件名
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + extension;
    }
}

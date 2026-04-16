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
import java.io.InputStream;
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
        String fileName = generateFileName(file.getOriginalFilename());
        String contentType = file.getContentType();
        String folder = resolveFolder(contentType);
        return uploadToCos(file.getInputStream(), fileName, contentType, (int) file.getSize(), folder);
    }

    /**
     * 上传文件（base64/流方式）
     * @param inputStream 文件流
     * @param fileName 文件名
     * @param contentType 文件类型
     * @param size 文件大小
     * @return 文件访问URL
     */
    public String uploadFileBase64(InputStream inputStream, String fileName, String contentType, int size) throws IOException {
        if (fileName == null || fileName.isEmpty()) {
            fileName = generateFileName("image.jpg");
        }
        String folder = resolveFolder(contentType);
        return uploadToCos(inputStream, fileName, contentType, size, folder);
    }

    /**
     * 实际上传到COS的私有方法
     */
    private String uploadToCos(InputStream inputStream, String fileName, String contentType, int size, String folder) throws IOException {
        String objectName = folder + "/" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + "/" + fileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(size);
        if (contentType != null) {
            metadata.setContentType(contentType);
        }

        PutObjectRequest request = new PutObjectRequest(
                cosConfig.getBucketName(),
                objectName,
                inputStream,
                metadata
        );

        PutObjectResult result = cosClient.putObject(request);
        log.info("文件上传成功: {} -> ETag: {}", objectName, result.getETag());

        String defaultDomain = String.format("https://%s.tcb.qcloud.la",
                cosConfig.getBucketName(),
                cosConfig.getRegion());
        // String defaultDomain = String.format("https://%s.cos.%s.myqcloud.com",
        //         cosConfig.getBucketName(),
        //         cosConfig.getRegion());

        return defaultDomain + "/" + objectName;
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

    private String resolveFolder(String contentType) {
        if (contentType != null && contentType.startsWith("video/")) {
            return "videos";
        }
        return "images";
    }
}

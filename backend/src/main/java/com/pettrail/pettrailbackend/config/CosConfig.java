package com.pettrail.pettrailbackend.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tencent.cos")
public class CosConfig {


    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 地域 (Region)
     * 示例: ap-shanghai (请根据你实际开通的地域填写)
     */
    private String region;

    /**
     * 初始化 COS 客户端
     * 注意：云托管环境下，不需要手动传入 secretId/secretKey
     * SDK 会自动从环境变量 (TENCENTCLOUD_*) 中读取临时密钥和 Token
     */

    @Bean
    public COSClient cosClient() {
        // 1. 手动读取云托管自动注入的环境变量
        String secretId = System.getenv("TENCENTCLOUD_SECRET_ID");
        String secretKey = System.getenv("TENCENTCLOUD_SECRET_KEY");
        String sessionToken = System.getenv("TENCENTCLOUD_SESSION_TOKEN");

        // 2. 使用 BasicSessionCredentials 创建凭证
        // 注意：云托管必须传 sessionToken，否则鉴权失败
        BasicSessionCredentials credentials = new BasicSessionCredentials(secretId, secretKey, sessionToken);

        // 3. 初始化客户端
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(credentials, clientConfig);
    }
}

package com.pettrail.pettrailbackend.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tencent.cos")
public class CosConfig {

    private String bucketName;
    private String region;
    private String secretId;
    private String secretKey;

    @Bean
    public COSClient cosClient() {
        String cloudSecretId = System.getenv("TENCENTCLOUD_SECRET_ID");
        String cloudSecretKey = System.getenv("TENCENTCLOUD_SECRET_KEY");
        String sessionToken = System.getenv("TENCENTCLOUD_SESSION_TOKEN");

        COSCredentials credentials;
        if (cloudSecretId != null && cloudSecretKey != null) {
            credentials = new BasicSessionCredentials(cloudSecretId, cloudSecretKey, sessionToken);
        } else if (secretId != null && !secretId.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
            credentials = new BasicCOSCredentials(secretId, secretKey);
        } else {
            throw new IllegalStateException("COS credentials not configured. Set TENCENTCLOUD_SECRET_ID/KEY or tencent.cos.secret-id/secret-key.");
        }

        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(credentials, clientConfig);
    }
}

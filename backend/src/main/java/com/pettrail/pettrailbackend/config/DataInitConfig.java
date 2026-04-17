package com.pettrail.pettrailbackend.config;

import com.pettrail.pettrailbackend.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitConfig {

    private final AdminService adminService;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            try {
                adminService.initDefaultAdmin();
            } catch (Exception e) {
                log.warn("初始化管理员账号失败（可能表尚未创建）: {}", e.getMessage());
            }
        };
    }
}

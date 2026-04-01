package com.pettrail.pettrailbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pettrail.pettrailbackend.mapper")
public class PetTrailBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetTrailBackendApplication.class, args);
    }
}

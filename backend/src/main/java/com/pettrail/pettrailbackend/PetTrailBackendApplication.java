package com.pettrail.pettrailbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {RabbitAutoConfiguration.class})
@MapperScan("com.pettrail.pettrailbackend.mapper")
@EnableScheduling
@EnableAsync
public class PetTrailBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetTrailBackendApplication.class, args);
    }
}

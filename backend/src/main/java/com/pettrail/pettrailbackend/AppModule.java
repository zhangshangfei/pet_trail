package com.pettrail.pettrailbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.pettrail.pettrailbackend.mapper")
public class AppModule {
}

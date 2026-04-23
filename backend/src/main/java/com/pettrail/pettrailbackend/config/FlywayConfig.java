package com.pettrail.pettrailbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class FlywayConfig {

    // @Bean
    // @Profile("flyway-enabled")
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return new FlywayMigrationStrategy() {
            @Override
            public void migrate(Flyway flyway) {
                try {
                    flyway.repair();
                    log.info("Flyway repair completed");
                } catch (Exception e) {
                    log.warn("Flyway repair skipped: {}", e.getMessage());
                }
                flyway.migrate();
                log.info("Flyway migration completed");
            }
        };
    }
}

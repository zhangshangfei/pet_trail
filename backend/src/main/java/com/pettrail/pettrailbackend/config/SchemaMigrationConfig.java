package com.pettrail.pettrailbackend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SchemaMigrationConfig {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner migrateSchema() {
        return args -> {
            migratePostsTable();
            migrateReportsTable();
            migrateCheckinRemindersTable();
            migrateFeedingRemindersTable();
            migratePetAlbumTable();
        };
    }

    private void migratePostsTable() {
        addColumnIfNotExists("posts", "ee_count", "int(11) DEFAULT '0' COMMENT '嗯嗯数'");
        addColumnIfNotExists("posts", "tags", "json DEFAULT NULL COMMENT '标签列表'");
        addColumnIfNotExists("posts", "audit_status", "tinyint(4) DEFAULT '1' COMMENT '审核状态'");
        addColumnIfNotExists("posts", "audit_remark", "varchar(500) DEFAULT NULL COMMENT '审核备注'");
        addColumnIfNotExists("posts", "deleted", "tinyint(1) DEFAULT '0' COMMENT '是否删除'");
    }

    private void migrateReportsTable() {
        addColumnIfNotExists("reports", "result", "varchar(500) DEFAULT NULL COMMENT '处理结果'");
    }

    private void migrateCheckinRemindersTable() {
        createTableIfNotExists("checkin_reminders",
                "`id` bigint(20) NOT NULL AUTO_INCREMENT, " +
                "`user_id` bigint(20) NOT NULL, " +
                "`item_id` bigint(20) DEFAULT NULL, " +
                "`remind_time` time DEFAULT NULL, " +
                "`is_enabled` tinyint(1) DEFAULT '1', " +
                "`created_at` datetime DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`id`), " +
                "KEY `idx_user_id` (`user_id`)",
                "打卡提醒表");
    }

    private void migrateFeedingRemindersTable() {
        createTableIfNotExists("feeding_reminders",
                "`id` bigint(20) NOT NULL AUTO_INCREMENT, " +
                "`user_id` bigint(20) NOT NULL, " +
                "`pet_id` bigint(20) NOT NULL, " +
                "`meal_type` varchar(50) DEFAULT 'breakfast' COMMENT '餐次类型', " +
                "`time` varchar(10) NOT NULL COMMENT '提醒时间', " +
                "`repeat_type` varchar(20) DEFAULT 'daily' COMMENT '重复方式', " +
                "`note` varchar(500) DEFAULT NULL COMMENT '备注', " +
                "`enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用', " +
                "`created_at` datetime DEFAULT CURRENT_TIMESTAMP, " +
                "`updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`id`), " +
                "KEY `idx_user_id` (`user_id`), " +
                "KEY `idx_pet_id` (`pet_id`)",
                "喂食提醒表");
    }

    private void migratePetAlbumTable() {
        createTableIfNotExists("pet_album",
                "`id` bigint(20) NOT NULL AUTO_INCREMENT, " +
                "`user_id` bigint(20) NOT NULL, " +
                "`pet_id` bigint(20) NOT NULL, " +
                "`image_url` varchar(500) NOT NULL COMMENT '图片地址', " +
                "`title` varchar(100) DEFAULT NULL COMMENT '标题', " +
                "`note` varchar(500) DEFAULT NULL COMMENT '备注', " +
                "`record_date` date DEFAULT NULL COMMENT '记录日期', " +
                "`created_at` datetime DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`id`), " +
                "KEY `idx_pet_id` (`pet_id`), " +
                "KEY `idx_user_id` (`user_id`)",
                "宠物相册表");
    }

    private void addColumnIfNotExists(String tableName, String columnName, String columnDefinition) {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName, columnName);
            if (count != null && count == 0) {
                String alterSql = String.format("ALTER TABLE `%s` ADD COLUMN `%s` %s", tableName, columnName, columnDefinition);
                jdbcTemplate.execute(alterSql);
                log.info("数据库迁移: 为 {} 表添加 {} 列", tableName, columnName);
            }
        } catch (Exception e) {
            log.warn("数据库迁移失败: 为 {} 表添加 {} 列 - {}", tableName, columnName, e.getMessage());
        }
    }

    private void createTableIfNotExists(String tableName, String columns, String comment) {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName);
            if (count != null && count == 0) {
                String createSql = String.format("CREATE TABLE `%s` (%s) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='%s'", tableName, columns, comment);
                jdbcTemplate.execute(createSql);
                log.info("数据库迁移: 创建 {} 表", tableName);
            }
        } catch (Exception e) {
            log.warn("数据库迁移失败: 创建 {} 表 - {}", tableName, e.getMessage());
        }
    }
}

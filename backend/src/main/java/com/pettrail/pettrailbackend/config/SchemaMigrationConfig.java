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
            migrateSysConfigTable();
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

    private void migrateSysConfigTable() {
        createTableIfNotExists("sys_config",
                "`id` bigint(20) NOT NULL AUTO_INCREMENT, " +
                "`config_name` varchar(100) NOT NULL COMMENT '配置项名称', " +
                "`config_key` varchar(100) NOT NULL COMMENT '配置项键名', " +
                "`config_value` text COMMENT '配置项值', " +
                "`config_desc` varchar(500) DEFAULT NULL COMMENT '配置项描述', " +
                "`category` varchar(50) DEFAULT 'system' COMMENT '配置分类', " +
                "`sort_order` int(11) DEFAULT '0' COMMENT '排序', " +
                "`created_at` datetime DEFAULT CURRENT_TIMESTAMP, " +
                "`updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`id`), " +
                "UNIQUE KEY `uk_config_key` (`config_key`), " +
                "KEY `idx_category` (`category`)",
                "系统配置表");
        seedSysConfigData();
    }

    private void seedSysConfigData() {
        String[][] initialConfigs = {
                {"AI功能开关", "ai.enabled", "true", "控制AI分析功能的启用与禁用", "ai", "1"},
                {"AI API密钥", "ai.api-key", "", "OpenRouter API密钥", "ai", "2"},
                {"AI模型", "ai.model", "deepseek/deepseek-chat", "AI分析使用的模型", "ai", "3"},
                {"AI接口地址", "ai.base-url", "https://openrouter.ai/api/v1", "AI接口基础地址", "ai", "4"},
                {"内容审核模式", "content.audit-mode", "auto", "内容审核模式: auto-自动, manual-人工", "content", "1"},
                {"屏蔽词列表", "content.block-words", "", "屏蔽词列表，逗号分隔", "content", "2"},
                {"全局通知开关", "notification.enabled", "true", "全局通知开关", "notification", "1"},
                {"新用户注册开关", "registration.enabled", "true", "新用户注册开关", "registration", "1"},
                {"小程序版本号", "app.version", "1.0.0", "小程序版本号", "system", "1"},
        };
        for (String[] cfg : initialConfigs) {
            try {
                String checkSql = "SELECT COUNT(*) FROM sys_config WHERE config_key = ?";
                Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, cfg[1]);
                if (count != null && count == 0) {
                    String insertSql = "INSERT INTO sys_config (config_name, config_key, config_value, config_desc, category, sort_order) VALUES (?, ?, ?, ?, ?, ?)";
                    jdbcTemplate.update(insertSql, cfg[0], cfg[1], cfg[2], cfg[3], cfg[4], Integer.parseInt(cfg[5]));
                    log.info("数据库迁移: 初始化系统配置 {}", cfg[1]);
                }
            } catch (Exception e) {
                log.warn("数据库迁移失败: 初始化系统配置 {} - {}", cfg[1], e.getMessage());
            }
        }
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

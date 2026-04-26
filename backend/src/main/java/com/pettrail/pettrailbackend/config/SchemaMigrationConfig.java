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
            migrateAiModelTable();
            migrateAiModelSwitchLogTable();
            migrateAiModelStatsTable();
            migrateWxSubscribeAuthorizationTable();
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
                {"GLM模型开关", "ai.glm.enabled", "true", "GLM模型优先调用开关", "ai", "5"},
                {"GLM API密钥", "ai.glm.api-key", "", "智谱GLM API密钥", "ai", "6"},
                {"GLM模型名称", "ai.glm.model", "glm-4.7-flash", "GLM模型名称", "ai", "7"},
                {"GLM接口地址", "ai.glm.base-url", "https://open.bigmodel.cn/api/paas/v4", "GLM接口基础地址", "ai", "8"},
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

    private void migrateAiModelTable() {
        createTableIfNotExists("ai_model",
                "`id` bigint(20) NOT NULL AUTO_INCREMENT, " +
                "`model_name` varchar(100) NOT NULL COMMENT '模型标识名', " +
                "`display_name` varchar(100) NOT NULL COMMENT '模型显示名称', " +
                "`provider` varchar(50) NOT NULL COMMENT '模型提供商', " +
                "`base_url` varchar(255) NOT NULL COMMENT 'API基础地址', " +
                "`api_key` varchar(500) NOT NULL COMMENT 'API密钥', " +
                "`model_version` varchar(50) DEFAULT NULL COMMENT '模型版本号', " +
                "`parameters` json DEFAULT NULL COMMENT '模型参数配置', " +
                "`status` tinyint(4) DEFAULT 1 COMMENT '状态: 1-启用 0-禁用', " +
                "`is_default` tinyint(4) DEFAULT 0 COMMENT '是否为当前活动模型', " +
                "`sort_order` int(11) DEFAULT 0 COMMENT '排序', " +
                "`description` varchar(500) DEFAULT NULL COMMENT '模型描述', " +
                "`icon` varchar(50) DEFAULT NULL COMMENT '模型图标', " +
                "`created_at` datetime DEFAULT CURRENT_TIMESTAMP, " +
                "`updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`id`), " +
                "KEY `idx_status` (`status`), " +
                "KEY `idx_is_default` (`is_default`), " +
                "KEY `idx_provider` (`provider`)",
                "AI模型配置表");
        seedAiModelData();
    }

    private void migrateAiModelSwitchLogTable() {
        createTableIfNotExists("ai_model_switch_log",
                "`id` bigint(20) NOT NULL AUTO_INCREMENT, " +
                "`from_model_id` bigint(20) DEFAULT NULL COMMENT '切换前模型ID', " +
                "`from_model_name` varchar(100) DEFAULT NULL COMMENT '切换前模型名称', " +
                "`to_model_id` bigint(20) NOT NULL COMMENT '切换后模型ID', " +
                "`to_model_name` varchar(100) NOT NULL COMMENT '切换后模型名称', " +
                "`switch_type` varchar(20) NOT NULL COMMENT '切换类型', " +
                "`operator_id` bigint(20) DEFAULT NULL COMMENT '操作者ID', " +
                "`operator_name` varchar(50) DEFAULT NULL COMMENT '操作者名称', " +
                "`reason` varchar(500) DEFAULT NULL COMMENT '切换原因', " +
                "`status` varchar(20) NOT NULL COMMENT '切换状态', " +
                "`duration` bigint(20) DEFAULT NULL COMMENT '切换耗时(毫秒)', " +
                "`error_message` varchar(500) DEFAULT NULL COMMENT '错误信息', " +
                "`created_at` datetime DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`id`), " +
                "KEY `idx_to_model_id` (`to_model_id`), " +
                "KEY `idx_switch_type` (`switch_type`), " +
                "KEY `idx_created_at` (`created_at`)",
                "AI模型切换日志表");
    }

    private void migrateAiModelStatsTable() {
        createTableIfNotExists("ai_model_stats",
                "`id` bigint(20) NOT NULL AUTO_INCREMENT, " +
                "`model_id` bigint(20) NOT NULL COMMENT '模型ID', " +
                "`stats_date` date NOT NULL COMMENT '统计日期', " +
                "`call_count` bigint(20) DEFAULT 0 COMMENT '调用次数', " +
                "`success_count` bigint(20) DEFAULT 0 COMMENT '成功次数', " +
                "`fail_count` bigint(20) DEFAULT 0 COMMENT '失败次数', " +
                "`total_response_time` bigint(20) DEFAULT 0 COMMENT '总响应时间(毫秒)', " +
                "`avg_response_time` double DEFAULT NULL COMMENT '平均响应时间(毫秒)', " +
                "`success_rate` double DEFAULT NULL COMMENT '成功率(%)', " +
                "`min_response_time` bigint(20) DEFAULT NULL COMMENT '最小响应时间(毫秒)', " +
                "`max_response_time` bigint(20) DEFAULT NULL COMMENT '最大响应时间(毫秒)', " +
                "`created_at` datetime DEFAULT CURRENT_TIMESTAMP, " +
                "`updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`id`), " +
                "UNIQUE KEY `uk_model_date` (`model_id`, `stats_date`), " +
                "KEY `idx_stats_date` (`stats_date`), " +
                "KEY `idx_model_id` (`model_id`)",
                "AI模型性能统计表");
    }

    private void migrateWxSubscribeAuthorizationTable() {
        createTableIfNotExists("wx_subscribe_authorization",
                "`id` bigint(20) NOT NULL AUTO_INCREMENT, " +
                "`user_id` bigint(20) NOT NULL COMMENT '用户ID', " +
                "`template_type` varchar(50) NOT NULL COMMENT '模板类型: checkin/vaccine/parasite/feeding', " +
                "`credits` int(11) NOT NULL DEFAULT 0 COMMENT '剩余可用授权次数', " +
                "`used_credits` int(11) NOT NULL DEFAULT 0 COMMENT '已使用授权次数', " +
                "`created_at` datetime DEFAULT CURRENT_TIMESTAMP, " +
                "`updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`id`), " +
                "UNIQUE KEY `uk_user_template` (`user_id`, `template_type`), " +
                "KEY `idx_user_id` (`user_id`)",
                "微信订阅消息授权积分表");
    }

    private void seedAiModelData() {
        String[][] initialModels = {
                {"deepseek/deepseek-chat", "DeepSeek 智能分析", "openrouter", "https://openrouter.ai/api/v1", "", "v3", "{\"temperature\": 0.7, \"max_tokens\": 300}", "1", "1", "1", "基于DeepSeek大模型的宠物健康智能分析，擅长综合评估与建议", "🧠"},
                {"glm-4-flash", "智谱GLM快速分析", "zhipu", "https://open.bigmodel.cn/api/paas/v4", "", "v4", "{\"temperature\": 0.6, \"max_tokens\": 250}", "1", "0", "2", "基于智谱GLM-4-Flash的快速健康分析，响应速度快", "⚡"},
                {"openrouter/free", "通用免费模型", "openrouter", "https://openrouter.ai/api/v1", "", "free", "{\"temperature\": 0.7, \"max_tokens\": 200}", "1", "0", "3", "免费通用模型，适合基础健康分析需求", "🎁"},
        };
        for (String[] m : initialModels) {
            try {
                String checkSql = "SELECT COUNT(*) FROM ai_model WHERE model_name = ?";
                Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, m[0]);
                if (count != null && count == 0) {
                    String insertSql = "INSERT INTO ai_model (model_name, display_name, provider, base_url, api_key, model_version, parameters, status, is_default, sort_order, description, icon) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    jdbcTemplate.update(insertSql, m[0], m[1], m[2], m[3], m[4], m[5], m[6], Integer.parseInt(m[7]), Integer.parseInt(m[8]), Integer.parseInt(m[9]), m[10], m[11]);
                    log.info("数据库迁移: 初始化AI模型 {}", m[0]);
                }
            } catch (Exception e) {
                log.warn("数据库迁移失败: 初始化AI模型 {} - {}", m[0], e.getMessage());
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

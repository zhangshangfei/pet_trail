-- 宠迹项目 - 数据库完整初始化脚本
-- 更新日期: 2026-04-10

-- 创建数据库
CREATE DATABASE IF NOT EXISTS pet_trail DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_trail;

-- ========================================
-- 基础表：用户、宠物
-- ========================================

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `openid` varchar(100) NOT NULL COMMENT '微信OpenID',
  `unionid` varchar(100) DEFAULT NULL COMMENT '微信UnionID',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `gender` tinyint(4) DEFAULT NULL COMMENT '性别: 0-未知, 1-男, 2-女',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_unionid` (`unionid`),
  KEY `idx_openid` (`openid`),
  KEY `idx_unionid` (`unionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 宠物表
CREATE TABLE IF NOT EXISTS `pets` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '所属用户ID',
  `name` varchar(50) NOT NULL COMMENT '宠物名称',
  `breed` varchar(50) DEFAULT NULL COMMENT '品种',
  `gender` tinyint(4) DEFAULT NULL COMMENT '性别: 0-未知, 1-公, 2-母',
  `sterilized` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否绝育: 0-未绝育, 1-已绝育',
  `category` tinyint(4) NOT NULL DEFAULT '0' COMMENT '类别: 0-其他, 1-猫, 2-狗',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `weight` decimal(5,2) DEFAULT NULL COMMENT '体重(kg)',
  `color` varchar(50) DEFAULT NULL COMMENT '毛色',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物表';

-- ========================================
-- 打卡模块
-- ========================================

-- 打卡项表
CREATE TABLE IF NOT EXISTS `checkin_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '打卡项名称',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
  `type` tinyint(4) DEFAULT '1' COMMENT '类型：1-日常 2-健康 3-训练',
  `description` varchar(500) DEFAULT NULL COMMENT '说明',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认',
  `is_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡项表';

-- 打卡记录表
CREATE TABLE IF NOT EXISTS `checkin_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) DEFAULT NULL,
  `item_id` bigint(20) NOT NULL,
  `record_date` date NOT NULL,
  `status` tinyint(4) DEFAULT '1' COMMENT '状态：1-完成 2-取消',
  `note` varchar(500) DEFAULT NULL COMMENT '备注',
  `images` json DEFAULT NULL COMMENT '图片',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_item_date` (`user_id`,`item_id`,`record_date`),
  KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- 打卡统计表
CREATE TABLE IF NOT EXISTS `checkin_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `item_id` bigint(20) NOT NULL,
  `total_count` int(11) DEFAULT '0' COMMENT '累计次数',
  `current_streak` int(11) DEFAULT '0' COMMENT '当前连续天数',
  `max_streak` int(11) DEFAULT '0' COMMENT '最大连续天数',
  `last_checkin_date` date DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_item` (`user_id`,`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡统计表';

-- ========================================
-- 社交模块（动态、点赞、评论）
-- ========================================

-- 动态表
CREATE TABLE IF NOT EXISTS `posts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '发布者 ID',
  `pet_id` bigint(20) DEFAULT NULL COMMENT '关联宠物 ID',
  `content` varchar(2000) NOT NULL COMMENT '内容',
  `images` json DEFAULT NULL COMMENT '图片 URL 数组',
  `like_count` int(11) DEFAULT '0' COMMENT '点赞数',
  `comment_count` int(11) DEFAULT '0' COMMENT '评论数',
  `share_count` int(11) DEFAULT '0' COMMENT '分享数',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态：0-审核中 1-正常 2-删除',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';

-- 动态点赞表
CREATE TABLE IF NOT EXISTS `post_likes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`),
  KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态点赞表';

-- 动态评论表
CREATE TABLE IF NOT EXISTS `post_comments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父评论 ID',
  `reply_to_id` bigint(20) DEFAULT NULL COMMENT '回复的评论 ID',
  `content` varchar(1000) NOT NULL COMMENT '评论内容',
  `images` json DEFAULT NULL COMMENT '评论图片',
  `like_count` int(11) DEFAULT '0' COMMENT '点赞数',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态：1-正常 2-删除',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态评论表';

-- ========================================
-- 健康模块（体重、步数、饮水、统计）
-- ========================================

-- 体重记录表
CREATE TABLE IF NOT EXISTS `weight_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pet_id` bigint(20) NOT NULL COMMENT '宠物ID',
  `weight` decimal(5,2) NOT NULL COMMENT '体重(kg)',
  `record_date` date NOT NULL COMMENT '记录日期',
  `note` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pet_date` (`pet_id`, `record_date`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体重记录表';

-- 步数记录表
CREATE TABLE IF NOT EXISTS `step_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) DEFAULT NULL,
  `steps` int(11) NOT NULL COMMENT '步数',
  `distance` decimal(10,2) DEFAULT NULL COMMENT '距离 (km)',
  `record_date` date NOT NULL,
  `source` tinyint(4) DEFAULT '1' COMMENT '来源：1-手动 2-设备同步',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pet_date` (`user_id`,`pet_id`,`record_date`),
  KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='步数记录表';

-- 饮水记录表
CREATE TABLE IF NOT EXISTS `water_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) DEFAULT NULL,
  `amount` decimal(10,2) NOT NULL COMMENT '水量 (ml)',
  `record_date` date NOT NULL,
  `record_time` time DEFAULT NULL COMMENT '记录时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`,`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮水记录表';

-- 健康统计表（每日聚合）
CREATE TABLE IF NOT EXISTS `health_daily_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) DEFAULT NULL,
  `stat_date` date NOT NULL,
  `total_steps` int(11) DEFAULT '0',
  `total_water` decimal(10,2) DEFAULT '0',
  `weight` decimal(5,2) DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pet_date` (`user_id`,`pet_id`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康统计表';

-- ========================================
-- 提醒模块（疫苗、驱虫）
-- ========================================

-- 疫苗提醒表
CREATE TABLE IF NOT EXISTS `vaccine_reminders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pet_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `vaccine_name` varchar(100) NOT NULL COMMENT '疫苗名称',
  `vaccine_type` tinyint(4) DEFAULT '1' COMMENT '类型：1-核心疫苗 2-非核心',
  `next_date` date NOT NULL COMMENT '下次接种日期',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态：0-待接种 1-已接种 2-已取消',
  `reminder_days` int(11) DEFAULT '7' COMMENT '提前几天提醒',
  `is_notified` tinyint(1) DEFAULT '0' COMMENT '是否已通知',
  `note` varchar(500) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_next_date` (`next_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫苗提醒表';

-- 驱虫提醒表
CREATE TABLE IF NOT EXISTS `parasite_reminders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pet_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `type` tinyint(4) NOT NULL COMMENT '类型：1-体内驱虫 2-体外驱虫',
  `product_name` varchar(100) DEFAULT NULL COMMENT '产品名称',
  `next_date` date NOT NULL,
  `interval_days` int(11) DEFAULT '30' COMMENT '间隔天数',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态：0-待驱虫 1-已完成 2-已取消',
  `is_notified` tinyint(1) DEFAULT '0',
  `note` varchar(500) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_next_date` (`next_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='驱虫提醒表';

-- ========================================
-- 成就模块
-- ========================================

-- 成就表
CREATE TABLE IF NOT EXISTS `achievements` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '成就名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
  `type` tinyint(4) DEFAULT '1' COMMENT '类型：1-打卡 2-健康 3-社交 4-成长',
  `condition_type` varchar(50) DEFAULT NULL COMMENT '条件类型',
  `condition_value` int(11) DEFAULT NULL COMMENT '条件值',
  `sort_order` int(11) DEFAULT '0',
  `is_enabled` tinyint(1) DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就表';

-- 用户成就表
CREATE TABLE IF NOT EXISTS `user_achievements` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `achievement_id` bigint(20) NOT NULL,
  `unlocked_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint(4) DEFAULT '1' COMMENT '状态：1-未完成 2-已完成 3-已领取',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_achievement` (`user_id`,`achievement_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就表';

-- ========================================
-- 初始化数据
-- ========================================

-- 插入默认打卡项
INSERT INTO `checkin_items` (`name`, `icon`, `type`, `sort_order`, `is_default`) VALUES
('喂食', '🍽️', 1, 1, 1),
('遛狗', '🐕', 1, 2, 1),
('梳毛', '🪮', 1, 3, 1),
('刷牙', '🪥', 1, 4, 1),
('洗澡', '🛁', 1, 5, 1),
('玩耍', '🎾', 1, 6, 1),
('训练', '🎓', 3, 7, 1),
('体检', '🩺', 2, 8, 1);

-- 插入默认成就
INSERT INTO `achievements` (`name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`) VALUES
('新手上路', '完成第一次打卡', '🌱', 1, 'checkin_count', 1, 1),
('坚持不懈', '连续打卡 7 天', '🔥', 1, 'checkin_streak', 7, 2),
('打卡达人', '累计打卡 30 天', '⭐', 1, 'checkin_count', 30, 3),
('健康卫士', '记录 30 次健康数据', '💪', 2, 'health_record_count', 30, 4),
('社交达人', '发布 10 条动态', '📱', 3, 'post_count', 10, 5),
('人气王', '获得 100 个赞', '❤️', 3, 'like_received', 100, 6);


ALTER TABLE posts ADD COLUMN ee_count INT DEFAULT 0 COMMENT '收藏数' AFTER share_count;


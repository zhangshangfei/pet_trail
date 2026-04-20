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
  `status` tinyint(4) DEFAULT 1 COMMENT '状态: 1-正常 0-已删除',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_unionid` (`unionid`),
  KEY `idx_openid` (`openid`),
  KEY `idx_unionid` (`unionid`),
  KEY `idx_status` (`status`)
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
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID，NULL表示系统默认',
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
  KEY `idx_type` (`type`),
  KEY `idx_user_id` (`user_id`)
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

CREATE TABLE IF NOT EXISTS `user_hidden_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `item_id` bigint(20) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_item` (`user_id`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户隐藏的打卡项';

CREATE TABLE IF NOT EXISTS `feedbacks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `type` varchar(50) DEFAULT 'other' COMMENT '类型：bug/feature/experience/other',
  `content` text NOT NULL COMMENT '反馈内容',
  `contact` varchar(100) DEFAULT NULL COMMENT '联系方式',
  `images` text DEFAULT NULL COMMENT '截图',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态：0-待处理 1-已处理',
  `reply` text DEFAULT NULL COMMENT '回复',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意见反馈';

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
  `videos` json DEFAULT NULL COMMENT '视频 URL 数组',
  `like_count` int(11) DEFAULT '0' COMMENT '点赞数',
  `comment_count` int(11) DEFAULT '0' COMMENT '评论数',
  `share_count` int(11) DEFAULT '0' COMMENT '分享数',
  `location` varchar(200) DEFAULT NULL COMMENT '位置信息',
  `stickers` json DEFAULT NULL COMMENT '贴纸列表',
  `bubble` json DEFAULT NULL COMMENT '文字气泡',
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

-- 关注表
CREATE TABLE IF NOT EXISTS `follows` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `follower_id` bigint(20) NOT NULL COMMENT '关注者ID',
  `followee_id` bigint(20) NOT NULL COMMENT '被关注者ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_followee` (`follower_id`, `followee_id`),
  KEY `idx_follower_id` (`follower_id`),
  KEY `idx_followee_id` (`followee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `post_ee` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

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

CREATE TABLE IF NOT EXISTS `notifications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '接收通知的用户ID',
  `from_user_id` bigint(20) NOT NULL COMMENT '触发通知的用户ID',
  `type` varchar(32) NOT NULL COMMENT '通知类型：like-点赞, comment-评论, follow-关注, system-系统',
  `target_id` bigint(20) DEFAULT NULL COMMENT '关联目标ID（动态ID/评论ID等）',
  `content` varchar(500) DEFAULT NULL COMMENT '通知内容',
  `title` varchar(100) DEFAULT NULL COMMENT '通知标题',
  `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读：0-未读, 1-已读',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_read` (`user_id`, `is_read`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ========================================
-- 举报表
-- ========================================

CREATE TABLE IF NOT EXISTS `reports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `reporter_id` bigint(20) NOT NULL COMMENT '举报人ID',
  `target_id` bigint(20) NOT NULL COMMENT '被举报目标ID',
  `target_type` varchar(20) NOT NULL COMMENT '目标类型：post/user/comment',
  `reason` varchar(50) NOT NULL COMMENT '举报原因',
  `description` varchar(500) DEFAULT NULL COMMENT '补充描述',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态：0-待处理 1-已处理 2-已驳回',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reporter_target` (`reporter_id`, `target_id`, `target_type`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

CREATE TABLE IF NOT EXISTS `admins` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码(BCrypt)',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `role` varchar(50) DEFAULT 'ADMIN' COMMENT '角色: SUPER_ADMIN, ADMIN',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
  `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

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

-- ========================================
-- 后台管理扩展表
-- ========================================

CREATE TABLE IF NOT EXISTS `admin_operation_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_id` bigint(20) DEFAULT NULL COMMENT '操作管理员ID',
  `admin_name` varchar(50) DEFAULT NULL COMMENT '操作管理员名称',
  `module` varchar(30) DEFAULT NULL COMMENT '操作模块',
  `action` varchar(30) DEFAULT NULL COMMENT '操作动作',
  `target_type` varchar(30) DEFAULT NULL COMMENT '目标类型',
  `target_id` bigint(20) DEFAULT NULL COMMENT '目标ID',
  `detail` varchar(500) DEFAULT NULL COMMENT '操作详情',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志';

CREATE TABLE IF NOT EXISTS `system_settings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `setting_key` varchar(100) NOT NULL COMMENT '设置键',
  `setting_value` text COMMENT '设置值',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_setting_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置';

INSERT IGNORE INTO `system_settings` (`setting_key`, `setting_value`, `description`) VALUES
('content_audit_mode', 'auto', '内容审核模式: auto-自动, manual-人工'),
('audit_block_words', '', '屏蔽词列表，逗号分隔'),
('app_version', '1.0.0', '小程序版本号'),
('notification_enabled', 'true', '全局通知开关'),
('registration_enabled', 'true', '新用户注册开关');

-- ========================================
-- V2.1 新增表：用户行为追踪、话题标签、打卡提醒
-- ========================================

CREATE TABLE IF NOT EXISTS `user_behaviors` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `action` varchar(32) NOT NULL COMMENT '行为类型：view-浏览, like-点赞, comment-评论, share-分享, follow-关注, collect-收藏, publish-发布',
  `target_type` varchar(32) DEFAULT NULL COMMENT '目标类型：post/user/pet',
  `target_id` bigint(20) DEFAULT NULL COMMENT '目标ID',
  `duration` int(11) DEFAULT 0 COMMENT '停留时长(秒)，仅浏览行为有值',
  `extra` json DEFAULT NULL COMMENT '额外数据',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_action` (`action`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_user_action` (`user_id`, `action`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为追踪表';

CREATE TABLE IF NOT EXISTS `tags` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `usage_count` int(11) DEFAULT '0' COMMENT '使用次数',
  `is_hot` tinyint(1) DEFAULT '0' COMMENT '是否热门',
  `is_official` tinyint(1) DEFAULT '0' COMMENT '是否官方标签',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态：1-正常 0-禁用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_usage_count` (`usage_count`),
  KEY `idx_is_hot` (`is_hot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题标签表';

CREATE TABLE IF NOT EXISTS `post_tags` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL COMMENT '动态ID',
  `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态-标签关联表';

CREATE TABLE IF NOT EXISTS `checkin_reminders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `item_id` bigint(20) DEFAULT NULL COMMENT '打卡项ID，NULL表示全部',
  `remind_time` time NOT NULL COMMENT '提醒时间',
  `is_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡提醒设置表';

ALTER TABLE posts ADD COLUMN IF NOT EXISTS `tags` json DEFAULT NULL COMMENT '标签列表' AFTER bubble;

ALTER TABLE posts ADD INDEX idx_status_created (status, created_at);
ALTER TABLE post_comments ADD INDEX idx_user_status (user_id, status);

INSERT IGNORE INTO `tags` (`name`, `usage_count`, `is_hot`, `is_official`) VALUES
('萌宠日常', 0, 1, 1),
('宠物才艺', 0, 1, 1),
('宠物穿搭', 0, 1, 1),
('宠物旅行', 0, 1, 1),
('养宠心得', 0, 1, 1),
('新手养宠', 0, 1, 1),
('猫咪控', 0, 1, 1),
('狗狗控', 0, 1, 1);


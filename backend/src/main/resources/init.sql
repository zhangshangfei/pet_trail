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
  `ee_count` int(11) DEFAULT '0' COMMENT '嗯嗯数',
  `location` varchar(200) DEFAULT NULL COMMENT '位置信息',
  `stickers` json DEFAULT NULL COMMENT '贴纸列表',
  `bubble` json DEFAULT NULL COMMENT '文字气泡',
  `tags` json DEFAULT NULL COMMENT '标签列表',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态：0-审核中 1-正常 2-删除',
  `audit_status` tinyint(4) DEFAULT '1' COMMENT '审核状态：1-通过 2-拒绝',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0-否 1-是',
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
-- 健康模块（体重）
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

CREATE TABLE IF NOT EXISTS `feeding_reminders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) NOT NULL,
  `meal_type` varchar(50) DEFAULT 'breakfast' COMMENT '餐次类型',
  `time` varchar(10) NOT NULL COMMENT '提醒时间',
  `repeat_type` varchar(20) DEFAULT 'daily' COMMENT '重复方式',
  `note` varchar(500) DEFAULT NULL COMMENT '备注',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_pet_id` (`pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='喂食提醒表';

CREATE TABLE IF NOT EXISTS `pet_album` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) NOT NULL,
  `image_url` varchar(500) NOT NULL COMMENT '图片地址',
  `title` varchar(100) DEFAULT NULL COMMENT '标题',
  `note` varchar(500) DEFAULT NULL COMMENT '备注',
  `record_date` date DEFAULT NULL COMMENT '记录日期',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物相册表';

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
  `result` varchar(500) DEFAULT NULL COMMENT '处理结果',
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

CREATE TABLE IF NOT EXISTS `user_membership` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `plan` varchar(20) NOT NULL COMMENT 'monthly/yearly',
  `amount` decimal(10,2) NOT NULL,
  `start_date` datetime DEFAULT NULL,
  `expire_date` datetime DEFAULT NULL,
  `status` tinyint(4) DEFAULT '1' COMMENT '0-取消 1-有效 2-被替换',
  `order_no` varchar(50) DEFAULT NULL,
  `transaction_id` varchar(50) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会员表';

CREATE TABLE IF NOT EXISTS `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `plan` varchar(20) NOT NULL COMMENT 'pro_month/pro_year',
  `amount` decimal(10,2) NOT NULL,
  `status` tinyint(4) DEFAULT '0' COMMENT '0-待支付 1-已支付 2-已退款 3-已取消',
  `wx_transaction_id` varchar(50) DEFAULT NULL COMMENT '微信交易号',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `paid_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

INSERT IGNORE INTO `tags` (`name`, `usage_count`, `is_hot`, `is_official`) VALUES
('萌宠日常', 0, 1, 1),
('宠物才艺', 0, 1, 1),
('宠物穿搭', 0, 1, 1),
('宠物旅行', 0, 1, 1),
('养宠心得', 0, 1, 1),
('新手养宠', 0, 1, 1),
('猫咪控', 0, 1, 1),
('狗狗控', 0, 1, 1);

-- 增量迁移：为 posts 表添加缺失字段
ALTER TABLE `posts` ADD COLUMN IF NOT EXISTS `ee_count` int(11) DEFAULT '0' COMMENT '嗯嗯数' AFTER `share_count`;
ALTER TABLE `posts` ADD COLUMN IF NOT EXISTS `tags` json DEFAULT NULL COMMENT '标签列表' AFTER `bubble`;
ALTER TABLE `posts` ADD COLUMN IF NOT EXISTS `audit_status` tinyint(4) DEFAULT '1' COMMENT '审核状态：1-通过 2-拒绝' AFTER `status`;
ALTER TABLE `posts` ADD COLUMN IF NOT EXISTS `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注' AFTER `audit_status`;
ALTER TABLE `posts` ADD COLUMN IF NOT EXISTS `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0-否 1-是' AFTER `audit_remark`;

-- 增量迁移：为 reports 表添加缺失字段
ALTER TABLE `reports` ADD COLUMN IF NOT EXISTS `result` varchar(500) DEFAULT NULL COMMENT '处理结果' AFTER `status`;

-- ========================================
-- 系统配置表
-- ========================================

CREATE TABLE IF NOT EXISTS `sys_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_name` varchar(100) NOT NULL COMMENT '配置项名称',
  `config_key` varchar(100) NOT NULL COMMENT '配置项键名',
  `config_value` text COMMENT '配置项值',
  `config_desc` varchar(500) DEFAULT NULL COMMENT '配置项描述',
  `category` varchar(50) DEFAULT 'system' COMMENT '配置分类',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

INSERT IGNORE INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`) VALUES
('AI功能开关', 'ai.enabled', 'true', '控制AI分析功能的启用与禁用', 'ai', 1),
('AI API密钥', 'ai.api-key', '', 'OpenRouter API密钥', 'ai', 2),
('AI模型', 'ai.model', 'deepseek/deepseek-chat', 'AI分析使用的模型', 'ai', 3),
('AI接口地址', 'ai.base-url', 'https://openrouter.ai/api/v1', 'AI接口基础地址', 'ai', 4),
('GLM模型开关', 'ai.glm.enabled', 'true', 'GLM模型优先调用开关', 'ai', 5),
('GLM API密钥', 'ai.glm.api-key', '', '智谱GLM API密钥', 'ai', 6),
('GLM模型名称', 'ai.glm.model', 'glm-4.7-flash', 'GLM模型名称', 'ai', 7),
('GLM接口地址', 'ai.glm.base-url', 'https://open.bigmodel.cn/api/paas/v4', 'GLM接口基础地址', 'ai', 8),
('内容审核模式', 'content.audit-mode', 'auto', '内容审核模式: auto-自动, manual-人工', 'content', 1),
('屏蔽词列表', 'content.block-words', '', '屏蔽词列表，逗号分隔', 'content', 2),
('全局通知开关', 'notification.enabled', 'true', '全局通知开关', 'notification', 1),
('新用户注册开关', 'registration.enabled', 'true', '新用户注册开关', 'registration', 1),
('小程序版本号', 'app.version', '1.0.0', '小程序版本号', 'system', 1);

-- ========================================
-- V3.0 AI模型管理系统
-- ========================================

CREATE TABLE IF NOT EXISTS `ai_model` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `model_name` varchar(100) NOT NULL COMMENT '模型标识名(如deepseek/deepseek-chat)',
  `display_name` varchar(100) NOT NULL COMMENT '模型显示名称',
  `provider` varchar(50) NOT NULL COMMENT '模型提供商: openrouter/zhipu/openai/custom',
  `base_url` varchar(255) NOT NULL COMMENT 'API基础地址',
  `api_key` varchar(500) NOT NULL COMMENT 'API密钥',
  `model_version` varchar(50) DEFAULT NULL COMMENT '模型版本号',
  `parameters` json DEFAULT NULL COMMENT '模型参数配置(temperature/max_tokens等)',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
  `is_default` tinyint(4) DEFAULT 0 COMMENT '是否为当前活动模型: 1-是 0-否',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `description` varchar(500) DEFAULT NULL COMMENT '模型描述',
  `icon` varchar(50) DEFAULT NULL COMMENT '模型图标emoji',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_default` (`is_default`),
  KEY `idx_provider` (`provider`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置表';

CREATE TABLE IF NOT EXISTS `ai_model_switch_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_model_id` bigint(20) DEFAULT NULL COMMENT '切换前模型ID',
  `from_model_name` varchar(100) DEFAULT NULL COMMENT '切换前模型名称',
  `to_model_id` bigint(20) NOT NULL COMMENT '切换后模型ID',
  `to_model_name` varchar(100) NOT NULL COMMENT '切换后模型名称',
  `switch_type` varchar(20) NOT NULL COMMENT '切换类型: manual-手动/auto-自动',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作者ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作者名称',
  `reason` varchar(500) DEFAULT NULL COMMENT '切换原因',
  `status` varchar(20) NOT NULL COMMENT '切换状态: success/failed',
  `duration` bigint(20) DEFAULT NULL COMMENT '切换耗时(毫秒)',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_to_model_id` (`to_model_id`),
  KEY `idx_switch_type` (`switch_type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型切换日志表';

CREATE TABLE IF NOT EXISTS `ai_model_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `model_id` bigint(20) NOT NULL COMMENT '模型ID',
  `stats_date` date NOT NULL COMMENT '统计日期',
  `call_count` bigint(20) DEFAULT 0 COMMENT '调用次数',
  `success_count` bigint(20) DEFAULT 0 COMMENT '成功次数',
  `fail_count` bigint(20) DEFAULT 0 COMMENT '失败次数',
  `total_response_time` bigint(20) DEFAULT 0 COMMENT '总响应时间(毫秒)',
  `avg_response_time` double DEFAULT NULL COMMENT '平均响应时间(毫秒)',
  `success_rate` double DEFAULT NULL COMMENT '成功率(%)',
  `min_response_time` bigint(20) DEFAULT NULL COMMENT '最小响应时间(毫秒)',
  `max_response_time` bigint(20) DEFAULT NULL COMMENT '最大响应时间(毫秒)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_model_date` (`model_id`, `stats_date`),
  KEY `idx_stats_date` (`stats_date`),
  KEY `idx_model_id` (`model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型性能统计表';

INSERT IGNORE INTO `ai_model` (`model_name`, `display_name`, `provider`, `base_url`, `api_key`, `model_version`, `parameters`, `status`, `is_default`, `sort_order`, `description`, `icon`) VALUES
('deepseek/deepseek-chat', 'DeepSeek 智能分析', 'openrouter', 'https://openrouter.ai/api/v1', '', 'v3', '{"temperature": 0.7, "max_tokens": 300}', 1, 1, 1, '基于DeepSeek大模型的宠物健康智能分析，擅长综合评估与建议', '🧠'),
('glm-4-flash', '智谱GLM快速分析', 'zhipu', 'https://open.bigmodel.cn/api/paas/v4', '', 'v4', '{"temperature": 0.6, "max_tokens": 250}', 1, 0, 2, '基于智谱GLM-4-Flash的快速健康分析，响应速度快', '⚡'),
('openrouter/free', '通用免费模型', 'openrouter', 'https://openrouter.ai/api/v1', '', 'free', '{"temperature": 0.7, "max_tokens": 200}', 1, 0, 3, '免费通用模型，适合基础健康分析需求', '🎁');

-- V2__add_achievements_and_membership.sql
-- 成就系统扩展：新增成长类成就和更多条件类型

INSERT IGNORE INTO `achievements` (`name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`) VALUES
('打卡王者', '累计打卡 100 天', '👑', 1, 'checkin_count', 100, 7),
('打卡传奇', '累计打卡 365 天', '🏆', 1, 'checkin_count', 365, 8),
('铁人精神', '连续打卡 30 天', '💪', 1, 'checkin_streak', 30, 9),
('百日坚持', '连续打卡 100 天', '🎯', 1, 'checkin_streak', 100, 10),
('健康先锋', '记录 10 次健康数据', '📝', 2, 'health_record_count', 10, 11),
('健康专家', '记录 100 次健康数据', '🩺', 2, 'health_record_count', 100, 12),
('初出茅庐', '发布第 1 条动态', '✨', 3, 'post_count', 1, 13),
('动态达人', '发布 50 条动态', '📢', 3, 'post_count', 50, 14),
('小有名气', '获得 10 个赞', '👍', 3, 'like_received', 10, 15),
('万众瞩目', '获得 500 个赞', '🌟', 3, 'like_received', 500, 16),
('萌宠之家', '添加第 1 只宠物', '🐾', 4, 'pet_count', 1, 17),
('多宠家庭', '添加 3 只宠物', '🏠', 4, 'pet_count', 3, 18),
('宠物达人', '添加 5 只宠物', '🏡', 4, 'pet_count', 5, 19),
('互动新手', '发表第 1 条评论', '💬', 4, 'comment_count', 1, 20),
('互动达人', '发表 50 条评论', '🗣️', 4, 'comment_count', 50, 21),
('受人关注', '获得 10 个粉丝', '👥', 4, 'follower_count', 10, 22),
('人气之星', '获得 100 个粉丝', '⭐', 4, 'follower_count', 100, 23);

-- 微信支付配置表
CREATE TABLE IF NOT EXISTS `sys_pay_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` varchar(500) DEFAULT NULL COMMENT '配置值',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付配置表';

-- 挑战赛表
CREATE TABLE IF NOT EXISTS `challenges` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '挑战标题',
  `description` varchar(500) DEFAULT NULL COMMENT '挑战描述',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图',
  `type` tinyint(4) DEFAULT 1 COMMENT '类型：1-打卡 2-社交 3-健康',
  `start_date` datetime NOT NULL COMMENT '开始时间',
  `end_date` datetime NOT NULL COMMENT '结束时间',
  `condition_type` varchar(50) DEFAULT NULL COMMENT '条件类型',
  `condition_value` int(11) DEFAULT NULL COMMENT '条件值',
  `reward_description` varchar(200) DEFAULT NULL COMMENT '奖励描述',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态：0-下线 1-进行中 2-已结束',
  `participant_count` int(11) DEFAULT 0 COMMENT '参与人数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_dates` (`start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挑战赛表';

-- 挑战赛参与记录表
CREATE TABLE IF NOT EXISTS `challenge_participants` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `challenge_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `progress` int(11) DEFAULT 0 COMMENT '当前进度',
  `completed` tinyint(1) DEFAULT 0 COMMENT '是否完成',
  `completed_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_challenge_user` (`challenge_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挑战赛参与记录表';

-- V3__add_commerce_features.sql
-- 商业化功能：宠物商城、医院预约

CREATE TABLE IF NOT EXISTS `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL COMMENT '商品名称',
  `description` text COMMENT '商品描述',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图',
  `images` text COMMENT '商品图片JSON数组',
  `category` varchar(50) DEFAULT NULL COMMENT '分类：food/toy/health/supplies',
  `brand` varchar(100) DEFAULT NULL COMMENT '品牌',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `commission_rate` decimal(5,4) DEFAULT 0.0000 COMMENT '佣金比例',
  `source_url` varchar(500) DEFAULT NULL COMMENT '来源链接',
  `source_platform` varchar(50) DEFAULT NULL COMMENT '来源平台：jd/tb/pdd',
  `sales_count` int(11) DEFAULT 0 COMMENT '销量',
  `rating` decimal(3,2) DEFAULT 5.00 COMMENT '评分',
  `pet_type` tinyint(4) DEFAULT 0 COMMENT '适用宠物类型：0-通用 1-猫 2-狗',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态：0-下架 1-上架',
  `sort_order` int(11) DEFAULT 0,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_pet_type` (`pet_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE IF NOT EXISTS `vet_clinics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL COMMENT '医院名称',
  `description` text COMMENT '医院描述',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图',
  `address` varchar(500) DEFAULT NULL COMMENT '地址',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `phone` varchar(50) DEFAULT NULL COMMENT '电话',
  `business_hours` varchar(200) DEFAULT NULL COMMENT '营业时间',
  `rating` decimal(3,2) DEFAULT 5.00 COMMENT '评分',
  `specialties` varchar(500) DEFAULT NULL COMMENT '专科：逗号分隔',
  `is_partner` tinyint(1) DEFAULT 0 COMMENT '是否合作医院',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态：0-关闭 1-营业',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_location` (`latitude`, `longitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物医院表';

CREATE TABLE IF NOT EXISTS `vet_appointments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `clinic_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) DEFAULT NULL,
  `appointment_date` date NOT NULL COMMENT '预约日期',
  `appointment_time` varchar(20) NOT NULL COMMENT '预约时段',
  `symptom` varchar(500) DEFAULT NULL COMMENT '症状描述',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态：0-待确认 1-已确认 2-已完成 3-已取消',
  `cancel_reason` varchar(200) DEFAULT NULL COMMENT '取消原因',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_clinic_id` (`clinic_id`),
  KEY `idx_date` (`appointment_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医院预约表';


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

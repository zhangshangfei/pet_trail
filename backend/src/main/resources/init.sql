-- ============================================================
-- 宠迹项目 - 数据库完整初始化脚本
-- 更新日期: 2026-04-28
-- ============================================================

CREATE DATABASE IF NOT EXISTS pet_trail DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_trail;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 一、用户模块
-- ============================================================

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '微信OpenID',
  `unionid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信UnionID',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `gender` tinyint(4) NULL DEFAULT NULL COMMENT '性别: 0-未知, 1-男, 2-女',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态: 1-正常 0-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `openid`(`openid`) USING BTREE,
  UNIQUE INDEX `unionid`(`unionid`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `follows`;
CREATE TABLE `follows` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `follower_id` bigint(20) NOT NULL COMMENT '关注者ID',
  `followee_id` bigint(20) NOT NULL COMMENT '被关注者ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_follower_followee`(`follower_id`, `followee_id`) USING BTREE,
  INDEX `idx_follower_id`(`follower_id`) USING BTREE,
  INDEX `idx_followee_id`(`followee_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '关注表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `user_achievements`;
CREATE TABLE `user_achievements` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `achievement_id` bigint(20) NOT NULL,
  `unlocked_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：1-未完成 2-已完成 3-已领取',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_achievement`(`user_id`, `achievement_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户成就表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `user_behaviors`;
CREATE TABLE `user_behaviors` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `action` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '行为类型：view-浏览, like-点赞, comment-评论, share-分享, follow-关注, collect-收藏, publish-发布',
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标类型：post/user/pet',
  `target_id` bigint(20) NULL DEFAULT NULL COMMENT '目标ID',
  `duration` int(11) NULL DEFAULT 0 COMMENT '停留时长(秒)，仅浏览行为有值',
  `extra` json NULL COMMENT '额外数据',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_action`(`action`) USING BTREE,
  INDEX `idx_target`(`target_type`, `target_id`) USING BTREE,
  INDEX `idx_user_action`(`user_id`, `action`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户行为追踪表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `user_hidden_items`;
CREATE TABLE `user_hidden_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `item_id` bigint(20) NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_item`(`user_id`, `item_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户隐藏的打卡项' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `user_membership`;
CREATE TABLE `user_membership` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `plan` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'monthly',
  `amount` decimal(10, 2) NOT NULL,
  `start_date` datetime NOT NULL,
  `expire_date` datetime NOT NULL,
  `status` int(11) NOT NULL DEFAULT 1 COMMENT '0-已取消 1-有效 2-已续费',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_expire_date`(`expire_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ============================================================
-- 二、宠物模块
-- ============================================================

DROP TABLE IF EXISTS `pets`;
CREATE TABLE `pets` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '所属用户ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '宠物名称',
  `breed` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '品种',
  `gender` tinyint(4) NULL DEFAULT NULL COMMENT '性别: 0-未知, 1-公, 2-母',
  `sterilized` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否绝育: 0-未绝育, 1-已绝育',
  `category` tinyint(4) NOT NULL DEFAULT 0 COMMENT '类别: 0-其他, 1-猫, 2-狗',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像URL',
  `weight` decimal(5, 2) NULL DEFAULT NULL COMMENT '体重(kg)',
  `color` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '毛色',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '宠物表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `pet_album`;
CREATE TABLE `pet_album` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) NOT NULL,
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `note` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `record_date` date NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pet_id`(`pet_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_record_date`(`record_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `weight_records`;
CREATE TABLE `weight_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pet_id` bigint(20) NOT NULL COMMENT '宠物ID',
  `weight` decimal(5, 2) NOT NULL COMMENT '体重(kg)',
  `record_date` date NOT NULL COMMENT '记录日期',
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pet_date`(`pet_id`, `record_date`) USING BTREE,
  INDEX `idx_pet_id`(`pet_id`) USING BTREE,
  INDEX `idx_record_date`(`record_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '体重记录表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 三、健康记录模块
-- ============================================================

DROP TABLE IF EXISTS `health_daily_stats`;
CREATE TABLE `health_daily_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) NULL DEFAULT NULL,
  `stat_date` date NOT NULL,
  `total_steps` int(11) NULL DEFAULT 0,
  `total_water` decimal(10, 2) NULL DEFAULT 0.00,
  `weight` decimal(5, 2) NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_pet_date`(`user_id`, `pet_id`, `stat_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '健康统计表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `step_records`;
CREATE TABLE `step_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) NULL DEFAULT NULL,
  `steps` int(11) NOT NULL COMMENT '步数',
  `distance` decimal(10, 2) NULL DEFAULT NULL COMMENT '距离 (km)',
  `record_date` date NOT NULL,
  `source` tinyint(4) NULL DEFAULT 1 COMMENT '来源：1-手动 2-设备同步',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_pet_date`(`user_id`, `pet_id`, `record_date`) USING BTREE,
  INDEX `idx_record_date`(`record_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '步数记录表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `water_records`;
CREATE TABLE `water_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) NULL DEFAULT NULL,
  `amount` decimal(10, 2) NOT NULL COMMENT '水量 (ml)',
  `record_date` date NOT NULL,
  `record_time` time NULL DEFAULT NULL COMMENT '记录时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_date`(`user_id`, `record_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '饮水记录表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 四、打卡模块
-- ============================================================

DROP TABLE IF EXISTS `checkin_items`;
CREATE TABLE `checkin_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID，NULL表示系统默认',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '打卡项名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `type` tinyint(4) NULL DEFAULT 1 COMMENT '类型：1-日常 2-健康 3-训练',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '说明',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认',
  `is_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '打卡项表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `checkin_records`;
CREATE TABLE `checkin_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) NULL DEFAULT NULL,
  `item_id` bigint(20) NOT NULL,
  `record_date` date NOT NULL,
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：1-完成 2-取消',
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `images` json NULL COMMENT '图片',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_item_date`(`user_id`, `item_id`, `record_date`, `pet_id`) USING BTREE,
  INDEX `idx_record_date`(`record_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 66 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '打卡记录表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `checkin_stats`;
CREATE TABLE `checkin_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `item_id` bigint(20) NOT NULL,
  `total_count` int(11) NULL DEFAULT 0 COMMENT '累计次数',
  `current_streak` int(11) NULL DEFAULT 0 COMMENT '当前连续天数',
  `max_streak` int(11) NULL DEFAULT 0 COMMENT '最大连续天数',
  `last_checkin_date` date NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_item`(`user_id`, `item_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '打卡统计表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 五、提醒模块
-- ============================================================

DROP TABLE IF EXISTS `checkin_reminders`;
CREATE TABLE `checkin_reminders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `item_id` bigint(20) NULL DEFAULT NULL COMMENT '打卡项ID，NULL表示全部',
  `remind_time` time NOT NULL COMMENT '提醒时间',
  `is_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '打卡提醒设置表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `feeding_reminders`;
CREATE TABLE `feeding_reminders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) NOT NULL,
  `meal_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'breakfast',
  `time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `repeat_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'daily',
  `note` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_pet_id`(`pet_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `vaccine_reminders`;
CREATE TABLE `vaccine_reminders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pet_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `vaccine_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '疫苗名称',
  `vaccine_type` tinyint(4) NULL DEFAULT 1 COMMENT '类型：1-核心疫苗 2-非核心',
  `next_date` date NOT NULL COMMENT '下次接种日期',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态：0-待接种 1-已接种 2-已取消',
  `reminder_days` int(11) NULL DEFAULT 7 COMMENT '提前几天提醒',
  `is_notified` tinyint(1) NULL DEFAULT 0 COMMENT '是否已通知',
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pet_id`(`pet_id`) USING BTREE,
  INDEX `idx_next_date`(`next_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '疫苗提醒表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `parasite_reminders`;
CREATE TABLE `parasite_reminders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pet_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `type` tinyint(4) NOT NULL COMMENT '类型：1-体内驱虫 2-体外驱虫',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '产品名称',
  `next_date` date NOT NULL,
  `interval_days` int(11) NULL DEFAULT 30 COMMENT '间隔天数',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态：0-待驱虫 1-已完成 2-已取消',
  `is_notified` tinyint(1) NULL DEFAULT 0,
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pet_id`(`pet_id`) USING BTREE,
  INDEX `idx_next_date`(`next_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '驱虫提醒表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 六、社区/动态模块
-- ============================================================

DROP TABLE IF EXISTS `posts`;
CREATE TABLE `posts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '发布者 ID',
  `pet_id` bigint(20) NULL DEFAULT NULL COMMENT '关联宠物 ID',
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容',
  `videos` json NULL COMMENT '视频 URL 数组',
  `images` json NULL COMMENT '图片 URL 数组',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int(11) NULL DEFAULT 0 COMMENT '评论数',
  `share_count` int(11) NULL DEFAULT 0 COMMENT '分享数',
  `ee_count` int(11) NULL DEFAULT 0 COMMENT '收藏数',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-审核中 1-正常 2-删除',
  `audit_status` tinyint(4) NULL DEFAULT 1 COMMENT '审核状态: 0-待审核 1-通过 2-拒绝',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核备注',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '是否删除: 0-未删除 1-已删除',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '位置信息',
  `stickers` json NULL COMMENT '贴纸列表',
  `bubble` json NULL COMMENT '文字气泡',
  `tags` json NULL COMMENT '标签列表',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE,
  INDEX `idx_status_created`(`status`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '动态表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `post_comments`;
CREATE TABLE `post_comments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `parent_id` bigint(20) NULL DEFAULT NULL COMMENT '父评论 ID',
  `reply_to_id` bigint(20) NULL DEFAULT NULL COMMENT '回复的评论 ID',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论内容',
  `images` json NULL COMMENT '评论图片',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：1-正常 2-删除',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE,
  INDEX `idx_user_status`(`user_id`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '动态评论表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `post_likes`;
CREATE TABLE `post_likes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id`, `user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '动态点赞表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `post_ee`;
CREATE TABLE `post_ee` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_user_post`(`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '收藏表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签名称',
  `usage_count` int(11) NULL DEFAULT 0 COMMENT '使用次数',
  `is_hot` tinyint(1) NULL DEFAULT 0 COMMENT '是否热门',
  `is_official` tinyint(1) NULL DEFAULT 0 COMMENT '是否官方标签',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name`) USING BTREE,
  INDEX `idx_usage_count`(`usage_count`) USING BTREE,
  INDEX `idx_is_hot`(`is_hot`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '话题标签表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `post_tags`;
CREATE TABLE `post_tags` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL COMMENT '动态ID',
  `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_tag`(`post_id`, `tag_id`) USING BTREE,
  INDEX `idx_tag_id`(`tag_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '动态-标签关联表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `reports`;
CREATE TABLE `reports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `reporter_id` bigint(20) NOT NULL COMMENT '举报人ID',
  `target_id` bigint(20) NOT NULL COMMENT '被举报目标ID',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标类型：post/user/comment',
  `reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '举报原因',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '补充描述',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态：0-待处理 1-已处理 2-已驳回',
  `result` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '处理结果',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_reporter_target`(`reporter_id`, `target_id`, `target_type`) USING BTREE,
  INDEX `idx_target`(`target_type`, `target_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '举报表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 七、成就/挑战模块
-- ============================================================

DROP TABLE IF EXISTS `achievements`;
CREATE TABLE `achievements` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '成就名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `type` tinyint(4) NULL DEFAULT 1 COMMENT '类型：1-打卡 2-健康 3-社交 4-成长',
  `condition_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '条件类型',
  `condition_value` int(11) NULL DEFAULT NULL COMMENT '条件值',
  `sort_order` int(11) NULL DEFAULT 0,
  `is_enabled` tinyint(1) NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '成就表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `challenges`;
CREATE TABLE `challenges` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '挑战标题',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '挑战描述',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
  `type` tinyint(4) NULL DEFAULT 1 COMMENT '类型：1-打卡 2-社交 3-健康',
  `start_date` datetime NOT NULL COMMENT '开始时间',
  `end_date` datetime NOT NULL COMMENT '结束时间',
  `condition_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '条件类型',
  `condition_value` int(11) NULL DEFAULT NULL COMMENT '条件值',
  `reward_description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '奖励描述',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-下线 1-进行中 2-已结束',
  `participant_count` int(11) NULL DEFAULT 0 COMMENT '参与人数',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_dates`(`start_date`, `end_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '挑战赛表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `challenge_participants`;
CREATE TABLE `challenge_participants` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `challenge_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `progress` int(11) NULL DEFAULT 0 COMMENT '当前进度',
  `completed` tinyint(1) NULL DEFAULT 0 COMMENT '是否完成',
  `completed_at` datetime NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_challenge_user`(`challenge_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '挑战赛参与记录表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 八、通知模块
-- ============================================================

DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '接收通知的用户ID',
  `from_user_id` bigint(20) NOT NULL COMMENT '触发通知的用户ID',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知类型：like-点赞, comment-评论, follow-关注, system-系统',
  `target_id` bigint(20) NULL DEFAULT NULL COMMENT '关联目标ID（动态ID/评论ID等）',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通知内容',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通知标题',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读：0-未读, 1-已读',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_user_read`(`user_id`, `is_read`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 167 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 九、医院/预约模块
-- ============================================================

DROP TABLE IF EXISTS `vet_clinics`;
CREATE TABLE `vet_clinics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '医院名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '医院描述',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地址',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '电话',
  `business_hours` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业时间',
  `rating` decimal(3, 2) NULL DEFAULT 5.00 COMMENT '评分',
  `specialties` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '专科：逗号分隔',
  `is_partner` tinyint(1) NULL DEFAULT 0 COMMENT '是否合作医院',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-关闭 1-营业',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_location`(`latitude`, `longitude`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '宠物医院表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `vet_appointments`;
CREATE TABLE `vet_appointments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `clinic_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) NULL DEFAULT NULL,
  `appointment_date` date NOT NULL COMMENT '预约日期',
  `appointment_time` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '预约时段',
  `symptom` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '症状描述',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态：0-待确认 1-已确认 2-已完成 3-已取消',
  `cancel_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '取消原因',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_clinic_id`(`clinic_id`) USING BTREE,
  INDEX `idx_date`(`appointment_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '医院预约表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 十、商品/订单模块
-- ============================================================

DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '商品描述',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '商品图片JSON数组',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类：food/toy/health/supplies',
  `brand` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '品牌',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `commission_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '佣金比例',
  `source_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '来源链接',
  `source_platform` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '来源平台：jd/tb/pdd',
  `sales_count` int(11) NULL DEFAULT 0 COMMENT '销量',
  `rating` decimal(3, 2) NULL DEFAULT 5.00 COMMENT '评分',
  `pet_type` tinyint(4) NULL DEFAULT 0 COMMENT '适用宠物类型：0-通用 1-猫 2-狗',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-下架 1-上架',
  `sort_order` int(11) NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_pet_type`(`pet_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `plan` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'pro_month/pro_year',
  `amount` decimal(10, 2) NOT NULL,
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '0-待支付 1-已支付 2-已退款 3-已取消',
  `wx_transaction_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `paid_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 十一、AI 模块
-- ============================================================

DROP TABLE IF EXISTS `ai_model`;
CREATE TABLE `ai_model` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模型标识名(如deepseek/deepseek-chat)',
  `display_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模型显示名称',
  `provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模型提供商: openrouter/zhipu/openai/custom',
  `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'API基础地址',
  `api_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'API密钥',
  `model_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型版本号',
  `parameters` json NULL COMMENT '模型参数配置(temperature/max_tokens等)',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
  `is_default` tinyint(4) NULL DEFAULT 0 COMMENT '是否为当前活动模型: 1-是 0-否',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型描述',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型图标emoji',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_is_default`(`is_default`) USING BTREE,
  INDEX `idx_provider`(`provider`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI模型配置表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `ai_model_stats`;
CREATE TABLE `ai_model_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `model_id` bigint(20) NOT NULL COMMENT '模型ID',
  `stats_date` date NOT NULL COMMENT '统计日期',
  `call_count` bigint(20) NULL DEFAULT 0 COMMENT '调用次数',
  `success_count` bigint(20) NULL DEFAULT 0 COMMENT '成功次数',
  `fail_count` bigint(20) NULL DEFAULT 0 COMMENT '失败次数',
  `total_response_time` bigint(20) NULL DEFAULT 0 COMMENT '总响应时间(毫秒)',
  `avg_response_time` double NULL DEFAULT NULL COMMENT '平均响应时间(毫秒)',
  `success_rate` double NULL DEFAULT NULL COMMENT '成功率(%)',
  `min_response_time` bigint(20) NULL DEFAULT NULL COMMENT '最小响应时间(毫秒)',
  `max_response_time` bigint(20) NULL DEFAULT NULL COMMENT '最大响应时间(毫秒)',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_model_date`(`model_id`, `stats_date`) USING BTREE,
  INDEX `idx_stats_date`(`stats_date`) USING BTREE,
  INDEX `idx_model_id`(`model_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI模型性能统计表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `ai_model_switch_log`;
CREATE TABLE `ai_model_switch_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_model_id` bigint(20) NULL DEFAULT NULL COMMENT '切换前模型ID',
  `from_model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '切换前模型名称',
  `to_model_id` bigint(20) NOT NULL COMMENT '切换后模型ID',
  `to_model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '切换后模型名称',
  `switch_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '切换类型: manual-手动/auto-自动',
  `operator_id` bigint(20) NULL DEFAULT NULL COMMENT '操作者ID',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作者名称',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '切换原因',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '切换状态: success/failed',
  `duration` bigint(20) NULL DEFAULT NULL COMMENT '切换耗时(毫秒)',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '错误信息',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_to_model_id`(`to_model_id`) USING BTREE,
  INDEX `idx_switch_type`(`switch_type`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI模型切换日志表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 十二、管理员/权限模块
-- ============================================================

DROP TABLE IF EXISTS `admins`;
CREATE TABLE `admins` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码(BCrypt)',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像URL',
  `role_id` bigint(20) NULL DEFAULT NULL COMMENT '角色ID, 关联sys_role表',
  `merchant_id` bigint(20) NULL DEFAULT NULL COMMENT '所属商户ID',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
  `last_login_at` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `admin_operation_logs`;
CREATE TABLE `admin_operation_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_id` bigint(20) NULL DEFAULT NULL COMMENT '操作管理员ID',
  `admin_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作管理员名称',
  `module` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作模块',
  `action` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作动作',
  `target_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标类型',
  `target_id` bigint(20) NULL DEFAULT NULL COMMENT '目标ID',
  `detail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作详情',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_id`(`admin_id`) USING BTREE,
  INDEX `idx_module`(`module`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '管理员操作日志' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色编码',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统角色表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父菜单ID, 0为顶级',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由路径',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联权限码',
  `buttons` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '可用按钮权限码',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统菜单表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `buttons` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '按钮权限码,逗号分隔',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色菜单关联表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `merchants`;
CREATE TABLE `merchants` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户名称',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'vet_clinic' COMMENT '商户类型: vet_clinic, pet_shop, other',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商户表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 十三、系统配置模块
-- ============================================================

DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置项名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置项键名',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置项值',
  `config_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置项描述',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'system' COMMENT '配置分类',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_settings`;
CREATE TABLE `system_settings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `setting_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设置键',
  `setting_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '设置值',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_setting_key`(`setting_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统设置' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sys_pay_config`;
CREATE TABLE `sys_pay_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置值',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付配置表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 十四、微信模块
-- ============================================================

DROP TABLE IF EXISTS `wx_subscribe_authorization`;
CREATE TABLE `wx_subscribe_authorization` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `template_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板类型: checkin/vaccine/parasite/feeding',
  `credits` int(11) NOT NULL DEFAULT 0 COMMENT '剩余可用授权次数',
  `used_credits` int(11) NOT NULL DEFAULT 0 COMMENT '已使用授权次数',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_template`(`user_id`, `template_type`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '微信订阅消息授权积分表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 十五、反馈模块
-- ============================================================

DROP TABLE IF EXISTS `feedbacks`;
CREATE TABLE `feedbacks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'other' COMMENT '类型：bug/feature/experience/other',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '反馈内容',
  `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系方式',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '截图',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态：0-待处理 1-已处理',
  `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '回复',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '意见反馈' ROW_FORMAT = Dynamic;

-- ============================================================
-- 初始数据
-- ============================================================

INSERT INTO `admins` (`id`, `username`, `password`, `nickname`, `role_id`, `status`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 1, 1);

INSERT INTO `checkin_items` (`id`, `user_id`, `name`, `icon`, `type`, `description`, `sort_order`, `is_default`, `is_enabled`) VALUES
(1, NULL, '喂食', '🍖', 1, '按时喂食', 1, 1, 1),
(2, NULL, '遛狗', '🦮', 1, '出门遛狗', 2, 1, 1),
(3, NULL, '喝水', '💧', 2, '补充水分', 3, 1, 1),
(4, NULL, '洗澡', '🛁', 1, '洗澡清洁', 4, 1, 1),
(5, NULL, '梳毛', '✂️', 1, '梳理毛发', 5, 1, 1),
(6, NULL, '训练', '🎯', 3, '技能训练', 6, 1, 1),
(7, NULL, '刷牙', '🪥', 2, '口腔清洁', 7, 1, 1),
(8, NULL, '吃药', '💊', 2, '按时服药', 8, 1, 1),
(9, NULL, '量体温', '🌡️', 2, '监测体温', 9, 1, 1),
(10, NULL, '称体重', '⚖️', 2, '体重记录', 10, 1, 1),
(11, NULL, '剪指甲', '💅', 1, '指甲修剪', 11, 1, 1),
(12, NULL, '清猫砂', '🧹', 1, '清理猫砂盆', 12, 1, 1),
(13, NULL, '社交', '🐕', 3, '宠物社交活动', 13, 1, 1),
(14, NULL, '散步', '🚶', 1, '户外散步', 14, 1, 1),
(15, NULL, '玩耍', '🎾', 3, '互动游戏', 15, 1, 1),
(16, NULL, '拍照', '📸', 1, '记录美好瞬间', 16, 1, 1);

INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (1, '新手上路', '完成第一次打卡', '🌱', 1, 'checkin_count', 1, 1, 1, '2026-04-10 20:51:28');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (2, '坚持不懈', '连续打卡 7 天', '🔥', 1, 'checkin_streak', 7, 2, 1, '2026-04-10 20:51:28');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (3, '打卡达人', '累计打卡 30 天', '⭐', 1, 'checkin_count', 30, 3, 1, '2026-04-10 20:51:28');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (4, '健康卫士', '记录 30 次健康数据', '💪', 2, 'health_record_count', 30, 4, 1, '2026-04-10 20:51:28');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (5, '社交达人', '发布 10 条动态', '📱', 3, 'post_count', 10, 5, 1, '2026-04-10 20:51:28');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (6, '人气王', '获得 100 个赞', '❤️', 3, 'like_received', 100, 6, 1, '2026-04-10 20:51:28');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (7, '打卡王者', '累计打卡 100 天', '👑', 1, 'checkin_count', 100, 7, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (8, '打卡传奇', '累计打卡 365 天', '🏆', 1, 'checkin_count', 365, 8, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (9, '铁人精神', '连续打卡 30 天', '💪', 1, 'checkin_streak', 30, 9, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (10, '百日坚持', '连续打卡 100 天', '🎯', 1, 'checkin_streak', 100, 10, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (11, '健康先锋', '记录 10 次健康数据', '📝', 2, 'health_record_count', 10, 11, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (12, '健康专家', '记录 100 次健康数据', '🩺', 2, 'health_record_count', 100, 12, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (13, '初出茅庐', '发布第 1 条动态', '✨', 3, 'post_count', 1, 13, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (14, '动态达人', '发布 50 条动态', '📢', 3, 'post_count', 50, 14, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (15, '小有名气', '获得 10 个赞', '👍', 3, 'like_received', 10, 15, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (16, '万众瞩目', '获得 500 个赞', '🌟', 3, 'like_received', 500, 16, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (17, '萌宠之家', '添加第 1 只宠物', '🐾', 4, 'pet_count', 1, 17, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (18, '多宠家庭', '添加 3 只宠物', '🏠', 4, 'pet_count', 3, 18, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (19, '宠物达人', '添加 5 只宠物', '🏡', 4, 'pet_count', 5, 19, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (20, '互动新手', '发表第 1 条评论', '💬', 4, 'comment_count', 1, 20, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (21, '互动达人', '发表 50 条评论', '🗣️', 4, 'comment_count', 50, 21, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (22, '受人关注', '获得 10 个粉丝', '👥', 4, 'follower_count', 10, 22, 1, '2026-04-23 09:29:44');
INSERT INTO `pet_trail`.`achievements` (`id`, `name`, `description`, `icon`, `type`, `condition_type`, `condition_value`, `sort_order`, `is_enabled`, `created_at`) VALUES (23, '人气之星', '获得 100 个粉丝', '⭐', 4, 'follower_count', 100, 23, 1, '2026-04-23 09:29:44');



INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `icon`, `permission`, `buttons`, `sort_order`) VALUES
(1,  0, '仪表盘',     '/dashboard',     'DataAnalysis',  'dashboard',        NULL,                            1),
(2,  0, '用户与宠物',  NULL,             'User',          NULL,               NULL,                            2),
(3,  2, '用户管理',    '/users',         'User',          'user:view',        'user:manage,export',            1),
(4,  2, '宠物管理',    '/pets',          'Guide',         'pet:view',         'pet:manage,export',             2),
(5,  0, '内容管理',    NULL,             'Document',      NULL,               NULL,                            3),
(6,  5, '动态管理',    '/posts',         'Document',      'post:view',        'post:manage,export',            1),
(7,  5, '评论管理',    '/comments',      'ChatDotRound',  'comment:view',     'comment:manage',                2),
(8,  0, '运营与互动',  NULL,             'Bell',          NULL,               NULL,                            4),
(9,  8, '举报管理',    '/reports',       'Warning',       'report:view',      'report:handle,export',          1),
(10, 8, '通知管理',    '/notifications', 'Bell',          'notification:view', 'notification:send',            2),
(11, 8, '反馈管理',    '/feedbacks',     'ChatLineSquare','feedback:view',    'feedback:reply',                3),
(12, 8, '挑战赛配置',  '/challenges',    'Trophy',        'challenge:view',   'challenge:manage,export',       4),
(13, 0, '商业服务',    NULL,             'ShoppingCart',  NULL,               NULL,                            5),
(14, 13,'医院管理',    '/vet-clinics',   'FirstAidKit',   'vet-clinic:view',  'vet-clinic:manage',             1),
(15, 13,'商品管理',    '/products',      'ShoppingCart',  'product:view',     'product:manage,export',         2),
(16, 13,'商户管理',    '/merchants',     'OfficeBuilding','merchant:manage',  NULL,                            3),
(17, 0, '系统管理',    NULL,             'Setting',       NULL,               NULL,                            6),
(18, 17,'管理员管理',  '/admins',        'UserFilled',    'admin:manage',     NULL,                            1),
(19, 17,'操作日志',    '/logs',          'List',          'log:view',         'export',                        2),
(20, 17,'系统设置',    '/settings',      'Setting',       'setting:manage',   NULL,                            3),
(21, 17,'系统配置',    '/config',        'Tools',         'config:manage',    NULL,                            4),
(22, 17,'AI模型管理',  '/ai-models',     'Cpu',           'ai-model:view',    'ai-model:manage',               5),
(23, 17,'角色管理',    '/roles',         'Stamp',         'admin:manage',     NULL,                            6),
(24, 17,'菜单管理',    '/sys-menus',     'Menu',          'admin:manage',     NULL,                            7);

INSERT IGNORE INTO `sys_role` (`id`, `name`, `code`, `description`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有全部权限'),
(2, '平台管理员', 'ADMIN', '除系统管理外的所有权限'),
(3, '商户管理员', 'MERCHANT_ADMIN', '管理本商户的医院、商品、反馈'),
(4, '商户员工', 'MERCHANT_STAFF', '查看本商户的医院和商品');

INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (1, 1, 1, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (2, 1, 2, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (3, 1, 3, 'user:manage,export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (4, 1, 4, 'pet:manage,export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (5, 1, 5, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (6, 1, 6, 'post:manage,export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (7, 1, 7, 'comment:manage');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (8, 1, 8, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (9, 1, 9, 'report:handle,export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (10, 1, 10, 'notification:send');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (11, 1, 11, 'feedback:reply');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (12, 1, 12, 'challenge:manage,export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (13, 1, 13, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (14, 1, 14, 'vet-clinic:manage');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (15, 1, 15, 'product:manage,export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (16, 1, 16, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (17, 1, 17, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (18, 1, 18, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (19, 1, 19, 'export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (20, 1, 20, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (21, 1, 21, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (22, 1, 22, 'ai-model:manage');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (23, 1, 23, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (24, 1, 24, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (25, 2, 1, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (26, 2, 2, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (27, 2, 3, 'user:manage,export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (28, 2, 4, 'pet:manage,export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (29, 2, 5, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (30, 2, 6, 'post:manage,export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (31, 2, 7, 'comment:manage');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (32, 2, 8, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (33, 2, 9, 'report:handle,export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (34, 2, 10, 'notification:send');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (35, 2, 11, 'feedback:reply');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (36, 2, 12, 'challenge:manage,export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (37, 2, 13, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (38, 2, 14, 'vet-clinic:manage');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (39, 2, 15, 'product:manage,export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (40, 2, 16, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (41, 2, 17, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (42, 2, 19, 'export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (43, 2, 22, 'ai-model:manage');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (44, 3, 1, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (45, 3, 8, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (46, 3, 11, 'feedback:reply');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (47, 3, 13, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (48, 3, 14, 'vet-clinic:manage');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (49, 3, 15, 'product:manage,export');
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (50, 4, 1, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (51, 4, 13, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (52, 4, 14, NULL);
INSERT INTO `pet_trail`.`sys_role_menu` (`id`, `role_id`, `menu_id`, `buttons`) VALUES (53, 4, 15, NULL);


INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (1, 'AI功能开关', 'ai.enabled', 'true', '控制AI分析功能的启用与禁用', 'ai', 1, '2026-04-21 08:48:55', '2026-04-21 08:48:55');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (2, 'AI API密钥', 'ai.api-key', '', 'OpenRouter API密钥', 'ai', 2, '2026-04-21 08:48:55', '2026-04-21 16:57:08');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (3, 'AI模型', 'ai.model', 'openrouter/free', 'AI分析使用的模型', 'ai', 3, '2026-04-21 08:48:55', '2026-04-21 09:39:43');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (4, 'AI接口地址', 'ai.base-url', 'https://openrouter.ai/api/v1', 'AI接口基础地址', 'ai', 4, '2026-04-21 08:48:55', '2026-04-21 08:48:55');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (5, '内容审核模式', 'content.audit-mode', 'auto', '内容审核模式: auto-自动, manual-人工', 'content', 1, '2026-04-21 08:48:55', '2026-04-21 08:48:55');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (6, '屏蔽词列表', 'content.block-words', '', '屏蔽词列表，逗号分隔', 'content', 2, '2026-04-21 08:48:55', '2026-04-21 08:48:55');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (7, '全局通知开关', 'notification.enabled', 'true', '全局通知开关', 'notification', 1, '2026-04-21 08:48:55', '2026-04-21 08:48:55');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (8, '新用户注册开关', 'registration.enabled', 'true', '新用户注册开关', 'registration', 1, '2026-04-21 08:48:55', '2026-04-21 08:48:55');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (9, '小程序版本号', 'app.version', '1.0.0', '小程序版本号', 'system', 1, '2026-04-21 08:48:55', '2026-04-21 08:48:55');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (10, 'GLM模型开关', 'ai.glm.enabled', 'true', 'GLM模型优先调用开关', 'ai', 5, '2026-04-21 13:30:49', '2026-04-21 13:30:49');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (11, 'GLM API密钥', 'ai.glm.api-key', '', '智谱GLM API密钥', 'ai', 6, '2026-04-21 13:30:49', '2026-04-21 13:30:49');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (12, 'GLM模型名称', 'ai.glm.model', 'glm-4.7-flash', 'GLM模型名称', 'ai', 7, '2026-04-21 13:30:49', '2026-04-21 13:30:49');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (13, 'GLM接口地址', 'ai.glm.base-url', 'https://open.bigmodel.cn/api/paas/v4', 'GLM接口基础地址', 'ai', 8, '2026-04-21 13:30:49', '2026-04-21 13:30:49');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (14, '疫苗提醒模板ID', 'wx.subscribe.template.vaccine', 'vE9eOr4tT6BXWCeb6BI5C-k1CIhNiYCOQee05qvNvv0', '', 'notification', 0, '2026-04-25 23:02:19', '2026-04-26 21:51:17');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (15, '打卡提醒模板ID', 'wx.subscribe.template.checkin', 'bqWCrHEHhmVCKzW53sQ80y1Dmbe7gK6K4z8ofruvmRE', '', 'notification', 0, '2026-04-25 23:23:40', '2026-04-25 23:23:40');
INSERT INTO `pet_trail`.`sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_desc`, `category`, `sort_order`, `created_at`, `updated_at`) VALUES (16, '驱虫提醒模板ID', 'wx.subscribe.template.parasite', 's1dxeOREimpiBKBqNrsg1_NYKLxXJ8BT07dZ-2ApLSs', '', 'notification', 0, '2026-04-25 23:24:03', '2026-04-26 21:50:47');


INSERT INTO `pet_trail`.`system_settings` (`id`, `setting_key`, `setting_value`, `description`, `created_at`, `updated_at`) VALUES (1, 'content_audit_mode', 'auto', '内容审核模式: auto-自动, manual-人工', '2026-04-19 20:02:37', '2026-04-19 20:02:37');
INSERT INTO `pet_trail`.`system_settings` (`id`, `setting_key`, `setting_value`, `description`, `created_at`, `updated_at`) VALUES (2, 'audit_block_words', '', '屏蔽词列表，逗号分隔', '2026-04-19 20:02:37', '2026-04-19 20:02:37');
INSERT INTO `pet_trail`.`system_settings` (`id`, `setting_key`, `setting_value`, `description`, `created_at`, `updated_at`) VALUES (3, 'app_version', '1.0.0', '小程序版本号', '2026-04-19 20:02:37', '2026-04-19 20:02:37');
INSERT INTO `pet_trail`.`system_settings` (`id`, `setting_key`, `setting_value`, `description`, `created_at`, `updated_at`) VALUES (4, 'notification_enabled', 'true', '全局通知开关', '2026-04-19 20:02:37', '2026-04-19 20:02:37');
INSERT INTO `pet_trail`.`system_settings` (`id`, `setting_key`, `setting_value`, `description`, `created_at`, `updated_at`) VALUES (5, 'registration_enabled', 'true', '新用户注册开关', '2026-04-19 20:02:37', '2026-04-19 20:02:37');

INSERT INTO `pet_trail`.`ai_model` (`id`, `model_name`, `display_name`, `provider`, `base_url`, `api_key`, `model_version`, `parameters`, `status`, `is_default`, `sort_order`, `description`, `icon`, `created_at`, `updated_at`) VALUES (1, 'deepseek/deepseek-chat', 'DeepSeek 智能分析', 'openrouter', 'https://openrouter.ai/api/v1', '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', 'v3', '{\"max_tokens\": 300, \"temperature\": 0.7}', 1, 0, 1, '基于DeepSeek大模型的宠物健康智能分析，擅长综合评估与建议', '🧠', '2026-04-21 18:26:50', '2026-04-21 19:57:36');
INSERT INTO `pet_trail`.`ai_model` (`id`, `model_name`, `display_name`, `provider`, `base_url`, `api_key`, `model_version`, `parameters`, `status`, `is_default`, `sort_order`, `description`, `icon`, `created_at`, `updated_at`) VALUES (2, 'glm-4-flash', '智谱GLM快速分析', 'zhipu', 'https://open.bigmodel.cn/api/paas/v4', '9369496ba97b4fb08b1fdfbf7907b049.LdjmNyZ81m6mQSEp', 'v4', '{\"max_tokens\": 250, \"temperature\": 0.6}', 1, 1, 2, '基于智谱GLM-4-Flash的快速健康分析，响应速度快', '⚡', '2026-04-21 18:26:50', '2026-04-21 19:56:56');
INSERT INTO `pet_trail`.`ai_model` (`id`, `model_name`, `display_name`, `provider`, `base_url`, `api_key`, `model_version`, `parameters`, `status`, `is_default`, `sort_order`, `description`, `icon`, `created_at`, `updated_at`) VALUES (3, 'openrouter/free', '通用免费模型', 'openrouter', 'https://openrouter.ai/api/v1', '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', 'free', '{\"max_tokens\": 200, \"temperature\": 0.7}', 1, 0, 3, '免费通用模型，适合基础健康分析需求', '🎁', '2026-04-21 18:26:50', '2026-04-21 19:57:11');

INSERT INTO `pet_trail`.`tags` (`id`, `name`, `usage_count`, `is_hot`, `is_official`, `status`, `created_at`, `updated_at`) VALUES (1, '萌宠日常', 0, 1, 1, 1, '2026-04-21 01:01:00', '2026-04-21 01:01:00');
INSERT INTO `pet_trail`.`tags` (`id`, `name`, `usage_count`, `is_hot`, `is_official`, `status`, `created_at`, `updated_at`) VALUES (2, '宠物才艺', 0, 1, 1, 1, '2026-04-21 01:01:00', '2026-04-21 01:01:00');
INSERT INTO `pet_trail`.`tags` (`id`, `name`, `usage_count`, `is_hot`, `is_official`, `status`, `created_at`, `updated_at`) VALUES (3, '宠物穿搭', 0, 1, 1, 1, '2026-04-21 01:01:00', '2026-04-21 01:01:00');
INSERT INTO `pet_trail`.`tags` (`id`, `name`, `usage_count`, `is_hot`, `is_official`, `status`, `created_at`, `updated_at`) VALUES (4, '宠物旅行', 0, 1, 1, 1, '2026-04-21 01:01:00', '2026-04-21 01:01:00');
INSERT INTO `pet_trail`.`tags` (`id`, `name`, `usage_count`, `is_hot`, `is_official`, `status`, `created_at`, `updated_at`) VALUES (5, '养宠心得', 0, 1, 1, 1, '2026-04-21 01:01:00', '2026-04-21 01:01:00');
INSERT INTO `pet_trail`.`tags` (`id`, `name`, `usage_count`, `is_hot`, `is_official`, `status`, `created_at`, `updated_at`) VALUES (6, '新手养宠', 0, 1, 1, 1, '2026-04-21 01:01:00', '2026-04-21 01:01:00');
INSERT INTO `pet_trail`.`tags` (`id`, `name`, `usage_count`, `is_hot`, `is_official`, `status`, `created_at`, `updated_at`) VALUES (7, '猫咪控', 0, 1, 1, 1, '2026-04-21 01:01:00', '2026-04-21 01:01:00');
INSERT INTO `pet_trail`.`tags` (`id`, `name`, `usage_count`, `is_hot`, `is_official`, `status`, `created_at`, `updated_at`) VALUES (8, '狗狗控', 1, 1, 1, 1, '2026-04-21 01:01:00', '2026-04-21 23:13:19');


SET FOREIGN_KEY_CHECKS = 1;

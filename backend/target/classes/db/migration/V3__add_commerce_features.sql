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

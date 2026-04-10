-- 为体重记录表添加 note 字段
-- 执行日期: 2026-04-10

ALTER TABLE `weight_records` 
ADD COLUMN `note` varchar(500) DEFAULT NULL COMMENT '备注' AFTER `record_date`;

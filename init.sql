-- Database initialization for Jiangmai platform
-- This script will run automatically when the MySQL container is first created

CREATE DATABASE IF NOT EXISTS `jiangmai_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `jiangmai_db`;
SET NAMES utf8mb4;

-- Basic user table
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` longtext COMMENT '头像(base64/url)',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `role` varchar(20) DEFAULT 'STUDENT' COMMENT '角色: ADMIN, STUDENT, UPLOADER',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- Insert default admin user (password: admin123 generated via BCrypt ideally, 
-- but here for demo we just put plain text or simple hash depending on your actual auth implementation later)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) 
VALUES ('admin', 'admin123', '超级管理员', 'ADMIN');

-- Course table
CREATE TABLE IF NOT EXISTS `course` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '课程标题',
  `description` text COMMENT '课程简介',
  `cover_url` varchar(255) DEFAULT NULL COMMENT '封面图URL',
  `video_url` varchar(500) DEFAULT NULL COMMENT '课程视频URL',
  `teacher_id` bigint(20) NOT NULL COMMENT '上传者ID(UPLOADER)',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '价格,0为免费',
  `status` varchar(20) DEFAULT 'PUBLISHED' COMMENT '状态: DRAFT, PUBLISHED, OFFLINE',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- Course enrollment table (purchase + learning progress)
CREATE TABLE IF NOT EXISTS `course_enrollment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '学员ID',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `status` varchar(20) DEFAULT 'PAID' COMMENT '状态: PAID, REFUNDED',
  `pay_amount` decimal(10,2) DEFAULT '0.00' COMMENT '支付金额',
  `progress` int(11) DEFAULT 0 COMMENT '学习进度(0-100)',
  `study_minutes` int(11) DEFAULT 0 COMMENT '累计学习分钟',
  `last_study_time` datetime DEFAULT NULL COMMENT '最近学习时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '购买时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course` (`user_id`,`course_id`),
  KEY `idx_course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程购买与学习记录表';

-- Work (Uploads) table
CREATE TABLE IF NOT EXISTS `work` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '作品标题',
  `description` text COMMENT '作品描述',
  `image_url` varchar(255) NOT NULL COMMENT '作品图片URL',
  `author_id` bigint(20) NOT NULL COMMENT '作者ID(STUDENT/UPLOADER)',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态: PENDING(待审核), APPROVED(已通过), REJECTED(已拒绝)',
  `is_for_sale` tinyint(1) DEFAULT 0 COMMENT '是否申请售卖 0-否 1-是',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '售卖标价',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户作品表';

-- Profile change request table (username/avatar changes require admin approval)
CREATE TABLE IF NOT EXISTS `profile_change_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '申请用户ID',
  `current_username` varchar(50) NOT NULL COMMENT '申请时用户名',
  `requested_username` varchar(50) DEFAULT NULL COMMENT '申请修改后的用户名',
  `requested_avatar` longtext COMMENT '申请修改后的头像(base64/url)',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED',
  `review_comment` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `reviewed_by` varchar(50) DEFAULT NULL COMMENT '审核人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资料修改申请表';

-- Learning homework submission and review
CREATE TABLE IF NOT EXISTS `learning_homework` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '提交用户ID',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `content` text COMMENT '作业说明',
  `work_url` varchar(500) DEFAULT NULL COMMENT '作品图片/视频地址',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT 'PENDING, REVIEWED',
  `ai_comment` text COMMENT 'AI点评',
  `teacher_comment` text COMMENT '导师点评',
  `score` int(11) DEFAULT NULL COMMENT '评分(0-100)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_course` (`user_id`, `course_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习作业与点评表';

-- Offline activity and booking
CREATE TABLE IF NOT EXISTS `offline_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(120) NOT NULL COMMENT '活动标题',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `location` varchar(255) DEFAULT NULL COMMENT '活动地点',
  `activity_time` datetime NOT NULL COMMENT '活动时间',
  `quota` int(11) DEFAULT 30 COMMENT '名额',
  `booked_count` int(11) DEFAULT 0 COMMENT '已报名人数',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '活动价格',
  `status` varchar(20) DEFAULT 'OPEN' COMMENT 'OPEN, CLOSED, FINISHED',
  `description` text COMMENT '活动介绍',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线下活动表';

CREATE TABLE IF NOT EXISTS `offline_booking` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `status` varchar(20) DEFAULT 'BOOKED' COMMENT 'BOOKED, CHECKED_IN, CANCELED, REFUNDED',
  `pay_amount` decimal(10,2) DEFAULT '0.00' COMMENT '支付金额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线下活动报名表';

-- Work order and after-sale
CREATE TABLE IF NOT EXISTS `order_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(40) NOT NULL COMMENT '订单号',
  `buyer_id` bigint(20) NOT NULL COMMENT '买家ID',
  `work_id` bigint(20) NOT NULL COMMENT '作品ID',
  `amount` decimal(10,2) DEFAULT '0.00' COMMENT '订单金额',
  `status` varchar(20) DEFAULT 'PAID' COMMENT 'PAID, SHIPPED, RECEIVED, REFUNDING, REFUNDED',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_buyer` (`buyer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `after_sale_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `user_id` bigint(20) NOT NULL COMMENT '申请用户ID',
  `type` varchar(20) DEFAULT 'REFUND' COMMENT '售后类型',
  `reason` varchar(255) DEFAULT NULL COMMENT '售后原因',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED',
  `review_comment` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_status` (`order_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后申请表';

CREATE TABLE IF NOT EXISTS `work_favorite` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '收藏用户ID',
  `work_id` bigint(20) NOT NULL COMMENT '作品ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_work_favorite` (`user_id`, `work_id`),
  KEY `idx_work_favorite_work` (`work_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品收藏表';

CREATE TABLE IF NOT EXISTS `work_view_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '浏览用户ID',
  `work_id` bigint(20) NOT NULL COMMENT '作品ID',
  `view_count` int(11) DEFAULT 1 COMMENT '浏览次数',
  `last_view_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最近浏览时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次浏览时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_work_view` (`user_id`, `work_id`),
  KEY `idx_work_view_time` (`last_view_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品浏览记录表';

CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `actor_id` bigint(20) DEFAULT NULL COMMENT '操作人ID',
  `actor_name` varchar(60) DEFAULT NULL COMMENT '操作人用户名',
  `actor_role` varchar(20) DEFAULT NULL COMMENT '操作人角色',
  `module` varchar(40) NOT NULL COMMENT '业务模块',
  `action` varchar(80) NOT NULL COMMENT '操作动作',
  `target_id` bigint(20) DEFAULT NULL COMMENT '目标记录ID',
  `detail` varchar(500) DEFAULT NULL COMMENT '操作详情',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_operation_module` (`module`),
  KEY `idx_operation_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- More realistic demo users (idempotent by username)
INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `role`, `create_time`)
VALUES
  ('uploader', 'uploader123', '锡雕讲师张老师', '13900000001', 'UPLOADER', '2026-01-10 10:20:00'),
  ('artisan_chen', '123456', '陈师傅（省级传承人）', '13900000002', 'UPLOADER', '2026-01-15 09:15:00'),
  ('artisan_lin', '123456', '林老师（工艺设计）', '13900000003', 'UPLOADER', '2026-01-20 14:40:00'),
  ('student_li', '123456', '李同学', '13800000011', 'STUDENT', '2026-02-02 11:00:00'),
  ('student', 'student123', '学员小李', '13700009999', 'STUDENT', '2026-02-03 09:00:00'),
  ('student_wang', '123456', '王同学', '13800000012', 'STUDENT', '2026-02-05 16:30:00'),
  ('student_zhao', '123456', '赵同学', '13800000013', 'STUDENT', '2026-02-11 19:20:00'),
  ('student_xu', '123456', '徐同学', '13800000014', 'STUDENT', '2026-02-16 13:10:00'),
  ('student_sun', '123456', '孙同学', '13800000015', 'STUDENT', '2026-02-21 08:50:00'),
  ('collector_ma', '123456', '马老师（收藏爱好者）', '13700000021', 'STUDENT', '2026-02-25 12:35:00'),
  ('designer_he', '123456', '何同学（文创方向）', '13700000022', 'STUDENT', '2026-03-01 17:45:00')
ON DUPLICATE KEY UPDATE
  `nickname` = VALUES(`nickname`),
  `phone` = VALUES(`phone`),
  `role` = VALUES(`role`);

-- More realistic demo courses (idempotent by title)
INSERT INTO `course` (`title`, `description`, `cover_url`, `teacher_id`, `price`, `status`, `create_time`)
SELECT
  '锡雕入门：錾刻与退火基础',
  '面向零基础学员，系统讲解锡料特性、退火处理、基础錾刻手法与安全规范，配套作业可完成第一件入门作品。',
  'https://images.unsplash.com/photo-1517048676732-d65bc937f952?w=1200',
  u.id, 99.00, 'PUBLISHED', '2026-02-01 09:30:00'
FROM `user` u
WHERE u.username = 'uploader'
  AND NOT EXISTS (SELECT 1 FROM `course` c WHERE c.title = '锡雕入门：錾刻与退火基础');

INSERT INTO `course` (`title`, `description`, `cover_url`, `teacher_id`, `price`, `status`, `create_time`)
SELECT
  '传统纹样训练：回纹、云纹、缠枝纹',
  '从传统纹样结构拆解到上板实操，提升线条稳定性与构图能力，适合希望快速提高作品质感的学习者。',
  'https://images.unsplash.com/photo-1455390582262-044cdead277a?w=1200',
  u.id, 129.00, 'PUBLISHED', '2026-02-04 15:10:00'
FROM `user` u
WHERE u.username = 'artisan_chen'
  AND NOT EXISTS (SELECT 1 FROM `course` c WHERE c.title = '传统纹样训练：回纹、云纹、缠枝纹');

INSERT INTO `course` (`title`, `description`, `cover_url`, `teacher_id`, `price`, `status`, `create_time`)
SELECT
  '茶器专项：锡壶与锡茶仓制作',
  '围绕实用器展开，从结构拆分、拼接焊点到抛光收尾，帮助学员完成可展示、可售卖的茶器作品。',
  'https://images.unsplash.com/photo-1489515217757-5fd1be406fef?w=1200',
  u.id, 268.00, 'PUBLISHED', '2026-02-08 20:05:00'
FROM `user` u
WHERE u.username = 'uploader'
  AND NOT EXISTS (SELECT 1 FROM `course` c WHERE c.title = '茶器专项：锡壶与锡茶仓制作');

INSERT INTO `course` (`title`, `description`, `cover_url`, `teacher_id`, `price`, `status`, `create_time`)
SELECT
  '浮雕进阶：层次塑形与光影表达',
  '重点训练浮雕深浅、转折和层次衔接，附带案例拆解，适合已完成入门课程并希望冲刺比赛的学员。',
  'https://images.unsplash.com/photo-1452802447250-470a88ac82bc?w=1200',
  u.id, 199.00, 'PUBLISHED', '2026-02-12 10:25:00'
FROM `user` u
WHERE u.username = 'artisan_lin'
  AND NOT EXISTS (SELECT 1 FROM `course` c WHERE c.title = '浮雕进阶：层次塑形与光影表达');

INSERT INTO `course` (`title`, `description`, `cover_url`, `teacher_id`, `price`, `status`, `create_time`)
SELECT
  '非遗史论：永康锡雕发展脉络',
  '梳理地方工艺源流、代表器型与文化语境，为比赛路演和文案包装提供扎实的理论支持。',
  'https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=1200',
  u.id, 0.00, 'PUBLISHED', '2026-02-15 18:00:00'
FROM `user` u
WHERE u.username = 'artisan_chen'
  AND NOT EXISTS (SELECT 1 FROM `course` c WHERE c.title = '非遗史论：永康锡雕发展脉络');

INSERT INTO `course` (`title`, `description`, `cover_url`, `teacher_id`, `price`, `status`, `create_time`)
SELECT
  '文创开发：锡雕饰品商业化设计',
  '从用户画像、定价策略到小批量打样，完整走一遍锡雕文创产品开发流程，强调市场可落地性。',
  'https://images.unsplash.com/photo-1519741497674-611481863552?w=1200',
  u.id, 159.00, 'PUBLISHED', '2026-02-19 13:45:00'
FROM `user` u
WHERE u.username = 'artisan_lin'
  AND NOT EXISTS (SELECT 1 FROM `course` c WHERE c.title = '文创开发：锡雕饰品商业化设计');

INSERT INTO `course` (`title`, `description`, `cover_url`, `teacher_id`, `price`, `status`, `create_time`)
SELECT
  '比赛冲刺营：三创赛作品打磨',
  '聚焦选题、作品叙事、答辩展示与评审关注点，帮助团队在有限时间内提升作品完成度与路演表现。',
  'https://images.unsplash.com/photo-1524178232363-1fb2b075b655?w=1200',
  u.id, 299.00, 'PENDING', '2026-03-02 21:00:00'
FROM `user` u
WHERE u.username = 'uploader'
  AND NOT EXISTS (SELECT 1 FROM `course` c WHERE c.title = '比赛冲刺营：三创赛作品打磨');

INSERT INTO `course` (`title`, `description`, `cover_url`, `teacher_id`, `price`, `status`, `create_time`)
SELECT
  '锡雕与AI：设计灵感协同工作流',
  '演示如何用AI生成纹样草图、拆分工序和教学文案，再结合手工工艺完成作品，实现数字与非遗融合。',
  'https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=1200',
  u.id, 89.00, 'PUBLISHED', '2026-03-05 09:05:00'
FROM `user` u
WHERE u.username = 'artisan_lin'
  AND NOT EXISTS (SELECT 1 FROM `course` c WHERE c.title = '锡雕与AI：设计灵感协同工作流');

USE `jiangmai_db`;
SET NAMES utf8mb4;

ALTER DATABASE `jiangmai_db` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

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

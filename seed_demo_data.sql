USE `jiangmai_db`;
SET NAMES utf8mb4;

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

-- Demo enrollments for business loop
INSERT INTO `course_enrollment` (`user_id`, `course_id`, `status`, `pay_amount`, `progress`, `study_minutes`, `last_study_time`, `create_time`)
SELECT
  u.id, c.id, 'PAID', c.price, 85, 460, '2026-03-09 20:10:00', '2026-03-02 09:20:00'
FROM `user` u
JOIN `course` c ON c.title = '锡雕入门：錾刻与退火基础'
WHERE u.username = 'student_li'
  AND NOT EXISTS (
    SELECT 1 FROM `course_enrollment` e
    WHERE e.user_id = u.id AND e.course_id = c.id
  );

INSERT INTO `course_enrollment` (`user_id`, `course_id`, `status`, `pay_amount`, `progress`, `study_minutes`, `last_study_time`, `create_time`)
SELECT
  u.id, c.id, 'PAID', c.price, 38, 220, '2026-03-08 18:30:00', '2026-03-03 14:35:00'
FROM `user` u
JOIN `course` c ON c.title = '传统纹样训练：回纹、云纹、缠枝纹'
WHERE u.username = 'student_wang'
  AND NOT EXISTS (
    SELECT 1 FROM `course_enrollment` e
    WHERE e.user_id = u.id AND e.course_id = c.id
  );

INSERT INTO `work` (`title`, `description`, `image_url`, `author_id`, `status`, `is_for_sale`, `price`, `create_time`)
SELECT
  '梅兰竹菊四君子锡雕挂盘',
  '采用浅浮雕与细錾结合，突出枝叶层次与器表光泽，适合展览陈列。',
  'https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=1200',
  u.id, 'APPROVED', 1, 398.00, '2026-02-18 14:30:00'
FROM `user` u
WHERE u.username = 'student_li'
  AND NOT EXISTS (SELECT 1 FROM `work` w WHERE w.title = '梅兰竹菊四君子锡雕挂盘');

INSERT INTO `work` (`title`, `description`, `image_url`, `author_id`, `status`, `is_for_sale`, `price`, `create_time`)
SELECT
  '龙纹锡壶·竞赛打样版',
  '壶身龙纹采用多层次敲錾工艺，壶钮与壶流比例经过三轮打样优化。',
  'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=1200',
  u.id, 'APPROVED', 1, 688.00, '2026-02-20 19:10:00'
FROM `user` u
WHERE u.username = 'student_wang'
  AND NOT EXISTS (SELECT 1 FROM `work` w WHERE w.title = '龙纹锡壶·竞赛打样版');

INSERT INTO `work` (`title`, `description`, `image_url`, `author_id`, `status`, `is_for_sale`, `price`, `create_time`)
SELECT
  '青花意向锡雕茶仓',
  '将青花纹样转译为锡雕线刻语言，整体风格雅致，适合文创礼赠。',
  'https://images.unsplash.com/photo-1473448912268-2022ce9509d8?w=1200',
  u.id, 'APPROVED', 1, 268.00, '2026-02-23 10:05:00'
FROM `user` u
WHERE u.username = 'designer_he'
  AND NOT EXISTS (SELECT 1 FROM `work` w WHERE w.title = '青花意向锡雕茶仓');

INSERT INTO `work` (`title`, `description`, `image_url`, `author_id`, `status`, `is_for_sale`, `price`, `create_time`)
SELECT
  '喜鹊登梅纹香插',
  '小尺寸器型训练作品，重点表现枝干起伏和羽翼纹理的刀痕节奏。',
  'https://images.unsplash.com/photo-1517705008128-361805f42e86?w=1200',
  u.id, 'APPROVED', 0, 0.00, '2026-02-26 16:40:00'
FROM `user` u
WHERE u.username = 'student_zhao'
  AND NOT EXISTS (SELECT 1 FROM `work` w WHERE w.title = '喜鹊登梅纹香插');

INSERT INTO `work` (`title`, `description`, `image_url`, `author_id`, `status`, `is_for_sale`, `price`, `create_time`)
SELECT
  '山水意境锡雕摆件',
  '尝试将远山、流水和亭台做层次分区，训练画面叙事与主体聚焦能力。',
  'https://images.unsplash.com/photo-1523419409543-0c1a0fa6d1c8?w=1200',
  u.id, 'APPROVED', 0, 0.00, '2026-02-28 11:20:00'
FROM `user` u
WHERE u.username = 'student_xu'
  AND NOT EXISTS (SELECT 1 FROM `work` w WHERE w.title = '山水意境锡雕摆件');

INSERT INTO `work` (`title`, `description`, `image_url`, `author_id`, `status`, `is_for_sale`, `price`, `create_time`)
SELECT
  '回纹礼盒扣件套装',
  '结合现代包装需求的金属小件开发，强调批量加工一致性与可装配性。',
  'https://images.unsplash.com/photo-1463320726281-696a485928c7?w=1200',
  u.id, 'APPROVED', 1, 128.00, '2026-03-01 09:55:00'
FROM `user` u
WHERE u.username = 'student_sun'
  AND NOT EXISTS (SELECT 1 FROM `work` w WHERE w.title = '回纹礼盒扣件套装');

INSERT INTO `work` (`title`, `description`, `image_url`, `author_id`, `status`, `is_for_sale`, `price`, `create_time`)
SELECT
  '福鹿同春锡雕壁饰',
  '采用点线结合技法表现鹿角与松针细节，面向节庆场景的家居陈设方向。',
  'https://images.unsplash.com/photo-1519710164239-da123dc03ef4?w=1200',
  u.id, 'PENDING', 1, 520.00, '2026-03-03 13:25:00'
FROM `user` u
WHERE u.username = 'student_li'
  AND NOT EXISTS (SELECT 1 FROM `work` w WHERE w.title = '福鹿同春锡雕壁饰');

INSERT INTO `work` (`title`, `description`, `image_url`, `author_id`, `status`, `is_for_sale`, `price`, `create_time`)
SELECT
  '十二花神纹茶托',
  '系列化打样中的第3版，边缘花瓣过渡还在优化，已提交审核等待意见。',
  'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=1200',
  u.id, 'PENDING', 1, 188.00, '2026-03-05 18:50:00'
FROM `user` u
WHERE u.username = 'designer_he'
  AND NOT EXISTS (SELECT 1 FROM `work` w WHERE w.title = '十二花神纹茶托');

INSERT INTO `work` (`title`, `description`, `image_url`, `author_id`, `status`, `is_for_sale`, `price`, `create_time`)
SELECT
  '竹影窗棂纹文镇',
  '构图完整但收边粗糙，因抛光不均被退回，后续将重新处理边角。',
  'https://images.unsplash.com/photo-1493666438817-866a91353ca9?w=1200',
  u.id, 'REJECTED', 0, 0.00, '2026-03-06 15:35:00'
FROM `user` u
WHERE u.username = 'student_wang'
  AND NOT EXISTS (SELECT 1 FROM `work` w WHERE w.title = '竹影窗棂纹文镇');

-- Demo homework submissions
INSERT INTO `learning_homework` (`user_id`, `course_id`, `content`, `work_url`, `status`, `ai_comment`, `teacher_comment`, `score`, `create_time`)
SELECT
  u.id, c.id,
  '已完成基础錾刻练习，请老师点评线条稳定性与收尾处理。',
  'https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=1200',
  'REVIEWED',
  '线条整体流畅，边缘收口略有毛刺，建议增加800目砂纸抛光步骤。',
  '整体完成度不错，建议下次加强转角过渡的连贯性。',
  86,
  '2026-03-06 20:10:00'
FROM `user` u
JOIN `course` c ON c.title = '锡雕入门：錾刻与退火基础'
WHERE u.username = 'student_li'
  AND NOT EXISTS (
    SELECT 1 FROM `learning_homework` h
    WHERE h.user_id = u.id AND h.course_id = c.id
  );

INSERT INTO `learning_homework` (`user_id`, `course_id`, `content`, `work_url`, `status`, `create_time`)
SELECT
  u.id, c.id,
  '提交第二次纹样作业，重点训练缠枝纹节奏。',
  'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=1200',
  'PENDING',
  '2026-03-09 19:30:00'
FROM `user` u
JOIN `course` c ON c.title = '传统纹样训练：回纹、云纹、缠枝纹'
WHERE u.username = 'student_wang'
  AND NOT EXISTS (
    SELECT 1 FROM `learning_homework` h
    WHERE h.user_id = u.id AND h.course_id = c.id AND h.status = 'PENDING'
  );

-- Demo offline activities
INSERT INTO `offline_activity` (`title`, `city`, `location`, `activity_time`, `quota`, `booked_count`, `price`, `status`, `description`, `create_time`)
SELECT
  '永康锡雕周末工坊体验',
  '金华·永康',
  '永康锡雕文化馆二层工坊',
  '2026-03-20 14:00:00',
  30,
  2,
  39.00,
  'OPEN',
  '面向初学者的2小时线下体验，含材料包与导师指导。',
  '2026-03-05 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `offline_activity` a WHERE a.title = '永康锡雕周末工坊体验');

INSERT INTO `offline_activity` (`title`, `city`, `location`, `activity_time`, `quota`, `booked_count`, `price`, `status`, `description`, `create_time`)
SELECT
  '非遗大师公开课：浮雕层次训练',
  '杭州',
  '浙江非遗研学中心',
  '2026-03-27 19:00:00',
  50,
  1,
  99.00,
  'OPEN',
  '大师现场示范与点评，适合进阶学习者。',
  '2026-03-06 15:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `offline_activity` a WHERE a.title = '非遗大师公开课：浮雕层次训练');

INSERT INTO `offline_booking` (`activity_id`, `user_id`, `status`, `pay_amount`, `create_time`)
SELECT
  a.id, u.id, 'BOOKED', a.price, '2026-03-08 11:00:00'
FROM `offline_activity` a
JOIN `user` u ON u.username = 'student_li'
WHERE a.title = '永康锡雕周末工坊体验'
  AND NOT EXISTS (SELECT 1 FROM `offline_booking` b WHERE b.activity_id = a.id AND b.user_id = u.id);

INSERT INTO `offline_booking` (`activity_id`, `user_id`, `status`, `pay_amount`, `create_time`)
SELECT
  a.id, u.id, 'BOOKED', a.price, '2026-03-09 13:20:00'
FROM `offline_activity` a
JOIN `user` u ON u.username = 'designer_he'
WHERE a.title = '永康锡雕周末工坊体验'
  AND NOT EXISTS (SELECT 1 FROM `offline_booking` b WHERE b.activity_id = a.id AND b.user_id = u.id);

-- Demo orders and after-sale
INSERT INTO `order_record` (`order_no`, `buyer_id`, `work_id`, `amount`, `status`, `remark`, `create_time`)
SELECT
  'OD202603080001',
  buyer.id,
  w.id,
  w.price,
  'PAID',
  '演示订单',
  '2026-03-08 09:18:00'
FROM `user` buyer
JOIN `work` w ON w.title = '梅兰竹菊四君子锡雕挂盘'
WHERE buyer.username = 'student'
  AND NOT EXISTS (SELECT 1 FROM `order_record` o WHERE o.order_no = 'OD202603080001');

INSERT INTO `after_sale_request` (`order_id`, `user_id`, `type`, `reason`, `status`, `review_comment`, `create_time`)
SELECT
  o.id, o.buyer_id, 'REFUND', '收货地址临时变更，申请退款后重新下单。', 'APPROVED', '已同意退款。', '2026-03-09 09:30:00'
FROM `order_record` o
WHERE o.order_no = 'OD202603080001'
  AND NOT EXISTS (SELECT 1 FROM `after_sale_request` r WHERE r.order_id = o.id);

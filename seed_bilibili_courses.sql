USE `jiangmai_db`;
SET NAMES utf8mb4;

-- Bilibili real-course demo data (idempotent)
INSERT INTO `course` (`title`, `description`, `cover_url`, `video_url`, `teacher_id`, `price`, `status`, `create_time`)
SELECT
  '非遗永康锡雕（纪录片）',
  'B站来源：手艺第六季（永康锡雕），用于平台演示真实视频课程链接。',
  'https://images.unsplash.com/photo-1517048676732-d65bc937f952?w=1200',
  'https://www.bilibili.com/video/BV1K334e6Epz/',
  u.id,
  0.00,
  'PUBLISHED',
  NOW()
FROM `user` u
WHERE u.username = 'uploader'
  AND NOT EXISTS (SELECT 1 FROM `course` c WHERE c.title = '非遗永康锡雕（纪录片）');

INSERT INTO `course` (`title`, `description`, `cover_url`, `video_url`, `teacher_id`, `price`, `status`, `create_time`)
SELECT
  '永康锡雕工艺介绍（基础认知）',
  'B站来源：永康锡雕相关介绍视频，适合新用户快速了解工艺背景。',
  'https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=1200',
  'https://www.bilibili.com/video/BV1a24y1B7Xm/',
  u.id,
  19.90,
  'PUBLISHED',
  NOW()
FROM `user` u
WHERE u.username = 'artisan_chen'
  AND NOT EXISTS (SELECT 1 FROM `course` c WHERE c.title = '永康锡雕工艺介绍（基础认知）');

INSERT INTO `course` (`title`, `description`, `cover_url`, `video_url`, `teacher_id`, `price`, `status`, `create_time`)
SELECT
  '金属錾刻入门（可迁移到锡雕）',
  'B站来源：金属錾刻教学，适合补齐线刻与敲錾基础动作。',
  'https://images.unsplash.com/photo-1455390582262-044cdead277a?w=1200',
  'https://www.bilibili.com/video/BV1va41147nU/',
  u.id,
  29.90,
  'PUBLISHED',
  NOW()
FROM `user` u
WHERE u.username = 'artisan_lin'
  AND NOT EXISTS (SELECT 1 FROM `course` c WHERE c.title = '金属錾刻入门（可迁移到锡雕）');

INSERT INTO `course` (`title`, `description`, `cover_url`, `video_url`, `teacher_id`, `price`, `status`, `create_time`)
SELECT
  '金工工艺进阶（浮雕与纹样）',
  'B站来源：金工类进阶内容，可用于锡雕中高阶纹样练习参考。',
  'https://images.unsplash.com/photo-1452802447250-470a88ac82bc?w=1200',
  'https://www.bilibili.com/video/BV1Qj41177xp/',
  u.id,
  39.90,
  'PUBLISHED',
  NOW()
FROM `user` u
WHERE u.username = 'uploader'
  AND NOT EXISTS (SELECT 1 FROM `course` c WHERE c.title = '金工工艺进阶（浮雕与纹样）');

INSERT INTO `course` (`title`, `description`, `cover_url`, `video_url`, `teacher_id`, `price`, `status`, `create_time`)
SELECT
  '金属器表面处理（抛光与修整）',
  'B站来源：金属器表面处理视频，可用于锡雕收尾环节教学。',
  'https://images.unsplash.com/photo-1519741497674-611481863552?w=1200',
  'https://www.bilibili.com/video/BV1W4411p7Lw/',
  u.id,
  25.00,
  'PUBLISHED',
  NOW()
FROM `user` u
WHERE u.username = 'artisan_chen'
  AND NOT EXISTS (SELECT 1 FROM `course` c WHERE c.title = '金属器表面处理（抛光与修整）');

INSERT INTO `course` (`title`, `description`, `cover_url`, `video_url`, `teacher_id`, `price`, `status`, `create_time`)
SELECT
  '传统金属手工案例（课程补充）',
  'B站来源：传统金属工艺案例，补充锡雕课堂案例素材。',
  'https://images.unsplash.com/photo-1524178232363-1fb2b075b655?w=1200',
  'https://www.bilibili.com/video/BV1Cr4y1B741/',
  u.id,
  0.00,
  'PUBLISHED',
  NOW()
FROM `user` u
WHERE u.username = 'artisan_lin'
  AND NOT EXISTS (SELECT 1 FROM `course` c WHERE c.title = '传统金属手工案例（课程补充）');

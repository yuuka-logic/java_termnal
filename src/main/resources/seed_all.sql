-- ============================================================
-- personal_blog 全量测试数据
-- 密码统一为 123456（密文由 PasswordUtil.encrypt 生成，可被 PasswordUtil.matches 校验通过）
-- ============================================================
USE `personal_blog`;

SET @PWD = '10000$atMCvJP7HqQ4ZDKx7nPUlw==$1dU45yFl+DraXxnu0DaDpANlNdaIYLLDH0K2DtTZZ8o=';

-- ---------- 清理（注意外键顺序）----------
DELETE FROM `article_tag`;
DELETE FROM `article`;
DELETE FROM `announcement`;
DELETE FROM `operation_log`;
DELETE FROM `tag`;
DELETE FROM `category`;
DELETE FROM `user`;
ALTER TABLE `user` AUTO_INCREMENT = 1;
ALTER TABLE `category` AUTO_INCREMENT = 1;
ALTER TABLE `tag` AUTO_INCREMENT = 1;
ALTER TABLE `article` AUTO_INCREMENT = 1;
ALTER TABLE `announcement` AUTO_INCREMENT = 1;

-- ---------- 用户 ----------
INSERT INTO `user` (username, password, nickname, email, phone, gender, bio, role, status, is_deleted) VALUES
('admin',   @PWD, '超级管理员', 'admin@blog.com',   '13800000001', 1, '系统最高权限账号',   0, 1, 0),
('manager', @PWD, '内容管理员', 'manager@blog.com', '13800000002', 2, '负责文章审核',       1, 1, 0),
('zhangsan',@PWD, '张三',       'zhang@blog.com',   '13800000003', 1, '爱写 Java 的学生',   2, 1, 0),
('lisi',    @PWD, '李四',       'li@blog.com',      '13800000004', 1, '前端爱好者',         2, 1, 0),
('wangwu',  @PWD, '王五',       'wang@blog.com',    '13800000005', 2, '数据库方向',         2, 1, 0),
('zhaoliu', @PWD, '赵六',       'zhao@blog.com',    '13800000006', 0, NULL,                 2, 0, 0),
('sunqi',   @PWD, '孙七',       'sun@blog.com',     NULL,          2, '已被逻辑删除的账号', 2, 0, 1);

-- ---------- 分类（含二级分类）----------
INSERT INTO `category` (name, slug, description, parent_id, sort_order, status, is_deleted) VALUES
('后端开发', 'backend',    'Java 后端相关技术',       0, 1, 1, 0),
('前端开发', 'frontend',   'HTML/CSS/JavaScript',     0, 2, 1, 0),
('数据库',   'database',   'MySQL 与数据库设计',      0, 3, 1, 0),
('Spring',   'spring',     'Spring 全家桶',            1, 1, 1, 0),
('MyBatis',  'mybatis',    'MyBatis 持久层框架',       1, 2, 1, 0),
('Vue',      'vue',        'Vue 前端框架',             2, 1, 1, 0),
('已废弃分类','deprecated','用于测试删除校验',         0, 9, 0, 1);

-- ---------- 标签 ----------
INSERT INTO `tag` (name, slug, description, article_count, is_deleted) VALUES
('Java',        'java',        'Java 语言',        0, 0),
('Spring Boot', 'spring-boot', 'Spring Boot 框架', 0, 0),
('MyBatis',     'mybatis',     'MyBatis 框架',     0, 0),
('MySQL',       'mysql',       'MySQL 数据库',     0, 0),
('Vue',         'vue',         'Vue 前端框架',     0, 0),
('踩坑记录',    'pitfall',     '踩过的坑',         0, 0);

-- ---------- 文章 ----------
INSERT INTO `article`
  (title, summary, content, cover_image, category_id, author_id, status, is_top, is_original,
   source_url, view_count, like_count, comment_count, publish_time) VALUES
('Spring Boot 快速入门', '从零搭建第一个 Spring Boot 项目',
 '# Spring Boot 快速入门\n\n本文介绍如何用 Spring Initializr 生成项目骨架……',
 NULL, 4, 1, 1, 1, 1, NULL, 128, 12, 3, '2026-10-08 10:00:00'),
('MyBatis 动态 SQL 详解', 'if / choose / foreach / set 全解析',
 '# MyBatis 动态 SQL\n\n动态 SQL 是 MyBatis 最强大的特性之一……',
 NULL, 5, 3, 1, 0, 1, NULL, 96, 8, 1, '2026-10-09 14:30:00'),
('MySQL 表设计规范', '主键、索引、范式与反范式的取舍',
 '# MySQL 表设计规范\n\n设计表时优先考虑字段类型的选择……',
 NULL, 3, 5, 1, 0, 1, NULL, 210, 25, 6, '2026-10-12 09:00:00'),
('Vue3 组合式 API 实践', 'setup 与 ref/reactive 的用法',
 '# Vue3 组合式 API\n\n组合式 API 让逻辑复用更自然……',
 NULL, 6, 4, 1, 0, 1, NULL, 75, 5, 2, '2026-10-11 16:20:00'),
('一篇还没写完的草稿', '草稿状态的文章，前台不应展示',
 '# 草稿\n\n内容还没写完……',
 NULL, 1, 3, 0, 0, 1, NULL, 0, 0, 0, NULL),
('已下架的文章', '用于测试状态过滤',
 '# 下架\n\n这篇文章已被下架……',
 NULL, 1, 1, 2, 0, 1, NULL, 33, 1, 0, '2026-10-05 08:00:00'),
('转载：Spring 事务传播机制', '转载自网络，非原创',
 '# Spring 事务传播\n\n本文转载自网络……',
 NULL, 4, 2, 1, 0, 0, 'https://example.com/spring-tx', 60, 4, 0, '2026-10-07 11:00:00');

-- ---------- 文章标签关联 ----------
INSERT INTO `article_tag` (article_id, tag_id) VALUES
(1, 1), (1, 2),          -- Spring Boot 快速入门 -> Java, Spring Boot
(2, 1), (2, 3),          -- MyBatis 动态 SQL -> Java, MyBatis
(3, 4),                  -- MySQL 表设计规范 -> MySQL
(4, 5),                  -- Vue3 组合式 API -> Vue
(5, 1),                  -- 草稿 -> Java
(6, 2),                  -- 已下架 -> Spring Boot
(7, 1), (7, 6);          -- 转载 -> Java, 踩坑记录

-- ---------- 同步标签文章数（只统计已发布文章，与业务口径一致）----------
UPDATE `tag` t SET t.article_count = (
    SELECT COUNT(*) FROM `article_tag` at
    INNER JOIN `article` a ON a.id = at.article_id
    WHERE at.tag_id = t.id AND a.status = 1
);

-- ---------- 通知公告 ----------
INSERT INTO `announcement`
  (title, content, type, priority, status, publisher_id, view_count, is_top, publish_time, expire_time) VALUES
('系统维护通知', '本系统将于本周日凌晨 2:00-4:00 进行维护，期间暂停服务。', 2, 2, 1, 1, 45, 1, '2026-10-10 09:00:00', NULL),
('v1.0.0 版本发布', '个人博客系统首个正式版本发布，包含用户、文章、分类、标签、公告模块。', 3, 1, 1, 1, 120, 0, '2026-10-12 10:00:00', NULL),
('国庆活动通知', '国庆期间注册即送积分，活动已结束。', 4, 0, 1, 2, 30, 0, '2026-10-01 08:00:00', '2026-10-08 00:00:00'),
('一条还没发布的草稿公告', '草稿状态的公告，前台不应展示。', 1, 0, 0, 1, 0, 0, NULL, NULL),
('已被撤回的公告', '这条公告已被撤回，前台不应展示。', 1, 0, 2, 1, 5, 0, '2026-10-06 09:00:00', NULL);

-- ---------- 核对 ----------
SELECT '用户' AS 表, COUNT(*) AS 条数 FROM `user`
UNION ALL SELECT '分类', COUNT(*) FROM `category`
UNION ALL SELECT '标签', COUNT(*) FROM `tag`
UNION ALL SELECT '文章', COUNT(*) FROM `article`
UNION ALL SELECT '文章标签关联', COUNT(*) FROM `article_tag`
UNION ALL SELECT '公告', COUNT(*) FROM `announcement`;

-- =============================================================================
-- 个人博客系统 personal_blog 数据库设计（MySQL 8.0）
-- 字符集：utf8mb4 / utf8mb4_0900_ai_ci    存储引擎：InnoDB
-- 共 7 张表：用户表、分类表、标签表、文章表、文章标签关联表、通知公告表、操作日志表
-- =============================================================================

DROP DATABASE IF EXISTS `personal_blog`;
CREATE DATABASE `personal_blog`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE `personal_blog`;

-- -----------------------------------------------------------------------------
-- 1. 用户表 user
-- -----------------------------------------------------------------------------
CREATE TABLE `user` (
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',
    `username`        VARCHAR(50)     NOT NULL                COMMENT '登录用户名，唯一',
    `password`        VARCHAR(100)    NOT NULL                COMMENT '登录密码，存 BCrypt 加密后的密文',
    `nickname`        VARCHAR(50)     NOT NULL DEFAULT ''     COMMENT '昵称，页面展示名称',
    `email`           VARCHAR(100)             DEFAULT NULL   COMMENT '邮箱，唯一',
    `phone`           VARCHAR(20)              DEFAULT NULL   COMMENT '手机号',
    `avatar`          VARCHAR(255)             DEFAULT NULL   COMMENT '头像图片 URL',
    `gender`          TINYINT         NOT NULL DEFAULT 0      COMMENT '性别：0-未知 1-男 2-女',
    `bio`             VARCHAR(255)             DEFAULT NULL   COMMENT '个人简介',
    `role`            TINYINT         NOT NULL DEFAULT 2      COMMENT '角色：0-超级管理员 1-普通管理员 2-注册用户',
    `status`          TINYINT         NOT NULL DEFAULT 1      COMMENT '状态：0-禁用 1-正常',
    `last_login_time` DATETIME                 DEFAULT NULL   COMMENT '最后登录时间',
    `last_login_ip`   VARCHAR(45)              DEFAULT NULL   COMMENT '最后登录IP，兼容IPv6',
    `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`      TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    UNIQUE KEY `uk_user_email` (`email`),
    KEY `idx_user_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- -----------------------------------------------------------------------------
-- 2. 分类表 category
-- -----------------------------------------------------------------------------
CREATE TABLE `category` (
    `id`          INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '分类ID，主键',
    `name`        VARCHAR(50)   NOT NULL                COMMENT '分类名称',
    `slug`        VARCHAR(50)   NOT NULL                COMMENT '分类别名，用于URL，唯一',
    `description` VARCHAR(255)           DEFAULT NULL   COMMENT '分类描述',
    `parent_id`   INT UNSIGNED  NOT NULL DEFAULT 0      COMMENT '父分类ID，0-顶级分类',
    `sort_order`  INT           NOT NULL DEFAULT 0      COMMENT '排序值，越小越靠前',
    `status`      TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：0-禁用 1-启用',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_slug` (`slug`),
    KEY `idx_category_parent` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '分类表';

-- -----------------------------------------------------------------------------
-- 3. 标签表 tag
-- -----------------------------------------------------------------------------
CREATE TABLE `tag` (
    `id`            INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '标签ID，主键',
    `name`          VARCHAR(50)   NOT NULL                COMMENT '标签名称，唯一',
    `slug`          VARCHAR(50)   NOT NULL                COMMENT '标签别名，用于URL，唯一',
    `description`   VARCHAR(255)           DEFAULT NULL   COMMENT '标签描述',
    `article_count` INT           NOT NULL DEFAULT 0      COMMENT '该标签下的文章数（冗余统计字段）',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_name` (`name`),
    UNIQUE KEY `uk_tag_slug` (`slug`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '标签表';

-- -----------------------------------------------------------------------------
-- 4. 文章表 article
-- -----------------------------------------------------------------------------
CREATE TABLE `article` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文章ID，主键',
    `title`         VARCHAR(200)    NOT NULL                COMMENT '文章标题',
    `summary`       VARCHAR(500)             DEFAULT NULL   COMMENT '文章摘要，列表页展示',
    `content`       LONGTEXT        NOT NULL                COMMENT '文章正文，Markdown 原文',
    `cover_image`   VARCHAR(255)             DEFAULT NULL   COMMENT '封面图 URL',
    `category_id`   INT UNSIGNED             DEFAULT NULL   COMMENT '所属分类ID，关联 category.id',
    `author_id`     BIGINT UNSIGNED NOT NULL                COMMENT '作者ID，关联 user.id',
    `status`        TINYINT         NOT NULL DEFAULT 0      COMMENT '状态：0-草稿 1-已发布 2-已下架',
    `is_top`        TINYINT         NOT NULL DEFAULT 0      COMMENT '是否置顶：0-否 1-是',
    `is_original`   TINYINT         NOT NULL DEFAULT 1      COMMENT '是否原创：0-转载 1-原创',
    `source_url`    VARCHAR(255)             DEFAULT NULL   COMMENT '转载原文地址',
    `view_count`    INT             NOT NULL DEFAULT 0      COMMENT '阅读量',
    `like_count`    INT             NOT NULL DEFAULT 0      COMMENT '点赞数',
    `comment_count` INT             NOT NULL DEFAULT 0      COMMENT '评论数',
    `publish_time`  DATETIME                 DEFAULT NULL   COMMENT '发布时间',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_category` (`category_id`),
    KEY `idx_article_author` (`author_id`),
    KEY `idx_article_status_publish` (`status`, `publish_time`),
    FULLTEXT KEY `ft_article_title_content` (`title`, `content`),
    CONSTRAINT `fk_article_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_article_author`   FOREIGN KEY (`author_id`)   REFERENCES `user` (`id`)     ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文章表';

-- -----------------------------------------------------------------------------
-- 5. 文章标签关联表 article_tag（文章与标签为多对多关系）
-- -----------------------------------------------------------------------------
CREATE TABLE `article_tag` (
    `article_id` BIGINT UNSIGNED NOT NULL COMMENT '文章ID，关联 article.id',
    `tag_id`     INT UNSIGNED    NOT NULL COMMENT '标签ID，关联 tag.id',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关联创建时间',
    PRIMARY KEY (`article_id`, `tag_id`),
    KEY `idx_article_tag_tag` (`tag_id`),
    CONSTRAINT `fk_article_tag_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_article_tag_tag`     FOREIGN KEY (`tag_id`)     REFERENCES `tag` (`id`)     ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文章标签关联表';

-- -----------------------------------------------------------------------------
-- 6. 通知公告表 announcement
-- -----------------------------------------------------------------------------
CREATE TABLE `announcement` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '公告ID，主键',
    `title`        VARCHAR(200)    NOT NULL                COMMENT '公告标题',
    `content`      TEXT            NOT NULL                COMMENT '公告内容',
    `type`         TINYINT         NOT NULL DEFAULT 1      COMMENT '公告类型：1-系统通知 2-维护公告 3-版本更新 4-活动通知',
    `priority`     TINYINT         NOT NULL DEFAULT 0      COMMENT '优先级：0-普通 1-重要 2-紧急',
    `status`       TINYINT         NOT NULL DEFAULT 0      COMMENT '状态：0-草稿 1-已发布 2-已撤回',
    `publisher_id` BIGINT UNSIGNED NOT NULL                COMMENT '发布人ID，关联 user.id',
    `view_count`   INT             NOT NULL DEFAULT 0      COMMENT '查看次数',
    `is_top`       TINYINT         NOT NULL DEFAULT 0      COMMENT '是否置顶：0-否 1-是',
    `publish_time` DATETIME                 DEFAULT NULL   COMMENT '发布时间',
    `expire_time`  DATETIME                 DEFAULT NULL   COMMENT '失效时间，NULL 表示永久有效',
    `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_ann_status_publish` (`status`, `publish_time`),
    KEY `idx_ann_publisher` (`publisher_id`),
    CONSTRAINT `fk_announcement_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '通知公告表';

-- -----------------------------------------------------------------------------
-- 7. 操作日志表 operation_log
-- -----------------------------------------------------------------------------
CREATE TABLE `operation_log` (
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID，主键',
    `user_id`        BIGINT UNSIGNED          DEFAULT NULL   COMMENT '操作人ID，关联 user.id，NULL 表示未登录',
    `username`       VARCHAR(50)              DEFAULT NULL   COMMENT '操作人用户名（冗余，便于用户删除后追溯）',
    `module`         VARCHAR(50)              DEFAULT NULL   COMMENT '所属模块，如 文章管理、用户管理',
    `operation`      VARCHAR(50)              DEFAULT NULL   COMMENT '操作类型，如 新增、修改、删除、查询、登录',
    `description`    VARCHAR(500)             DEFAULT NULL   COMMENT '操作描述',
    `request_method` VARCHAR(10)              DEFAULT NULL   COMMENT 'HTTP 请求方式：GET/POST/PUT/DELETE',
    `request_url`    VARCHAR(255)             DEFAULT NULL   COMMENT '请求 URL',
    `request_params` TEXT                     DEFAULT NULL   COMMENT '请求参数（JSON 字符串）',
    `ip`             VARCHAR(45)              DEFAULT NULL   COMMENT '操作IP，兼容 IPv6',
    `user_agent`     VARCHAR(500)             DEFAULT NULL   COMMENT '浏览器 User-Agent',
    `duration`       INT                      DEFAULT NULL   COMMENT '耗时，单位毫秒',
    `status`         TINYINT         NOT NULL DEFAULT 1      COMMENT '操作结果：0-失败 1-成功',
    `error_msg`      VARCHAR(500)             DEFAULT NULL   COMMENT '失败时的异常信息',
    `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_log_user` (`user_id`),
    KEY `idx_log_module` (`module`),
    KEY `idx_log_created` (`created_at`),
    CONSTRAINT `fk_operation_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '操作日志表';

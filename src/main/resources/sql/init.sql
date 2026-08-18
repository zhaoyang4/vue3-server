-- ============================================================
-- 用户管理模块 - 数据库初始化脚本
-- 执行方式（任选其一）：
--   1) 用 MySQL 客户端命令行：  mysql -u root -p < init.sql
--   2) 用图形化工具（Navicat / Workbench / IDEA Database）打开本文件执行
-- 前提：MySQL 服务已启动；请用你自己的 root 密码连接。
-- ============================================================

-- 创建数据库（IF NOT EXISTS 保证重复执行不会报错）
CREATE DATABASE IF NOT EXISTS vue3_user
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 切换到该库
USE vue3_user;

-- 如果表已存在先删掉，方便反复重置（首次执行无影响）
DROP TABLE IF EXISTS `user`;

-- 用户表
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `username`    VARCHAR(50)  NOT NULL                COMMENT '登录账号（唯一）',
  `name`        VARCHAR(50)  DEFAULT ''             COMMENT '姓名',
  `phone`       VARCHAR(20)  DEFAULT ''             COMMENT '手机号',
  `email`       VARCHAR(100) DEFAULT ''             COMMENT '邮箱',
  `age`         INT          DEFAULT NULL           COMMENT '年龄',
  `gender`      VARCHAR(10)  DEFAULT ''             COMMENT '性别（男/女/其他）',
  `address`     VARCHAR(255) DEFAULT ''             COMMENT '地址',
  `create_time` DATETIME     DEFAULT NULL           COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT NULL           COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)             -- 账号唯一约束，配合后端去重校验
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

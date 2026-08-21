-- ============================================================
-- V1: 用户表（幂等写法，重复执行不报错、不丢数据）
-- Flyway 规则：版本化迁移脚本一旦执行就【不可修改】，
--   以后要改表结构请新增 V2/V3... 用 ALTER TABLE 增量改。
-- 命名：V{版本}__{描述}.sql（双下划线分隔）
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
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

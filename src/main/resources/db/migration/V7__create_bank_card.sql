-- ============================================================
-- V7: 银行卡表 `bank_card`
-- 用途（金融/电商面试高频知识点）：
--   1) 敏感信息加密存储 —— 卡号/手机号/持卡人 用 AES 加密后存 `card_no` 等密文字段，
--      明文不落库（见 AESUtil + BankCardServiceImpl 入库前加密）。
--   2) 查询脱敏 —— 列表/详情返回 `card_no_mask`（如 6222 **** **** 1234）、
--      `phone_mask`（138****8888）、`holder_name_mask`（张*），不暴露明文（见 DesensitizeUtil）。
--   3) `deleted` 逻辑删除。
-- 注意：密文长度会膨胀，VARCHAR 给到 255；mask 字段存脱敏展示串，无需解密即可展示。
-- ============================================================
CREATE TABLE IF NOT EXISTS `bank_card` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT        NOT NULL                COMMENT '用户ID',
  `bank_name`       VARCHAR(50)   NOT NULL DEFAULT ''     COMMENT '银行名称（明文，如 招商银行）',
  `card_no`         VARCHAR(255)  NOT NULL                COMMENT '银行卡号（AES 加密密文）',
  `card_no_mask`    VARCHAR(32)   DEFAULT ''              COMMENT '卡号脱敏展示（如 6222 **** **** 1234）',
  `holder_name`     VARCHAR(255)  NOT NULL DEFAULT ''     COMMENT '持卡人姓名（AES 加密密文）',
  `holder_name_mask` VARCHAR(32)  DEFAULT ''             COMMENT '姓名脱敏展示（如 张*）',
  `phone`           VARCHAR(255)  NOT NULL DEFAULT ''     COMMENT '预留手机号（AES 加密密文）',
  `phone_mask`      VARCHAR(32)   DEFAULT ''              COMMENT '手机号脱敏展示（如 138****8888）',
  `deleted`         TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除 0未删 1已删',
  `create_time`     DATETIME      DEFAULT NULL            COMMENT '创建时间',
  `update_time`     DATETIME      DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银行卡表（敏感信息加密存储）';

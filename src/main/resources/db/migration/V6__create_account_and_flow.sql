-- ============================================================
-- V6: 资金账户表 `account` + 资金流水表 `account_flow`
-- 用途（金融/电商面试高频知识点）：
--   1) 账户余额 + 流水「双写」必须在同一个事务里，保证一致性（见 AccountServiceImpl.recharge）。
--   2) 流水表用 `biz_no` 唯一约束 + 应用层判重，演示【接口幂等性】（重复支付不会扣两次）。
--   3) `account.version` 乐观锁字段，配合自定义 UPDATE 防并发亏钱（见 updateBalance 乐观锁 SQL）。
--   4) `account.deleted` 逻辑删除字段（0未删 1已删），MyBatis-Plus @TableLogic 自动过滤。
-- ============================================================
CREATE TABLE IF NOT EXISTS `account` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT        NOT NULL                COMMENT '用户ID（一个用户一个账户）',
  `balance`     DECIMAL(12,2) NOT NULL DEFAULT 0.00   COMMENT '账户余额（元，用 DECIMAL 存钱，禁用 double）',
  `version`     INT           NOT NULL DEFAULT 0      COMMENT '乐观锁版本号（每次余额变动 +1）',
  `deleted`     TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除 0未删 1已删',
  `create_time` DATETIME      DEFAULT NULL            COMMENT '创建时间',
  `update_time` DATETIME      DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资金账户表';

CREATE TABLE IF NOT EXISTS `account_flow` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `flow_no`       VARCHAR(32)   NOT NULL                COMMENT '流水号（系统生成，全局唯一）',
  `biz_no`        VARCHAR(32)   DEFAULT NULL            COMMENT '业务单号（如订单号；唯一 → 幂等。充值可为 NULL）',
  `account_id`    BIGINT        NOT NULL                COMMENT '账户ID',
  `user_id`       BIGINT        NOT NULL                COMMENT '用户ID',
  `flow_type`     TINYINT       NOT NULL                COMMENT '流水类型 1充值 2支付 3退款',
  `amount`        DECIMAL(12,2) NOT NULL                COMMENT '变动金额（正数）',
  `balance_after` DECIMAL(12,2) NOT NULL DEFAULT 0.00   COMMENT '变动后余额（便于对账）',
  `remark`        VARCHAR(255)  DEFAULT ''              COMMENT '备注',
  `create_time`   DATETIME      DEFAULT NULL            COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flow_no` (`flow_no`),
  UNIQUE KEY `uk_biz_no`  (`biz_no`),
  KEY `idx_account` (`account_id`),
  KEY `idx_user`    (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金流水表（每笔余额变动都留痕）';

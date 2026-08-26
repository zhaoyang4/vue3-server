-- ============================================================
-- V5: 订单主表 `order` + 订单明细表 `order_item`
-- 说明：
--   - `order` 是 MySQL 保留字，建表/查询都要用反引号 `` `order` `` 包起来。
--   - 下单时在一个事务里写这两张表（见 OrderServiceImpl.createOrder）。
--   - order_item 冗余存了 product_name / product_price（下单时快照），
--     即使以后商品改名/调价，历史订单依然正确。
-- ============================================================
CREATE TABLE IF NOT EXISTS `order` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `order_no`     VARCHAR(32)   NOT NULL                COMMENT '订单号（唯一，业务生成）',
  `user_id`      BIGINT        NOT NULL                COMMENT '下单用户ID',
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00   COMMENT '订单总金额',
  `status`       TINYINT       NOT NULL DEFAULT 0      COMMENT '状态 0待支付 1已支付 2已取消',
  `create_time`  DATETIME      DEFAULT NULL            COMMENT '创建时间',
  `update_time`  DATETIME      DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

CREATE TABLE IF NOT EXISTS `order_item` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `order_id`      BIGINT        NOT NULL                COMMENT '订单ID',
  `product_id`    BIGINT        NOT NULL                COMMENT '商品ID',
  `product_name`  VARCHAR(100)  NOT NULL                COMMENT '商品名称（下单时快照）',
  `product_price` DECIMAL(10,2) NOT NULL                COMMENT '下单时单价',
  `quantity`      INT           NOT NULL DEFAULT 1      COMMENT '购买数量',
  `subtotal`      DECIMAL(12,2) NOT NULL DEFAULT 0.00  COMMENT '小计金额 = 单价 × 数量',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ============================================================
-- V2: 商品表（在 V1 用户表之后新增，体现“增量迁移”）
-- 以后要加字段：再写 V3__add_xxx.sql，例如
--   ALTER TABLE `product` ADD COLUMN `remark` VARCHAR(255) DEFAULT '';
-- ============================================================
CREATE TABLE IF NOT EXISTS `product` (
  `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `name`          VARCHAR(100)   NOT NULL                COMMENT '商品名称',
  `price`         DECIMAL(10,2)  NOT NULL DEFAULT 0.00   COMMENT '价格（元）',
  `purchase_date` DATE           DEFAULT NULL           COMMENT '购买日期',
  `create_time`   DATETIME       DEFAULT NULL           COMMENT '创建时间',
  `update_time`   DATETIME       DEFAULT NULL           COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

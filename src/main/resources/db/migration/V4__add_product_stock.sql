-- ============================================================
-- V4: 商品表增加「库存 stock」与「乐观锁版本号 version」
-- 用途：下单时要扣减库存，并用 version 做乐观锁防并发超卖。
--   ALTER TABLE 不支持 IF NOT EXISTS，依赖 Flyway 保证只执行一次。
--   stock 默认 100，让已有商品立刻有库存可下单测试。
-- ============================================================
ALTER TABLE `product`
  ADD COLUMN `stock`   INT  NOT NULL DEFAULT 100 COMMENT '库存数量' AFTER `purchase_date`,
  ADD COLUMN `version` INT NOT NULL DEFAULT 0   COMMENT '乐观锁版本号（每次更新 +1）' AFTER `stock`;

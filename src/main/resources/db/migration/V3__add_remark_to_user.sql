-- ============================================================
-- V3: 给用户表增加 remark（备注）字段
-- 增量改表用 ALTER TABLE；Flyway 保证本脚本只执行一次，
--   即使重复启动也不会再跑（history 表已记录 V3 成功）。
-- 注意：MySQL 的 ALTER TABLE ADD COLUMN 不支持 IF NOT EXISTS，
--   所以这里直接写，依赖 Flyway 的幂等性，不要手写重复执行逻辑。
-- ============================================================
ALTER TABLE `user`
  ADD COLUMN `remark` VARCHAR(255) DEFAULT NULL COMMENT '用户备注' AFTER `address`;

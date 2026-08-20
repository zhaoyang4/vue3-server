-- ============================================================
-- user 表常用查询小抄（MySQL）
-- 适用表结构：
--   id, username, name, phone, email, age, gender, address, create_time, update_time
-- 用法：
--   在 IDEA Database 面板里，右键数据源 → New → Query Console，
--   把下面任意一条粘进去，选中后按 Ctrl + Enter 执行。
-- 口诀（SELECT 五段式）：
--   SELECT 列 FROM 表 WHERE 条件 GROUP BY 分组 HAVING 过滤 ORDER BY 排序 LIMIT 条数
-- ============================================================


-- ① 查全部（数据多时一定加 LIMIT，别裸跑 SELECT *）
SELECT * FROM user;


-- ② 只查需要的列（比 SELECT * 更规范、更省资源）
SELECT id, username, name, phone, age FROM user;


-- ③ 按登录账号精确查（username 是唯一键，结果最多 1 条）
SELECT * FROM user WHERE username = 'xiaolan';


-- ④ 按姓名模糊查（% 代表任意长度字符，_ 代表单个字符）
SELECT * FROM user WHERE name LIKE '%小%';
SELECT * FROM user WHERE name LIKE '张%';   -- 姓张的


-- ⑤ 多条件：AND 表示"同时满足"，OR 表示"满足任一"
SELECT * FROM user WHERE age > 18 AND gender = '男';
SELECT * FROM user WHERE name LIKE '%小%' OR address LIKE '%北京%';


-- ⑥ 年龄范围（BETWEEN 包含两端边界）
SELECT * FROM user WHERE age BETWEEN 18 AND 60;


-- ⑦ 排序：DESC 倒序（最新在前），ASC 正序（默认）
SELECT * FROM user ORDER BY create_time DESC;
SELECT * FROM user ORDER BY age ASC;


-- ⑧ 分页（测试/前端联调必会）：LIMIT 偏移量, 每页条数
SELECT * FROM user ORDER BY id LIMIT 0, 10;    -- 第 1 页，每页 10 条
SELECT * FROM user ORDER BY id LIMIT 10, 10;   -- 第 2 页，每页 10 条
-- 公式：第 N 页 偏移 = (N-1) * 每页条数


-- ⑨ 计数：一共多少条 / 某条件下多少条
SELECT COUNT(*) FROM user;
SELECT COUNT(*) FROM user WHERE gender = '女';


-- ⑩ 空值判断（age 允许为 NULL，空字符串 '' 和 NULL 是两回事）
SELECT * FROM user WHERE age IS NULL;
SELECT * FROM user WHERE email = '' OR email IS NULL;


-- ⑪ 时间范围
SELECT * FROM user WHERE create_time >= '2026-08-01 00:00:00';
SELECT * FROM user WHERE create_time BETWEEN '2026-08-01 00:00:00' AND '2026-08-31 23:59:59';


-- ⑫ 去重：看性别字段都有哪些取值
SELECT DISTINCT gender FROM user;


-- ⑬ 组合实战：姓名含"小"、年龄≥18、按创建时间倒序取前 10 条
SELECT id, username, name, age, create_time
FROM user
WHERE name LIKE '%小%' AND age >= 18
ORDER BY create_time DESC
LIMIT 10;


-- ============================================================
-- 进阶：聚合统计（按性别分组统计人数）
-- ============================================================
SELECT gender, COUNT(*) AS 人数
FROM user
GROUP BY gender
ORDER BY 人数 DESC;

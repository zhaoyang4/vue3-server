# vue3-server · 用户管理后端

一个最小但完整的 **Spring Boot 3 + Java 21 + MyBatis-Plus + MySQL** 后端项目，  
提供「用户信息」的增 / 查 / 改 / 删（CRUD）接口，供前端 Vue3 项目通过 HTTP 调用。

代码注释详细，适合对照学习后端分层结构（Controller / Service / Mapper / Entity）。

---

## 一、环境要求

| 工具    | 版本         | 说明                               |
| ----- | ---------- | -------------------------------- |
| JDK   | **21**     | 已在 `pom.xml` 的 `java.version` 指定 |
| MySQL | 5.7+ / 8.x | 提供数据库存储                          |
| Maven | 可选         | 项目已自带 **Maven Wrapper**，无需全局安装   |

---

## 二、目录结构

cd vue3-server mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"  

---

## 三、第一步：准备数据库

1. 确保 MySQL 服务已启动。
2. 用你的 root（或任意有建库权限）账号执行初始化脚本：
   ```bash
   mysql -u root -p < src/main/resources/sql/init.sql
   ```
   脚本会创建数据库 `vue3_user` 和 `user` 表（重复执行安全）。

---

## 四、第二步：配置数据库连接

打开 `src/main/resources/application.yml`，把 **password** 改成你本机 MySQL 的真实密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vue3_user?...（一般不用改）
    username: root
    password: 这里改成你的真实密码   # ← 重点改这一行
```

> 如果 MySQL 不在本机、或端口/库名不同，相应修改 `url` 即可。

---

## 五、第三步：启动后端

进入本项目目录后，任选一种方式：

**方式 A：用自带 Wrapper（推荐，无需装 Maven）**

```bash
# Windows
mvnw.cmd spring-boot:run
# macOS / Linux
./mvnw spring-boot:run
```

**方式 B：已全局安装 Maven**

```bash
mvn spring-boot:run
```

**方式 C：打成 jar 后运行**

```bash
mvn package          # 生成 target/vue3-server-1.0.0.jar
java -jar target/vue3-server-1.0.0.jar
```

启动成功后，控制台会出现：

```
Tomcat started on port 8080 (http://localhost:8080)
```

此时可以用浏览器 / Postman 直接验证接口，例如：

```
GET http://localhost:8080/api/users          # 查询列表
POST http://localhost:8080/api/users         # 新增（body 传 JSON）
```

---

## 六、接口一览（RESTful）

| 方法     | 路径                                      | 说明                     |
| ------ | --------------------------------------- | ---------------------- |
| POST   | `/api/users`                            | 新增用户（body 传 User JSON） |
| GET    | `/api/users?current=1&size=10&keyword=` | 分页 + 关键字查询             |
| GET    | `/api/users/{id}`                       | 根据 id 查询单个             |
| PUT    | `/api/users/{id}`                       | 根据 id 修改               |
| DELETE | `/api/users/{id}`                       | 根据 id 删除               |

统一返回格式：

```json
{ "code": 0, "message": "ok", "data": { ... } }
```

- `code = 0` 表示成功；非 0 表示业务失败（如「账号已存在」）。

---

## 七、与前端的联调

前端项目（`vue3`）在开发模式下已配置 Vite 代理：把 `/api` 请求转发到 `http://localhost:8080`，  
所以前端直接请求 `/api/users` 即可，无需处理跨域。详见前端项目 README 的「用户管理模块」一节。

> 排错小贴士：
>
> - 启动报错 `Communications link failure` → MySQL 没启动或密码/地址错。
> - 启动报错 `Unknown database 'vue3_user'` → 没执行 init.sql。
> - 接口返回 500 → 看控制台 SQL 报错，通常是字段/类型不匹配。

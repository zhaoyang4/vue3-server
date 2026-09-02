# 部署手册：把「本地能跑」变成「可部署」

> 适用项目：后端 `vue3-server`（Spring Boot 3 + MyBatis-Plus + MySQL 8 + Flyway）+ 前端 `vue3`（Vue3 + Vite）
>
> **本地开发方式完全没变**：后端照旧 `mvnw.cmd spring-boot:run`，前端照旧 `npm run dev`。
> 下面所有改造都是「加了一条生产通道」，不是「改掉了原来的路」。

---

## 一、6 件事都落在哪个文件

| # | 事项 | 后端改动 | 前端改动 |
|---|------|---------|---------|
| 1 | 配置外置 | `application.yml` 全部改成 `${环境变量:默认值}`；新增 `application-prod.yml`（DB 地址/端口/账号/密码/JWT 密钥零硬编码）；新增 `config/AppProperties.java` 统一接配置；新增 `config/EnvGuard.java` 启动前体检 | 新增 `.env.development` / `.env.production` / `.env.example`；`env.d.ts` 补类型声明 |
| 2 | 后端打包 | `pom.xml` 加 `<finalName>vue3-server</finalName>`，产物固定为 `target/vue3-server.jar`；加 Actuator 依赖 | — |
| 3 | 前端 build | — | 新增 `src/api/http.ts` 统一 axios 实例，`baseURL` 读 `VITE_API_BASE`；6 个 api 文件不再各自 `axios.create`；`vite.config.js` 代理目标/产物参数可配 |
| 4 | 数据库自动化 | Flyway 已就绪，连接信息全走环境变量；`FLYWAY_ENABLED` 可开关 | — |
| 5 | 容器化 | `Dockerfile`（两阶段）、`docker-compose.yml`（MySQL+后端+前端）、`.dockerignore`、`.env.example` | `Dockerfile`（Node build → Nginx）、`nginx.conf`、`.dockerignore` |
| 6 | 守护 + 安全组 + CORS | `deploy/vue3-server.service`（systemd）、`deploy/app.sh`（nohup 启停）、`deploy/env.sh.example`；`config/CorsConfig.java` 全局跨域（已移除 `UserController` 上的 `@CrossOrigin`）；`server.shutdown=graceful` 优雅停机 | `deploy/nginx-host.conf`（裸机 Nginx，含 history fallback） |

---

## 二、本地开发（一点没变，确认一下就行）

```bash
# 后端：默认 dev profile，连本机 MySQL，所有默认值和以前一致
cd vue3-server
./mvnw.cmd spring-boot:run

# 前端：dev proxy 把 /api 转发到 localhost:8080
cd vue3
npm run dev        # http://localhost:5173
```

想临时连别的库？不用改代码，设个环境变量就行：

```powershell
$env:DB_HOST="192.168.1.20"; $env:DB_PASSWORD="xxx"
./mvnw.cmd spring-boot:run
```

---

## 三、方案 A：Docker 一键部署（推荐）

> ⚠ **国内网络必读（否则 build / pull 会卡死、超时）**：你本地和国内服务器拉取 Docker Hub / Maven Central / npm 经常连不上。部署前必须完成三处本土化（Docker Hub 镜像加速器 + Maven 阿里云 + npm 淘宝），详见文末「十、国内网络环境专属配置」。本项目已配好，换机器照抄即可。

**前提**：服务器装好 Docker（`curl -fsSL https://get.docker.com | sh`，或系统包管理器）。前后端两个目录同级摆放。

```bash
cd vue3-server
cp .env.example .env
vim .env                      # 填 MySQL 密码、JWT_SECRET（openssl rand -base64 48 生成）

docker compose up -d --build  # 首次构建约 3~5 分钟
docker compose ps             # 三个容器都 healthy 才算成功
```

访问 `http://服务器IP`，探活 `curl http://服务器IP/actuator/health` → `{"status":"UP"}`。

它帮你干了这些事：

```
浏览器 → :80 前端容器(Nginx)
             ├── /            静态页，刷新不 404（history fallback）
             └── /api/*  ──→  backend:8080（容器网络内部，同源，不跨域）
                                    └──→ mysql:3306（Flyway 自动建表）
```

对外只暴露 1 个端口（80），MySQL 和后端端口默认只绑 `127.0.0.1`，公网连不上。

常用命令：

```bash
docker compose logs -f backend    # 看后端日志
docker compose restart backend    # 只重启后端
docker compose up -d --build      # 改完代码重新发版
docker compose down               # 停止（数据保留在命名卷 mysql-data）
docker compose down -v            # ⚠ 连数据库数据一起删
```

---

## 四、方案 B：裸机部署（不用 Docker）

### 1. 后端

```bash
# 本机打包
cd vue3-server && ./mvnw.cmd package -DskipTests     # 产物 target/vue3-server.jar

# 上传
scp target/vue3-server.jar root@服务器IP:/opt/vue3-server/
scp deploy/app.sh deploy/env.sh.example root@服务器IP:/opt/vue3-server/

# 服务器上配置 + 启动
cd /opt/vue3-server
cp env.sh.example env.sh && vim env.sh && chmod 600 env.sh
chmod +x app.sh && ./app.sh start
./app.sh status
```

要开机自启 / 崩溃自动拉起，用 systemd（推荐，步骤见 `deploy/vue3-server.service` 文件头注释）：

```bash
cp deploy/vue3-server.service /etc/systemd/system/
systemctl daemon-reload && systemctl enable --now vue3-server
systemctl status vue3-server
journalctl -u vue3-server -f
```

### 2. 前端

```bash
cd vue3 && npm run build                       # 产物 dist/
scp -r dist/* root@服务器IP:/var/www/vue3/
# 服务器上
cp deploy/nginx-host.conf /etc/nginx/conf.d/vue3.conf
nginx -t && systemctl reload nginx
```

### 3. 数据库

MySQL 装好后建库建账号即可，**表结构不用管**，后端启动时 Flyway 会按 V1→V7 自动建好：

```sql
CREATE DATABASE vue3_user DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'app_user'@'localhost' IDENTIFIED BY '强密码';
GRANT ALL PRIVILEGES ON vue3_user.* TO 'app_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## 五、环境变量清单

### 后端（application-prod.yml 消费）

| 变量 | 必填 | 默认 | 说明 |
|------|:---:|------|------|
| `SPRING_PROFILES_ACTIVE` | 是 | dev | 生产填 `prod` |
| `DB_HOST` | 是 | — | 数据库地址；Docker 内填服务名 `mysql` |
| `DB_PORT` | 否 | 3306 | |
| `DB_NAME` | 是 | — | 库名 |
| `DB_USERNAME` | 是 | — | 别用 root |
| `DB_PASSWORD` | 是 | — | 绝不写进代码 |
| `JWT_SECRET` | 是 | — | ≥32 位随机串，`openssl rand -base64 48` |
| `CORS_ALLOWED_ORIGINS` | 是 | — | 前端地址，逗号分隔，**别写 `*`** |
| `SERVER_PORT` | 否 | 8080 | |
| `LOG_FILE` | 否 | ./logs/vue3-server.log | |
| `DB_POOL_MAX` | 否 | 20 | 连接池上限 |
| `FLYWAY_ENABLED` | 否 | true | 极端情况可临时关掉迁移 |

> 必填项没设 → **启动前就被拦下**，控制台直接列出缺哪几个变量（`config/EnvGuard.java` 干的）：
>
> ```
> ============================================================
>  启动中止：profile=prod 缺少必需的环境变量
> ------------------------------------------------------------
>    缺少 -> DB_HOST
>    缺少 -> DB_PASSWORD
>    缺少 -> JWT_SECRET
> ------------------------------------------------------------
> ```
>
> 没有这层体检的话，Spring 会把 `${DB_HOST}` 原样当主机名去连库，
> 只留下一句 `UnknownHostException: ${DB_HOST}` 让人猜半天。早失败、报清楚，才是可部署的样子。

### 前端（build 时烧进产物，改了必须重新 build）

| 变量 | 默认 | 说明 |
|------|------|------|
| `VITE_API_BASE` | /api | `/api`=走 Nginx 同源反代（推荐）；或写 `http://IP:8080/api` 直连后端 |
| `VITE_PROXY_TARGET` | http://localhost:8080 | 仅 dev 用，代理转发目标 |
| `VITE_BASE` | / | 部署到子路径时用，如 `/admin/` |
| `VITE_API_TIMEOUT` | 10000 | 请求超时（毫秒） |

---

## 六、安全组 / 防火墙

| 端口 | 对外开放 | 说明 |
|------|:-------:|------|
| 80（或 `WEB_PORT`） | ✅ 放行 | 前端唯一入口 |
| 443 | ✅ 上 HTTPS 时放行 | 建议用 certbot 免费证书 |
| 8080 | ❌ 不放行 | Nginx 内部转发即可；只在需要前端直连调试时临时放行 |
| 3306 | ❌ 绝不放行公网 | 只允许本机/内网。compose 已绑定 `127.0.0.1` |
| 22 | ⚠ 限制来源 IP | |

```bash
# Ubuntu ufw 示例
ufw allow 80/tcp && ufw allow 443/tcp && ufw enable
ufw status
```

---

## 七、跨域（CORS）怎么理解和排错

**最省事的做法：让浏览器看不到跨域。** 前端和 `/api` 都挂在同一域名下，由 Nginx 反代到后端 → 同源 → CORS 根本不触发。方案 A 和方案 B 都是这么设计的。

只有「前端直连后端 IP」时才需要 CORS：

1. 前端 `.env.production` 写 `VITE_API_BASE=http://后端IP:8080/api`，重新 build；
2. 后端设 `CORS_ALLOWED_ORIGINS=http://前端域名`（协议+域名+端口要完全一致，末尾不要带 `/`）；
3. 安全组放行 8080。

排错三步：

| 现象 | 原因 | 解决 |
|------|------|------|
| 控制台 `No 'Access-Control-Allow-Origin'` | 前端来源不在放行名单 | 检查 `CORS_ALLOWED_ORIGINS` 拼写，`http` 和 `https` 算不同来源 |
| OPTIONS 请求 403/404 | 预检被拦 | 后端已放行 OPTIONS；若前面还有网关，检查网关是否吞了 OPTIONS |
| 接口 404，路径变成 `/users` | Nginx 把 `/api` 前缀截掉了 | 保持 `proxy_pass` 带 `$request_uri`（本项目配置已处理） |

启动日志里会打印实际放行名单，一眼可查：

```
[CORS] 已放行的前端来源: http://localhost:5173, http://127.0.0.1:5173
```

---

## 八、上线自检清单

```bash
curl -s http://IP/actuator/health          # {"status":"UP"}
curl -s http://IP/api/users?page=1&size=1  # 返回 {"code":200,...}
# 浏览器打开 http://IP/users 后按 F5 刷新 —— 不 404 说明 history fallback 生效
docker compose logs backend | grep -i "flyway\|Migrating\|Successfully applied"   # 迁移记录
```

- [ ] `.env` / `env.sh` 权限 600，且没被 git 追踪（`git status` 看不到）
- [ ] 数据库账号不是 root
- [ ] `JWT_SECRET` 是随机生成的，不是示例值
- [ ] 3306 没有对公网开放
- [ ] 生产日志级别是 info，SQL 打印已关闭（prod 默认如此）

---

## 九、发版流程（改完代码怎么上线）

**Docker：**
```bash
git pull && docker compose up -d --build && docker compose ps
```

**裸机：**
```bash
# 后端
./mvnw.cmd package -DskipTests
scp target/vue3-server.jar root@IP:/opt/vue3-server/
ssh root@IP "systemctl restart vue3-server"     # 或 /opt/vue3-server/app.sh restart
# 前端
npm run build && scp -r dist/* root@IP:/var/www/vue3/
```

数据库改动只需新增 `V8__xxx.sql` 放进 `src/main/resources/db/migration/`，重启后 Flyway 自动执行 —— 别手工上生产库敲 SQL。

---

## 十、国内网络环境专属配置（Docker 部署必读）

如果你在**国内网络**（家用宽带、国内云服务器）部署，拉取 Docker Hub 镜像、Maven 依赖、npm 包经常超时/断开。本项目已内置三处本土化改动，换机器部署时照抄即可。

### 1. Docker Hub 镜像加速器（解决 `pull` 超时）

Docker Desktop（Windows/Mac）或 Linux `/etc/docker/daemon.json` 加：

```json
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com",
    "https://docker.nju.edu.cn",
    "https://docker.mirrors.sjtug.sjtu.edu.cn",
    "https://docker.mirrors.ustc.edu.cn"
  ]
}
```

改完 Apply & restart（Linux 是 `systemctl restart docker`）。验证：`docker info | grep -A5 "Registry Mirrors"` 能看到上面的地址。

### 2. 后端 Maven 走阿里云（解决 `mvn dependency:go-offline` 卡死）

已新增 `maven-settings.xml`（阿里云 Maven 镜像），并在 `Dockerfile` 里 `COPY` 到 `/root/.m2/settings.xml` 后执行 `mvn`。**裸机打包**同样适用：把该文件放到 `~/.m2/settings.xml` 即可。

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>*</mirrorOf>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
```

### 3. 前端 npm 走淘宝镜像（解决 `npm ci` 超时）

已在 `vue3/Dockerfile` 的 `npm ci` 前加了一行：

```dockerfile
RUN npm config set registry https://registry.npmmirror.com
```

裸机 `npm install` 同理，先执行上面这行再装。

### 为什么必须做

| 默认源 | 国内现象 |
|--------|----------|
| Docker Hub | `connectex: connection timed out`，pull 卡死 |
| Maven Central | `dependency:go-offline` 跑 30+ 分钟仍失败（exit 1） |
| npmjs.org | `npm ci` 中途断连 |

### 上云后要不要保留

- **国内云服务器（阿里云/腾讯云）**：这些镜像站通常**更快**，建议保留。
- **境外服务器**：可去掉镜像加速器 / Maven 配置，直连官方源速度反而更好；npm 淘宝镜像留着也无妨。

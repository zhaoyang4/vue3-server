# ============================================================
# 后端镜像：两阶段构建
#   阶段一 build  ：用 Maven 编译打包出 fat jar（等于你本地 mvnw package）
#   阶段二 runtime：只带 JRE + jar，不含 Maven / 源码 / .m2 缓存
#
# 好处：镜像从 ~800MB 降到 ~250MB，且服务器上不需要装 JDK 和 Maven
# ============================================================

# ---------- 阶段一：编译 ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# 先只拷 pom：依赖没变时这层走缓存，改代码不用重新下载全部依赖
COPY pom.xml .
# 使用阿里云 Maven 镜像，避免国内连 Maven Central 超时/失败
COPY maven-settings.xml /root/.m2/settings.xml
# go-offline 把依赖预先拉到本地仓库；-B 表示批处理模式（不输出交互式进度条）
RUN mvn -B -q dependency:go-offline

# 再拷源码编译
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- 阶段二：运行 ----------
FROM eclipse-temurin:21-jre

# 时区 + curl（healthcheck 要用）
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone \
    && apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# 用非 root 用户跑应用：容器被攻破时影响面更小（安全基本功）
RUN useradd -r -u 1001 -m appuser
WORKDIR /app
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# 从编译阶段拷 jar（pom 里配了 finalName，名字固定不带版本号）
COPY --from=build /build/target/vue3-server.jar app.jar

USER appuser

EXPOSE 8080

# 默认跑生产 profile；具体的数据库地址/密码/JWT 密钥由 docker-compose 的 environment 注入
ENV SPRING_PROFILES_ACTIVE=prod \
    LOG_FILE=/app/logs/vue3-server.log \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Dfile.encoding=UTF-8"

# 健康检查：Spring Boot Actuator 的 /actuator/health
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -fs http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"' || exit 1

# 用 sh -c 才能让 $JAVA_OPTS 展开；exec 让 java 成为 1 号进程，容器 stop 时能收到信号优雅退出
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

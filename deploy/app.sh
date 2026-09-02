#!/usr/bin/env bash
# ============================================================
# 轻量启停脚本（nohup 版）——没有 root 权限、用不了 systemd 时的方案
#
# 用法：
#   ./app.sh start     启动（后台常驻，SSH 断开也不会退出）
#   ./app.sh stop      优雅停止
#   ./app.sh restart   重启（发版后执行）
#   ./app.sh status    查看状态
#   ./app.sh log       实时跟踪日志
#
# 前置：同目录下有 vue3-server.jar，以及 env.sh（放数据库密码等，权限 600）
# ============================================================

set -euo pipefail

# 脚本所在目录，保证在任何路径下执行都能找到 jar
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_NAME="vue3-server"
JAR="$APP_DIR/$APP_NAME.jar"
PID_FILE="$APP_DIR/$APP_NAME.pid"
LOG_DIR="$APP_DIR/logs"
CONSOLE_LOG="$LOG_DIR/console.out"

# JVM 参数：按容器/机器内存自适应堆大小
JAVA_OPTS="${JAVA_OPTS:--XX:MaxRAMPercentage=75 -XX:+UseG1GC -Dfile.encoding=UTF-8}"

mkdir -p "$LOG_DIR"

# ---- 加载环境变量（配置外置：密码只存在这个文件里，不进 Git、不进 jar）----
load_env() {
  if [[ -f "$APP_DIR/env.sh" ]]; then
    # shellcheck disable=SC1091
    set -a; source "$APP_DIR/env.sh"; set +a
    echo "[info] 已加载环境变量文件 env.sh"
  else
    echo "[warn] 未找到 $APP_DIR/env.sh，将依赖当前 shell 已有的环境变量"
  fi
  export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"
  export LOG_FILE="${LOG_FILE:-$LOG_DIR/$APP_NAME.log}"
}

running_pid() {
  if [[ -f "$PID_FILE" ]]; then
    local pid; pid="$(cat "$PID_FILE")"
    if kill -0 "$pid" 2>/dev/null; then echo "$pid"; return 0; fi
  fi
  return 1
}

start() {
  if pid="$(running_pid)"; then
    echo "[skip] 已经在运行，PID=$pid"; return 0
  fi
  [[ -f "$JAR" ]] || { echo "[error] 找不到 $JAR，先执行 mvn package -DskipTests 并上传"; exit 1; }

  load_env
  echo "[info] profile=$SPRING_PROFILES_ACTIVE  启动中..."
  # nohup + & ：脱离终端后台运行；输出重定向到 console.out
  nohup java $JAVA_OPTS -jar "$JAR" > "$CONSOLE_LOG" 2>&1 &
  echo $! > "$PID_FILE"
  sleep 3
  if pid="$(running_pid)"; then
    echo "[ok] 已启动 PID=$pid"
    echo "     健康检查： curl -s http://127.0.0.1:${SERVER_PORT:-8080}/actuator/health"
    echo "     看日志：   ./app.sh log"
  else
    echo "[error] 启动失败，日志最后 30 行："; tail -30 "$CONSOLE_LOG"; exit 1
  fi
}

stop() {
  if ! pid="$(running_pid)"; then
    echo "[skip] 进程不在运行"; rm -f "$PID_FILE"; return 0
  fi
  echo "[info] 发送 SIGTERM，等待优雅停机（最多 30 秒）..."
  kill "$pid"
  for _ in $(seq 1 30); do
    kill -0 "$pid" 2>/dev/null || { echo "[ok] 已停止"; rm -f "$PID_FILE"; return 0; }
    sleep 1
  done
  echo "[warn] 超时未退出，强制 kill -9"
  kill -9 "$pid" 2>/dev/null || true
  rm -f "$PID_FILE"
}

status() {
  if pid="$(running_pid)"; then
    echo "[running] PID=$pid"
    curl -s "http://127.0.0.1:${SERVER_PORT:-8080}/actuator/health" || echo "(健康检查无响应，可能还在启动中)"
    echo
  else
    echo "[stopped]"
  fi
}

case "${1:-}" in
  start)   start ;;
  stop)    stop ;;
  restart) stop; start ;;
  status)  status ;;
  log)     tail -f "${LOG_FILE:-$LOG_DIR/$APP_NAME.log}" 2>/dev/null || tail -f "$CONSOLE_LOG" ;;
  *) echo "用法: $0 {start|stop|restart|status|log}"; exit 1 ;;
esac

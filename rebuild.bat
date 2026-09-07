@echo off
chcp 65001 >nul
cd /d "F:\workBuddy\space\vue3-server"

REM 没有 .env 就先提示，避免 docker compose 因变量为空报错
if not exist ".env" (
  echo [错误] 找不到 .env 文件！
  echo 请先复制 .env.example 为 .env 并填好数据库密码 / JWT 密钥：
  echo   copy .env.example .env
  pause
  exit /b 1
)

echo ============================================
echo  重新构建并启动 前后端 + MySQL
echo  目录: %CD%
echo ============================================
echo.

docker compose up -d --build

echo.
echo ============================================
echo  完成！容器已在后台运行：
echo    - 前端:  http://localhost
echo    - 后端:  http://localhost/actuator/health
echo  查看日志: docker compose logs -f backend
echo ============================================
echo.
pause

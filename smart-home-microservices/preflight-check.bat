@echo off
chcp 65001 >nul
echo ========================================
echo   智能家居微服务 - 启动前检查
echo ========================================
echo.

set ERR=0

echo [1] MySQL (端口 5455)...
powershell -NoProfile -Command "try { $c = New-Object System.Net.Sockets.TcpClient; $c.Connect('127.0.0.1', 5455); $c.Close(); exit 0 } catch { exit 1 }"
if %errorlevel% neq 0 (
    echo   X MySQL 未在 5455 端口监听，请先启动 Docker MySQL 或本地 MySQL
    set ERR=1
) else (
    echo   OK MySQL 5455 可连接
)

echo.
echo [2] 端口占用检查（若已运行可忽略）...
for %%P in (8761 8080 8081 8082 8083 8084 8085 9094) do (
    netstat -ano | findstr ":%%P " | findstr "LISTENING" >nul 2>&1
    if not errorlevel 1 echo   ! 端口 %%P 已被占用（可能已有实例在运行）
)

echo.
if %ERR% equ 1 (
    echo 请先解决上述问题，或运行 init-databases.sql 初始化数据库
    echo 数据库脚本: smart-home-microservices\init-databases.sql
    pause
    exit /b 1
)

echo 检查通过，可以启动微服务。
echo 提示: 若 edge-service 报 gRPC 端口冲突，请关闭占用 9094 的旧进程。
echo.
exit /b 0

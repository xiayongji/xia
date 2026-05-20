@echo off
chcp 65001 >nul
REM ========================================
REM 快速启动脚本 - 简化版
REM ========================================

echo.
echo ════════════════════════════════════════════════════════
echo   智能家居系统 - 快速启动
echo ════════════════════════════════════════════════════════
echo.

echo [1/6] 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo   [错误] 未检测到Java，请先安装JDK 17+
    pause
    exit /b 1
)
echo   ✓ Java环境正常

echo [2/6] 检查Maven...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo   [错误] 未检测到Maven，请先安装Maven
    pause
    exit /b 1
)
echo   ✓ Maven环境正常

echo [3/6] 启动Docker服务...
docker --version >nul 2>&1
if %errorlevel% equ 0 (
    docker start smart-home-mysql >nul 2>&1
    docker start smart-home-redis >nul 2>&1
    echo   ✓ Docker服务已启动
) else (
    echo   [跳过] Docker不可用，假设本地MySQL已运行
)

echo [4/6] 初始化数据库...
cd /d "%~dp0"
if exist "smart-home-microservices\edge-service\sql\init-edge-database.sql" (
    mysql -u root -p"infini_rag_flow" -e "CREATE DATABASE IF NOT EXISTS smarthome_edge;" 2>nul
    mysql -u root -p"infini_rag_flow" -D smarthome_edge -e "source smart-home-microservices\edge-service\sql\init-edge-database.sql" 2>nul
    mysql -u root -p"infini_rag_flow" -D smarthome_edge -e "source smart-home-microservices\edge-service\sql\insert-all-test-devices.sql" 2>nul
    echo   ✓ 数据库初始化完成
) else (
    echo   [跳过] 数据库脚本不存在
)

echo [5/6] 启动Eureka Server...
start "Eureka-Server" cmd /k "cd /d %~dp0smart-home-microservices\eureka-server && mvn spring-boot:run"
echo   ✓ Eureka Server 启动中（请等待30秒）...
timeout /t 30 /nobreak >nul

echo [6/6] 启动微服务...
start "Device-Service" cmd /k "cd /d %~dp0smart-home-microservices\device-service && mvn spring-boot:run"
start "User-Service" cmd /k "cd /d %~dp0smart-home-microservices\user-service && mvn spring-boot:run"
start "Edge-Service" cmd /k "cd /d %~dp0smart-home-microservices\edge-service && mvn spring-boot:run"

echo   ✓ 微服务启动中（请等待60秒）...
timeout /t 60 /nobreak >nul

echo [启动] API Gateway...
start "API-Gateway" cmd /k "cd /d %~dp0smart-home-microservices\api-gateway && mvn spring-boot:run"
echo   ✓ API Gateway 启动中（请等待30秒）...
timeout /t 30 /nobreak >nul

echo [启动] 前端应用...
if exist "package.json" (
    start "Frontend" cmd /k "cd /d %~dp0 && npm run dev"
) else (
    start "Frontend" cmd /k "cd /d %~dp0dist && python -m http.server 5173"
)
echo   ✓ 前端启动中...

echo.
echo ════════════════════════════════════════════════════════
echo   启动完成！
echo ════════════════════════════════════════════════════════
echo.
echo 服务地址：
echo   - Eureka:      http://localhost:8761
echo   - API Gateway: http://localhost:8080
echo   - 前端:        http://localhost:5173
echo   - 边缘服务:    http://localhost:8084
echo.
echo 提示：请检查各个终端窗口，确保所有服务都已正常启动。
echo.
pause

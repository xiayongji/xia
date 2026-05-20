@echo off
chcp 65001 >nul
echo ========================================
echo    智能家居微服务系统 - 一键启动
echo ========================================
echo.

cd /d "%~dp0smart-home-microservices"
call preflight-check.bat
if %errorlevel% neq 0 exit /b 1

echo [1/7] 启动 Eureka Server...
start "Eureka-Server" cmd /k "cd eureka-server && mvn spring-boot:run"
echo      等待 15 秒...
timeout /t 15 /nobreak >nul

echo [2/7] 启动 API Gateway...
start "API-Gateway" cmd /k "cd api-gateway && mvn spring-boot:run"
echo      等待 15 秒...
timeout /t 15 /nobreak >nul

echo [3/7] 启动 User Service...
start "User-Service" cmd /k "cd user-service && mvn spring-boot:run"
echo      等待 15 秒...
timeout /t 15 /nobreak >nul

echo [4/7] 启动 Device Service...
start "Device-Service" cmd /k "cd device-service && mvn spring-boot:run"
echo      等待 15 秒...
timeout /t 15 /nobreak >nul

echo [5/7] 启动 Scene Service...
start "Scene-Service" cmd /k "cd scene-service && mvn spring-boot:run"
echo      等待 15 秒...
timeout /t 15 /nobreak >nul

echo [6/7] 启动 Edge Service...
start "Edge-Service" cmd /k "cd edge-service && mvn spring-boot:run"
echo      等待 15 秒...
timeout /t 15 /nobreak >nul

echo [7/7] 启动 Analytics Service...
start "Analytics-Service" cmd /k "cd analytics-service && mvn spring-boot:run"

echo.
echo ========================================
echo    所有服务正在启动！
echo ========================================
echo.
echo 服务地址：
echo   - Eureka Dashboard: http://localhost:8761
echo   - API Gateway:      http://localhost:8080
echo   - User Service:     http://localhost:8082
echo   - Device Service:   http://localhost:8081
echo   - Scene Service:    http://localhost:8083
echo   - Edge Service:     http://localhost:8084
echo   - Analytics:        http://localhost:8085
echo.
echo 前端地址：
echo   - Vue Frontend:     http://localhost:3007
echo.
echo 请等待约 2 分钟让所有服务完全启动
echo ========================================
pause

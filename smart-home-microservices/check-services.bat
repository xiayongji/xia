@echo off
chcp 65001 >nul
REM ========================================
REM 检查所有服务健康状态
REM ========================================

echo.
echo ════════════════════════════════════════════════════════
echo   智能家居系统 - 服务健康检查
echo ════════════════════════════════════════════════════════
echo.

set ALL_OK=1

REM 检查Eureka
echo [1/7] 检查 Eureka Server (8761)...
curl -s http://localhost:8761 >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✓ Eureka Server 运行正常
) else (
    echo   ✗ Eureka Server 未启动
    set ALL_OK=0
)

REM 检查Device Service
echo [2/7] 检查 Device Service (8081)...
curl -s http://localhost:8081/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✓ Device Service 运行正常
) else (
    echo   ✗ Device Service 未启动
    set ALL_OK=0
)

REM 检查User Service
echo [3/7] 检查 User Service (8082)...
curl -s http://localhost:8082/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✓ User Service 运行正常
) else (
    echo   ✗ User Service 未启动
    set ALL_OK=0
)

REM 检查Scene Service
echo [4/7] 检查 Scene Service (8083)...
curl -s http://localhost:8083/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✓ Scene Service 运行正常
) else (
    echo   ✗ Scene Service 未启动
    set ALL_OK=0
)

REM 检查Edge Service
echo [5/7] 检查 Edge Service (8084)...
curl -s http://localhost:8084/api/edge/health >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✓ Edge Service 运行正常
) else (
    echo   ✗ Edge Service 未启动
    set ALL_OK=0
)

REM 检查API Gateway
echo [6/7] 检查 API Gateway (8080)...
curl -s http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✓ API Gateway 运行正常
) else (
    echo   ✗ API Gateway 未启动
    set ALL_OK=0
)

REM 检查前端
echo [7/7] 检查 Frontend (5173)...
curl -s http://localhost:5173 >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✓ Frontend 运行正常
) else (
    echo   ✗ Frontend 未启动
    set ALL_OK=0
)

echo.
echo ════════════════════════════════════════════════════════

if %ALL_OK% equ 1 (
    echo   ✓ 所有服务运行正常！
) else (
    echo   ✗ 部分服务未启动，请检查！
)

echo ════════════════════════════════════════════════════════
echo.
echo 详细检查：
echo.

REM 详细检查边缘服务
echo [边缘服务状态]
curl -s http://localhost:8084/api/edge/health 2>nul || echo 未启动
echo.

REM 详细检查设备列表
echo [设备列表]
curl -s http://localhost:8084/api/edge/devices 2>nul || echo 未启动
echo.

REM 详细检查设备状态
echo [设备状态]
curl -s http://localhost:8084/api/edge/devices/status/all 2>nul || echo 未启动
echo.

echo.
pause

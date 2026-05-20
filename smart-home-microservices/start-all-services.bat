# ========================================
# 智能家居云边协同系统 - 一键启动脚本
# ========================================

@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ════════════════════════════════════════════════════════
echo   智能家居云边协同系统 - 启动管理器
echo ════════════════════════════════════════════════════════
echo.
echo 正在检测环境...
echo.

REM 检测Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Java环境，请先安装JDK 17+
    pause
    exit /b 1
)

REM 检测Maven
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Maven，请先安装Maven
    pause
    exit /b 1
)

REM 检测Docker
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [警告] 未检测到Docker，部分服务可能无法启动
) else (
    echo [✓] 检测到Docker
)

echo [✓] Java环境正常
echo [✓] Maven环境正常
echo.

REM 设置工作目录
set PROJECT_ROOT=%~dp0
cd /d "%PROJECT_ROOT%"

echo ════════════════════════════════════════════════════════
echo   启动选项菜单
echo ════════════════════════════════════════════════════════
echo.
echo   1. 启动所有服务（完整启动）
echo   2. 仅启动基础设施（MySQL + Redis）
echo   3. 仅启动微服务后端
echo   4. 仅启动前端应用
echo   5. 停止所有服务
echo   6. 重启所有服务
echo   7. 检查服务状态
echo   8. 查看服务日志
echo   9. 初始化数据库
echo   0. 退出
echo.
echo ════════════════════════════════════════════════════════
echo.

set /p choice=请输入选项 (0-9):

if "%choice%"=="1" goto ALL_SERVICES
if "%choice%"=="2" goto INFRASTRUCTURE
if "%choice%"=="3" goto MICROSERVICES
if "%choice%"=="4" goto FRONTEND_ONLY
if "%choice%"=="5" goto STOP_ALL
if "%choice%"=="6" goto RESTART_ALL
if "%choice%"=="7" goto CHECK_STATUS
if "%choice%"=="8" goto VIEW_LOGS
if "%choice%"=="9" goto INIT_DATABASE
if "%choice%"=="0" goto EXIT

echo [错误] 无效选项，请重新选择
goto END

:ALL_SERVICES
echo.
echo ════════════════════════════════════════════════════════
echo   启动所有服务
echo ════════════════════════════════════════════════════════
echo.

call :START_INFRASTRUCTURE
call :START_EUREKA
call :START_MICROSERVICES
call :START_API_GATEWAY
call :START_FRONTEND

echo.
echo ════════════════════════════════════════════════════════
echo   所有服务启动完成！
echo ════════════════════════════════════════════════════════
echo.
echo 服务访问地址：
echo   - Eureka Dashboard: http://localhost:8761
echo   - API Gateway:      http://localhost:8080
echo   - 前端应用:         http://localhost:5173
echo   - 边缘服务:         http://localhost:8084
echo.
goto END

:INFRASTRUCTURE
echo.
echo ════════════════════════════════════════════════════════
echo   启动基础设施服务
echo ════════════════════════════════════════════════════════
echo.
call :START_INFRASTRUCTURE
goto END

:MICROSERVICES
echo.
echo ════════════════════════════════════════════════════════
echo   启动微服务
echo ════════════════════════════════════════════════════════
echo.
call :START_EUREKA
call :START_MICROSERVICES
call :START_API_GATEWAY
goto END

:FRONTEND_ONLY
echo.
echo ════════════════════════════════════════════════════════
echo   启动前端应用
echo ════════════════════════════════════════════════════════
echo.
call :START_FRONTEND
goto END

:STOP_ALL
echo.
echo ════════════════════════════════════════════════════════
echo   停止所有服务
echo ════════════════════════════════════════════════════════
echo.

echo [1/6] 停止Docker容器...
docker-compose -f docker-compose.yml down >nul 2>&1

echo [2/6] 停止前端服务...
taskkill /F /IM node.exe >nul 2>&1

echo [3/6] 停止所有Java服务...
taskkill /F /IM java.exe >nul 2>&1

echo [4/6] 停止MySQL服务...
net stop MySQL >nul 2>&1

echo [5/6] 停止Redis服务...
net stop Redis >nul 2>&1

echo [6/6] 清理完成
echo.
echo ✓ 所有服务已停止
goto END

:RESTART_ALL
echo.
echo 正在重启所有服务...
call :STOP_ALL
timeout /t 3 /nobreak >nul
call :ALL_SERVICES
goto END

:CHECK_STATUS
echo.
echo ════════════════════════════════════════════════════════
echo   检查服务状态
echo ════════════════════════════════════════════════════════
echo.

REM 检查Java服务
echo [检查] Java进程...
tasklist | findstr /I "java.exe" >nul
if %errorlevel% equ 0 (
    echo   ✓ 检测到Java服务运行中
) else (
    echo   ✗ 未检测到Java服务
)

REM 检查Node服务
echo [检查] Node进程...
tasklist | findstr /I "node.exe" >nul
if %errorlevel% equ 0 (
    echo   ✓ 检测到Node服务运行中
) else (
    echo   ✗ 未检测到Node服务
)

REM 检查Docker容器
echo [检查] Docker容器...
docker ps --format "table {{.Names}}\t{{.Status}}" 2>nul
if %errorlevel% neq 0 (
    echo   [警告] Docker未运行或无法访问
)

REM 检查端口
echo.
echo [检查] 服务端口...

netstat -an | findstr ":8761 " | findstr "LISTENING" >nul
if %errorlevel% equ 0 (
    echo   ✓ Eureka Server (8761) - 运行中
) else (
    echo   ✗ Eureka Server (8761) - 未启动
)

netstat -an | findstr ":8080 " | findstr "LISTENING" >nul
if %errorlevel% equ 0 (
    echo   ✓ API Gateway (8080) - 运行中
) else (
    echo   ✗ API Gateway (8080) - 未启动
)

netstat -an | findstr ":8084 " | findstr "LISTENING" >nul
if %errorlevel% equ 0 (
    echo   ✓ Edge Service (8084) - 运行中
) else (
    echo   ✗ Edge Service (8084) - 未启动
)

netstat -an | findstr ":5173 " | findstr "LISTENING" >nul
if %errorlevel% equ 0 (
    echo   ✓ Frontend (5173) - 运行中
) else (
    echo   ✗ Frontend (5173) - 未启动
)

goto END

:VIEW_LOGS
echo.
echo ════════════════════════════════════════════════════════
echo   查看服务日志
echo ════════════════════════════════════════════════════════
echo.
echo 请查看以下日志文件：
echo.
echo   - Eureka:         smart-home-microservices\eureka-server\logs\eureka.log
echo   - API Gateway:    smart-home-microservices\api-gateway\logs\gateway.log
echo   - Device Service:  smart-home-microservices\device-service\logs\device.log
echo   - User Service:    smart-home-microservices\user-service\logs\user.log
echo   - Edge Service:    smart-home-microservices\edge-service\logs\edge.log
echo   - Analytics:      smart-home-microservices\analytics-service\logs\analytics.log
echo.
echo 是否打开日志目录？(Y/N)
set /p open_logs=
if /i "%open_logs%"=="Y" (
    start explorer "smart-home-microservices"
)
goto END

:INIT_DATABASE
echo.
echo ════════════════════════════════════════════════════════
echo   初始化数据库
echo ════════════════════════════════════════════════════════
echo.

REM 检查MySQL
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到MySQL客户端
    echo 请确保MySQL已安装并添加到PATH
    pause
    goto END
)

echo [1/3] 创建数据库...
mysql -u root -p"infini_rag_flow" -e "CREATE DATABASE IF NOT EXISTS smarthome_user CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
mysql -u root -p"infini_rag_flow" -e "CREATE DATABASE IF NOT EXISTS smarthome_device CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
echo   ✓ 数据库创建完成

echo [2/3] 初始化边缘服务数据库...
if exist "smart-home-microservices\edge-service\sql\init-edge-database.sql" (
    mysql -u root -p"infini_rag_flow" -D smarthome_edge -e "source smart-home-microservices\edge-service\sql\init-edge-database.sql"
    echo   ✓ 边缘服务数据库初始化完成
) else (
    echo   [跳过] 边缘服务数据库脚本不存在
)

echo [3/3] 插入测试数据...
if exist "smart-home-microservices\edge-service\sql\insert-all-test-devices.sql" (
    mysql -u root -p"infini_rag_flow" -D smarthome_edge -e "source smart-home-microservices\edge-service\sql\insert-all-test-devices.sql"
    echo   ✓ 测试数据插入完成
) else (
    echo   [跳过] 测试数据脚本不存在
)

echo.
echo ✓ 数据库初始化完成！
goto END

:EXIT
echo.
echo 感谢使用，再见！
echo.
exit /b 0

:END
echo.
pause

REM ========================================
REM 子函数：启动基础设施
REM ========================================
:START_INFRASTRUCTURE
echo [1/2] 启动MySQL服务...

REM 检查Docker是否运行
docker info >nul 2>&1
if %errorlevel% equ 0 (
    echo 使用Docker启动MySQL...
    docker run -d --name smart-home-mysql ^
        -e MYSQL_ROOT_PASSWORD=infini_rag_flow ^
        -e MYSQL_DATABASE=smarthome ^
        -p 5455:3306 ^
        mysql:8.0
) else (
    echo [跳过] Docker未运行，假设MySQL已在本地运行
)

echo [2/2] 启动Redis服务...
docker run -d --name smart-home-redis -p 6379:6379 redis:7.0
if %errorlevel% equ 0 (
    echo   ✓ Redis启动成功
) else (
    echo   [跳过] Redis已在运行或Docker不可用
)

echo ✓ 基础设施启动完成
echo.
exit /b 0

REM ========================================
REM 子函数：启动Eureka
REM ========================================
:START_EUREKA
echo [启动] Eureka Server (8761)...
cd /d "%PROJECT_ROOT%smart-home-microservices\eureka-server"
start "Eureka-Server" cmd /c "mvn spring-boot:run"
cd /d "%PROJECT_ROOT%"
echo   ✓ Eureka Server 启动命令已执行
echo   请等待约30秒使服务完全启动...
timeout /t 30 /nobreak >nul
exit /b 0

REM ========================================
REM 子函数：启动微服务
REM ========================================
:START_MICROSERVICES
echo [启动] Device Service (8081)...
cd /d "%PROJECT_ROOT%smart-home-microservices\device-service"
start "Device-Service" cmd /c "mvn spring-boot:run"
cd /d "%PROJECT_ROOT%"

echo [启动] User Service (8082)...
cd /d "%PROJECT_ROOT%smart-home-microservices\user-service"
start "User-Service" cmd /c "mvn spring-boot:run"
cd /d "%PROJECT_ROOT%"

echo [启动] Scene Service (8083)...
cd /d "%PROJECT_ROOT%smart-home-microservices\scene-service"
start "Scene-Service" cmd /c "mvn spring-boot:run"
cd /d "%PROJECT_ROOT%"

echo [启动] Edge Service (8084)...
cd /d "%PROJECT_ROOT%smart-home-microservices\edge-service"
start "Edge-Service" cmd /c "mvn spring-boot:run"
cd /d "%PROJECT_ROOT%"

echo [启动] Analytics Service (8085)...
cd /d "%PROJECT_ROOT%smart-home-microservices\analytics-service"
start "Analytics-Service" cmd /c "mvn spring-boot:run"
cd /d "%PROJECT_ROOT%"

echo   ✓ 所有微服务启动命令已执行
echo   请等待约60秒使所有服务完全启动...
timeout /t 60 /nobreak >nul
exit /b 0

REM ========================================
REM 子函数：启动API网关
REM ========================================
:START_API_GATEWAY
echo [启动] API Gateway (8080)...
cd /d "%PROJECT_ROOT%smart-home-microservices\api-gateway"
start "API-Gateway" cmd /c "mvn spring-boot:run"
cd /d "%PROJECT_ROOT%"
echo   ✓ API Gateway 启动命令已执行
echo   请等待约30秒使服务完全启动...
timeout /t 30 /nobreak >nul
exit /b 0

REM ========================================
REM 子函数：启动前端
REM ========================================
:START_FRONTEND
echo [启动] 前端应用...

REM 检查是否在项目根目录
if exist "package.json" (
    start "Frontend" cmd /c "npm run dev"
) else if exist "dist\index.html" (
    echo   启动静态服务器...
    cd /d "%PROJECT_ROOT%"
    start "Frontend" cmd /c "python -m http.server 5173"
) else (
    echo   [警告] 未找到前端代码
)

echo   ✓ 前端启动命令已执行
echo   请等待约20秒使服务完全启动...
timeout /t 20 /nobreak >nul
exit /b 0

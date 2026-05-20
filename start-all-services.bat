@echo off
chcp 65001 >nul
echo.
echo ==============================================
echo      智能家居系统 - 一键启动脚本
echo ==============================================
echo.
echo 正在启动所有服务，请稍候...
echo.

:: 创建日志目录
if not exist logs mkdir logs

:: 启动 API网关 (端口 8080)
echo [1/6] 启动 API网关 (端口 8080)...
start "API网关" cmd /k "cd /d c:\Users\user\Desktop\毕业设计\smart-home-microservices\api-gateway && mvn spring-boot:run"
timeout /t 8 /nobreak >nul

:: 启动 用户服务 (端口 8082)
echo [2/6] 启动 用户服务 (端口 8082)...
start "用户服务" cmd /k "cd /d c:\Users\user\Desktop\毕业设计\smart-home-microservices\user-service && mvn spring-boot:run"
timeout /t 8 /nobreak >nul

:: 启动 设备服务 (端口 8081)
echo [3/6] 启动 设备服务 (端口 8081)...
start "设备服务" cmd /k "cd /d c:\Users\user\Desktop\毕业设计\smart-home-microservices\device-service && mvn spring-boot:run"
timeout /t 8 /nobreak >nul

:: 启动 场景服务 (端口 8083)
echo [4/6] 启动 场景服务 (端口 8083)...
start "场景服务" cmd /k "cd /d c:\Users\user\Desktop\毕业设计\smart-home-microservices\scene-service && mvn spring-boot:run"
timeout /t 8 /nobreak >nul

:: 启动 数据分析服务 (端口 8085)
echo [5/6] 启动 数据分析服务 (端口 8085)...
start "数据分析服务" cmd /k "cd /d c:\Users\user\Desktop\毕业设计\smart-home-microservices\analytics-service && mvn spring-boot:run"
timeout /t 8 /nobreak >nul

:: 启动 前端界面 (端口 5173)
echo [6/6] 启动 前端界面 (端口 5173)...
start "前端界面" cmd /k "cd /d c:\Users\user\Desktop\毕业设计 && npm run dev"

echo.
echo ==============================================
echo           所有服务启动命令已发出
echo ==============================================
echo.
echo 服务访问地址:
echo   前端界面:      http://localhost:5173
echo   API网关:       http://localhost:8080
echo   用户服务:      http://localhost:8082
echo   设备服务:      http://localhost:8081
echo   场景服务:      http://localhost:8083
echo   数据分析服务:  http://localhost:8085
echo.
echo 请等待各服务启动完成（约30-60秒），然后访问前端界面。
echo 按任意键关闭此窗口...
pause >nul
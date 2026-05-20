@echo off
chcp 65001 >nul
REM ========================================
REM 一键启动缺失服务脚本
REM ========================================

echo.
echo ════════════════════════════════════════════════════════
echo   启动缺失的服务
echo ════════════════════════════════════════════════════════
echo.

cd /d "%~dp0"

echo [1/4] 创建日志目录...
if not exist "edge-service\logs" mkdir edge-service\logs
echo   ✓ 完成

echo.
echo [2/4] 启动Edge Service (8084)...
echo   提示：这将打开一个新窗口
start "Edge-Service" cmd /k "cd /d %~dp0edge-service && mvn spring-boot:run"
echo   ✓ Edge Service启动命令已执行
echo   等待30秒让服务启动...
timeout /t 30 /nobreak >nul

echo.
echo [3/4] 启动Frontend (5173)...
if exist "package.json" (
    echo   使用npm启动前端...
    start "Frontend" cmd /k "cd /d %~dp0 && npm run dev"
) else if exist "dist\index.html" (
    echo   使用静态服务器启动前端...
    start "Frontend" cmd /k "cd /d %~dp0dist && python -m http.server 5173"
) else (
    echo   [警告] 未找到前端代码
)
echo   ✓ Frontend启动命令已执行
echo   等待10秒让服务启动...
timeout /t 10 /nobreak >nul

echo.
echo [4/4] 验证服务启动...
echo.

REM 测试Edge Service
curl -s http://localhost:8084/api/edge/health >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✓ Edge Service (8084) 启动成功
) else (
    echo   ✗ Edge Service (8084) 尚未完全启动，请稍后测试
)

REM 测试Frontend
curl -s http://localhost:5173 >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✓ Frontend (5173) 启动成功
) else (
    echo   ✗ Frontend (5173) 尚未完全启动，请稍后测试
)

echo.
echo ════════════════════════════════════════════════════════
echo   启动命令已执行！
echo ════════════════════════════════════════════════════════
echo.
echo 请查看新打开的终端窗口，确保服务已启动。
echo.
echo 访问地址：
echo   - Edge Service: http://localhost:8084
echo   - Frontend:     http://localhost:5173
echo.
echo 提示：
echo   1. 如果服务启动失败，请查看终端窗口的错误信息
echo   2. 确保MySQL和Redis已启动
echo   3. 如果端口被占用，请先停止占用端口的进程
echo.
pause

@echo off
REM 快速插入测试设备到数据库
REM 解决前端设备状态显示offline问题

set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASSWORD=infini_rag_flow
set MYSQL_DATABASE=smarthome_edge

echo ===================================
echo 插入测试设备数据
echo ===================================
echo.

REM 切换到SQL脚本目录
cd /d "%~dp0..\sql"

REM 执行SQL脚本
echo [1/3] 初始化数据库...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% < init-edge-database.sql
if %errorlevel% equ 0 (
    echo   ✓ 数据库初始化成功
) else (
    echo   ✗ 数据库初始化失败
    exit /b 1
)

echo.
echo [2/3] 插入测试设备数据...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% -D %MYSQL_DATABASE% < insert-all-test-devices.sql
if %errorlevel% equ 0 (
    echo   ✓ 测试设备数据插入成功
) else (
    echo   ✗ 测试设备数据插入失败
    exit /b 1
)

echo.
echo [3/3] 验证数据...
echo.
echo --- 设备列表 ---
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% -D %MYSQL_DATABASE% -e "SELECT device_id, device_name, status FROM edge_device;"
echo.
echo --- 设备状态 ---
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% -D %MYSQL_DATABASE% -e "SELECT device_id, status, power, temperature, humidity FROM edge_device_status;"
echo.

echo ===================================
echo 设备数据插入完成！
echo ===================================
echo.
echo 现在可以测试API：
echo   curl http://localhost:8084/api/edge/devices/status/all
echo.
pause

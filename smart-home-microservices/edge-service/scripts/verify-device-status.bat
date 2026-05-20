@echo off
REM 验证设备状态API
REM 检查设备是否正确显示为online

set EDGE_API=http://localhost:8084/api/edge

echo ===================================
echo 验证设备状态API
echo ===================================
echo.

echo [1/5] 测试健康检查...
curl -s -X GET "%EDGE_API%/health" | findstr /C:"UP"
if %errorlevel% equ 0 (
    echo   ✓ 服务正常
) else (
    echo   ✗ 服务未启动或不可用
)
echo.

echo [2/5] 获取所有设备...
curl -s -X GET "%EDGE_API%/devices"
echo.
echo.

echo [3/5] 获取所有设备状态...
curl -s -X GET "%EDGE_API%/devices/status/all"
echo.
echo.

echo [4/5] 获取设备统计...
curl -s -X GET "%EDGE_API%/devices/statistics"
echo.
echo.

echo [5/5] 获取单个设备状态 (device-1779073453825-9716)...
curl -s -X GET "%EDGE_API%/devices/device-1779073453825-9716/status"
echo.
echo.

echo ===================================
echo 测试完成！
echo ===================================
echo.
echo 如果设备状态都是"online"，说明数据库配置正确。
echo 如果还是"offline"，请检查：
echo   1. MySQL服务是否运行
echo   2. 是否已执行insert-test-devices.bat脚本
echo   3. 数据库连接配置是否正确
echo.
pause

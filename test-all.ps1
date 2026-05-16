Write-Host "`n=== 智能家居系统全面功能测试 ===" -ForegroundColor Cyan

# 用户服务测试
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "1. 用户服务测试" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

Write-Host "`n1.1 注册用户:" -ForegroundColor White
try {
    $register = Invoke-WebRequest -Uri "http://localhost:8080/api/user/auth/register?roleName=ADMIN" -Method POST -Body '{"username":"admin","password":"Admin@123","email":"admin@example.com","fullName":"管理员"}' -ContentType "application/json" -UseBasicParsing
    Write-Host "状态: " $register.StatusCode -ForegroundColor Green
} catch {
    Write-Host "状态: 失败 - $_" -ForegroundColor Red
}

Write-Host "`n1.2 用户登录:" -ForegroundColor White
try {
    $login = Invoke-WebRequest -Uri http://localhost:8080/api/user/auth/login -Method POST -Body '{"username":"admin","password":"Admin@123"}' -ContentType "application/json" -UseBasicParsing
    Write-Host "状态: " $login.StatusCode -ForegroundColor Green
    $token = ($login.Content | ConvertFrom-Json).accessToken
    if ($token) {
        Write-Host "Token获取: 成功" -ForegroundColor Green
    } else {
        Write-Host "Token获取: 失败" -ForegroundColor Red
    }
} catch {
    Write-Host "状态: 失败 - $_" -ForegroundColor Red
}

Write-Host "`n1.3 获取用户信息:" -ForegroundColor White
try {
    $userInfo = Invoke-WebRequest -Uri http://localhost:8080/api/user/profile -Headers @{Authorization="Bearer $token"} -UseBasicParsing
    Write-Host "状态: " $userInfo.StatusCode -ForegroundColor Green
} catch {
    Write-Host "状态: 失败 - $_" -ForegroundColor Red
}

# 设备服务测试
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "2. 设备服务测试" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

Write-Host "`n2.1 获取设备列表:" -ForegroundColor White
try {
    $devices = Invoke-WebRequest -Uri http://localhost:8080/api/devices -Headers @{Authorization="Bearer $token"} -UseBasicParsing
    Write-Host "状态: " $devices.StatusCode -ForegroundColor Green
    $deviceList = $devices.Content | ConvertFrom-Json
    Write-Host "设备数量: " $deviceList.Count -ForegroundColor White
} catch {
    Write-Host "状态: 失败 - $_" -ForegroundColor Red
}

Write-Host "`n2.2 添加设备:" -ForegroundColor White
try {
    $addDevice = Invoke-WebRequest -Uri http://localhost:8080/api/devices -Method POST -Body '{"name":"测试灯","type":"照明","protocol":"wifi"}' -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -UseBasicParsing
    Write-Host "状态: " $addDevice.StatusCode -ForegroundColor Green
    $newDevice = $addDevice.Content | ConvertFrom-Json
    $deviceId = $newDevice.id
    Write-Host "设备ID: " $deviceId -ForegroundColor White
} catch {
    Write-Host "状态: 失败 - $_" -ForegroundColor Red
}

Write-Host "`n2.3 删除设备:" -ForegroundColor White
try {
    $deleteDevice = Invoke-WebRequest -Uri "http://localhost:8080/api/devices/$deviceId" -Method DELETE -Headers @{Authorization="Bearer $token"} -UseBasicParsing
    Write-Host "状态: " $deleteDevice.StatusCode -ForegroundColor Green
} catch {
    Write-Host "状态: 失败 - $_" -ForegroundColor Red
}

# 场景服务测试
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "3. 场景服务测试" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

Write-Host "`n3.1 获取场景列表:" -ForegroundColor White
try {
    $scenes = Invoke-WebRequest -Uri http://localhost:8080/api/scenes -Headers @{Authorization="Bearer $token"} -UseBasicParsing
    Write-Host "状态: " $scenes.StatusCode -ForegroundColor Green
    $sceneList = $scenes.Content | ConvertFrom-Json
    Write-Host "场景数量: " $sceneList.Count -ForegroundColor White
} catch {
    Write-Host "状态: 失败 - $_" -ForegroundColor Red
}

# 数据分析服务测试
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "4. 数据分析服务测试" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

Write-Host "`n4.1 获取能耗数据:" -ForegroundColor White
try {
    $energy = Invoke-WebRequest -Uri http://localhost:8080/api/analytics/energy/today -Headers @{Authorization="Bearer $token"} -UseBasicParsing
    Write-Host "状态: " $energy.StatusCode -ForegroundColor Green
} catch {
    Write-Host "状态: 失败 - $_" -ForegroundColor Red
}

# 前端测试
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "5. 前端界面测试" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

Write-Host "`n5.1 访问前端首页:" -ForegroundColor White
try {
    $frontend = Invoke-WebRequest -Uri http://localhost:3000 -UseBasicParsing
    Write-Host "状态: " $frontend.StatusCode -ForegroundColor Green
} catch {
    Write-Host "状态: 失败 - $_" -ForegroundColor Red
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Cyan
Write-Host "`n服务状态汇总:" -ForegroundColor White
Write-Host "API网关:     http://localhost:8080" -ForegroundColor Green
Write-Host "用户服务:    http://localhost:8083" -ForegroundColor Green
Write-Host "设备服务:    http://localhost:8081" -ForegroundColor Green
Write-Host "场景服务:    http://localhost:8082" -ForegroundColor Green
Write-Host "数据分析:    http://localhost:8084" -ForegroundColor Green
Write-Host "前端界面:    http://localhost:3000" -ForegroundColor Green
param(
    [string]$ProjectPath = "c:\Users\user\Desktop\毕业设计"
)

Write-Host "`n==============================================" -ForegroundColor Cyan
Write-Host "      智能家居系统 - 一键启动脚本" -ForegroundColor Cyan
Write-Host "==============================================`n" -ForegroundColor Cyan

# 创建日志目录
if (-not (Test-Path "logs")) {
    New-Item -ItemType Directory -Path "logs" | Out-Null
}

# 初始化数据库
Write-Host "[准备] 初始化MySQL数据库..." -ForegroundColor Yellow
try {
    mysql -u root -ppassword -e "source $ProjectPath\smart-home-microservices\init-databases.sql"
    Write-Host "数据库初始化成功" -ForegroundColor Green
} catch {
    Write-Host "数据库初始化失败或已存在，继续启动服务..." -ForegroundColor Yellow
}

# 启动API网关
Write-Host "[1/6] 启动 API网关 (端口 8080)..." -ForegroundColor Yellow
$apiGatewayCmd = "cd /d `"$ProjectPath\smart-home-microservices\api-gateway`" && mvn spring-boot:run"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $apiGatewayCmd -WindowStyle Normal -PassThru
Start-Sleep -Seconds 12

# 启动用户服务
Write-Host "[2/6] 启动 用户服务 (端口 8083)..." -ForegroundColor Yellow
$userServiceCmd = "cd /d `"$ProjectPath\smart-home-microservices\user-service`" && mvn spring-boot:run"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $userServiceCmd -WindowStyle Normal -PassThru
Start-Sleep -Seconds 12

# 启动设备服务
Write-Host "[3/6] 启动 设备服务 (端口 8081)..." -ForegroundColor Yellow
$deviceServiceCmd = "cd /d `"$ProjectPath\smart-home-microservices\device-service`" && mvn spring-boot:run"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $deviceServiceCmd -WindowStyle Normal -PassThru
Start-Sleep -Seconds 12

# 启动场景服务
Write-Host "[4/6] 启动 场景服务 (端口 8082)..." -ForegroundColor Yellow
$sceneServiceCmd = "cd /d `"$ProjectPath\smart-home-microservices\scene-service`" && mvn spring-boot:run"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $sceneServiceCmd -WindowStyle Normal -PassThru
Start-Sleep -Seconds 12

# 启动数据分析服务
Write-Host "[5/6] 启动 数据分析服务 (端口 8084)..." -ForegroundColor Yellow
$analyticsServiceCmd = "cd /d `"$ProjectPath\smart-home-microservices\analytics-service`" && mvn spring-boot:run"
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $analyticsServiceCmd -WindowStyle Normal -PassThru
Start-Sleep -Seconds 12

# 启动前端
Write-Host "[6/6] 启动 前端界面 (端口 3000)..." -ForegroundColor Yellow
Start-Process -FilePath "powershell.exe" -ArgumentList "-ExecutionPolicy", "Bypass", "-File", "`"$ProjectPath\start-frontend.ps1`"" -WindowStyle Normal -PassThru

Write-Host "`n==============================================" -ForegroundColor Green
Write-Host "           所有服务启动命令已发出" -ForegroundColor Green
Write-Host "==============================================`n" -ForegroundColor Green

Write-Host "服务访问地址:" -ForegroundColor Cyan
Write-Host "  前端界面:      http://localhost:3000" -ForegroundColor White
Write-Host "  API网关:       http://localhost:8080" -ForegroundColor White
Write-Host "  用户服务:      http://localhost:8083" -ForegroundColor White
Write-Host "  设备服务:      http://localhost:8081" -ForegroundColor White
Write-Host "  场景服务:      http://localhost:8082" -ForegroundColor White
Write-Host "  数据分析服务:  http://localhost:8084" -ForegroundColor White

Write-Host "`n请等待各服务启动完成（约30-60秒），然后访问前端界面。" -ForegroundColor Yellow
Write-Host "按任意键继续..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
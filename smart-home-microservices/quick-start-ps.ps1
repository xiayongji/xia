# ========================================
# PowerShell快速启动脚本
# ========================================

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  智能家居系统 - PowerShell快速启动" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# 检查Java
Write-Host "[检查] Java环境..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "  [✓] Java已安装: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "  [✗] Java未安装或未配置PATH" -ForegroundColor Red
    Write-Host "  请先安装JDK 17+" -ForegroundColor Yellow
    exit 1
}

# 检查Maven
Write-Host "[检查] Maven环境..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1 | Select-Object -First 1
    Write-Host "  [✓] Maven已安装" -ForegroundColor Green
} catch {
    Write-Host "  [✗] Maven未安装" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "正在启动服务..." -ForegroundColor Cyan

# 设置工作目录
$projectRoot = "c:\Users\user\Desktop\毕业设计\smart-home-microservices"

# 启动Edge Service
Write-Host ""
Write-Host "[启动] Edge Service (8084)..." -ForegroundColor Yellow
$edgeServicePath = Join-Path $projectRoot "edge-service"
if (Test-Path $edgeServicePath) {
    Write-Host "  正在启动，请在新窗口中查看..." -ForegroundColor Green
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$edgeServicePath'; Write-Host 'Edge Service启动中，请等待约30秒...' -ForegroundColor Cyan; mvn spring-boot:run"
} else {
    Write-Host "  [✗] Edge Service目录不存在" -ForegroundColor Red
}

Write-Host "  等待服务启动 (30秒)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# 测试Edge Service
Write-Host ""
Write-Host "[测试] Edge Service..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8084/api/edge/health" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "  [✓] Edge Service启动成功！" -ForegroundColor Green
} catch {
    Write-Host "  [!] Edge Service可能还在启动中，请稍后访问" -ForegroundColor Yellow
    Write-Host "  提示：请查看Edge Service窗口查看日志" -ForegroundColor Yellow
}

# 启动Frontend
Write-Host ""
Write-Host "[启动] Frontend (5173)..." -ForegroundColor Yellow
$distPath = Join-Path (Split-Path $projectRoot -Parent) "dist"
$packageJson = Join-Path (Split-Path $projectRoot -Parent) "package.json"

if (Test-Path $distPath) {
    Write-Host "  启动静态服务器..." -ForegroundColor Green
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$distPath'; Write-Host 'Frontend静态服务器已启动，访问 http://localhost:5173' -ForegroundColor Cyan; python -m http.server 5173"
} elseif (Test-Path $packageJson) {
    Write-Host "  使用npm启动..." -ForegroundColor Green
    $frontendRoot = Split-Path $projectRoot -Parent
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$frontendRoot'; Write-Host 'Frontend启动中...' -ForegroundColor Cyan; npm run dev"
} else {
    Write-Host "  [✗] 未找到前端代码" -ForegroundColor Red
}

Write-Host "  等待服务启动 (10秒)..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 测试Frontend
Write-Host ""
Write-Host "[测试] Frontend..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:5173" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "  [✓] Frontend启动成功！" -ForegroundColor Green
} catch {
    Write-Host "  [!] Frontend可能还在启动中" -ForegroundColor Yellow
}

# 总结
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  启动完成！" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "请访问以下地址：" -ForegroundColor Yellow
Write-Host "  - 前端应用: http://localhost:5173" -ForegroundColor Cyan
Write-Host "  - Edge Service: http://localhost:8084" -ForegroundColor Cyan
Write-Host "  - API Gateway: http://localhost:8080" -ForegroundColor Cyan
Write-Host "  - Eureka: http://localhost:8761" -ForegroundColor Cyan
Write-Host ""
Write-Host "提示：" -ForegroundColor Yellow
Write-Host "  1. 请查看新打开的PowerShell窗口" -ForegroundColor Yellow
Write-Host "  2. 如果服务启动失败，窗口中会显示错误信息" -ForegroundColor Yellow
Write-Host "  3. 等待30秒后再尝试访问" -ForegroundColor Yellow
Write-Host ""

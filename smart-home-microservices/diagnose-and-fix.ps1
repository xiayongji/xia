# ========================================
# 智能家居系统 - 诊断和修复脚本
# ========================================

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  智能家居系统 - 诊断和修复工具" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# 检查各个端口
Write-Host "[步骤1] 检查服务端口状态..." -ForegroundColor Yellow
Write-Host ""

$services = @{
    "Eureka Server" = 8761
    "API Gateway" = 8080
    "Device Service" = 8081
    "User Service" = 8082
    "Scene Service" = 8083
    "Edge Service" = 8084
    "Analytics Service" = 8085
    "Frontend" = 5173
}

$missingServices = @()

foreach ($service in $services.GetEnumerator()) {
    $result = Get-NetTCPConnection -LocalPort $service.Value -ErrorAction SilentlyContinue |
              Where-Object { $_.State -eq 'Listen' }

    if ($result) {
        Write-Host "  [✓] $($service.Key) ($($service.Value)) - 已启动" -ForegroundColor Green
    } else {
        Write-Host "  [✗] $($service.Key) ($($service.Value)) - 未启动" -ForegroundColor Red
        $missingServices += $service.Key
    }
}

Write-Host ""

# 检查Docker
Write-Host "[步骤2] 检查Docker服务..." -ForegroundColor Yellow
Write-Host ""

try {
    $dockerResult = docker ps 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [✓] Docker运行正常" -ForegroundColor Green

        # 检查MySQL和Redis容器
        $mysqlContainer = docker ps --filter "name=smart-home-mysql" --format "{{.Names}}" 2>&1
        $redisContainer = docker ps --filter "name=smart-home-redis" --format "{{.Names}}" 2>&1

        if ($mysqlContainer) {
            Write-Host "  [✓] MySQL容器运行中" -ForegroundColor Green
        } else {
            Write-Host "  [✗] MySQL容器未运行" -ForegroundColor Red
        }

        if ($redisContainer) {
            Write-Host "  [✓] Redis容器运行中" -ForegroundColor Green
        } else {
            Write-Host "  [✗] Redis容器未运行" -ForegroundColor Red
        }
    } else {
        Write-Host "  [✗] Docker未运行或不可用" -ForegroundColor Red
        Write-Host "     提示: 请启动Docker Desktop或安装Docker" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  [✗] Docker不可用: $_" -ForegroundColor Red
}

Write-Host ""

# 检查Java
Write-Host "[步骤3] 检查Java进程..." -ForegroundColor Yellow
Write-Host ""

$javaProcesses = Get-Process -Name java -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "  [✓] Java进程数量: $($javaProcesses.Count)" -ForegroundColor Green
} else {
    Write-Host "  [✗] 未检测到Java进程" -ForegroundColor Red
}

Write-Host ""

# 尝试连接测试
Write-Host "[步骤4] 测试API连接..." -ForegroundColor Yellow
Write-Host ""

# 测试API Gateway
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "  [✓] API Gateway响应正常" -ForegroundColor Green
} catch {
    Write-Host "  [✗] API Gateway无响应" -ForegroundColor Red
}

# 测试Edge Service
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8084/api/edge/health" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "  [✓] Edge Service响应正常" -ForegroundColor Green
} catch {
    Write-Host "  [✗] Edge Service无响应" -ForegroundColor Red
}

# 测试前端
try {
    $response = Invoke-WebRequest -Uri "http://localhost:5173" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "  [✓] Frontend响应正常" -ForegroundColor Green
} catch {
    Write-Host "  [✗] Frontend无响应" -ForegroundColor Red
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  诊断完成" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

if ($missingServices.Count -gt 0) {
    Write-Host "未启动的服务: $($missingServices -join ', ')" -ForegroundColor Red
    Write-Host ""
    Write-Host "建议操作：" -ForegroundColor Yellow
    Write-Host "  1. 手动启动缺失的服务" -ForegroundColor Yellow
    Write-Host "  2. 检查Java进程日志" -ForegroundColor Yellow
    Write-Host "  3. 确保Docker已启动" -ForegroundColor Yellow
} else {
    Write-Host "[✓] 所有核心服务已启动" -ForegroundColor Green
}

Write-Host ""

# 尝试自动修复
Write-Host "是否尝试自动修复？(Y/N)" -ForegroundColor Cyan
$response = Read-Host
if ($response -eq "Y" -or $response -eq "y") {
    Write-Host ""
    Write-Host "[修复] 正在尝试修复..." -ForegroundColor Yellow

    # 尝试测试API是否真的可用
    Write-Host "[修复] 测试API Gateway..." -ForegroundColor Yellow
    try {
        $test = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 3 -ErrorAction Stop
        Write-Host "  [✓] API Gateway实际上可用" -ForegroundColor Green
    } catch {
        Write-Host "  [✗] API Gateway确实不可用" -ForegroundColor Red
    }

    Write-Host "[修复] 测试Edge Service..." -ForegroundColor Yellow
    try {
        $test = Invoke-WebRequest -Uri "http://localhost:8084/api/edge/health" -TimeoutSec 3 -ErrorAction Stop
        Write-Host "  [✓] Edge Service实际上可用" -ForegroundColor Green
    } catch {
        Write-Host "  [✗] Edge Service确实不可用，尝试启动..." -ForegroundColor Red

        Write-Host "[修复] 启动Edge Service..." -ForegroundColor Yellow
        $edgePath = "c:\Users\user\Desktop\毕业设计\smart-home-microservices\edge-service"

        if (Test-Path $edgePath) {
            Write-Host "  正在启动Edge Service，请等待..." -ForegroundColor Yellow
            Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$edgePath'; Write-Host 'Edge Service启动中...'; mvn spring-boot:run"

            Start-Sleep -Seconds 10

            # 再次测试
            try {
                $test = Invoke-WebRequest -Uri "http://localhost:8084/api/edge/health" -TimeoutSec 5 -ErrorAction Stop
                Write-Host "  [✓] Edge Service启动成功！" -ForegroundColor Green
            } catch {
                Write-Host "  [✗] Edge Service启动失败，请检查日志" -ForegroundColor Red
            }
        } else {
            Write-Host "  [✗] Edge Service目录不存在" -ForegroundColor Red
        }
    }

    Write-Host "[修复] 测试前端..." -ForegroundColor Yellow
    try {
        $test = Invoke-WebRequest -Uri "http://localhost:5173" -TimeoutSec 3 -ErrorAction Stop
        Write-Host "  [✓] Frontend实际上可用" -ForegroundColor Green
    } catch {
        Write-Host "  [✗] Frontend确实不可用，尝试启动..." -ForegroundColor Red

        Write-Host "[修复] 启动Frontend..." -ForegroundColor Yellow

        # 尝试启动前端
        $distPath = "c:\Users\user\Desktop\毕业设计\dist"
        if (Test-Path $distPath) {
            Write-Host "  启动静态服务器..." -ForegroundColor Yellow
            Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$distPath'; python -m http.server 5173"

            Start-Sleep -Seconds 5

            try {
                $test = Invoke-WebRequest -Uri "http://localhost:5173" -TimeoutSec 5 -ErrorAction Stop
                Write-Host "  [✓] Frontend启动成功！" -ForegroundColor Green
            } catch {
                Write-Host "  [✗] Frontend启动失败，请手动启动" -ForegroundColor Red
            }
        } else {
            Write-Host "  [✗] Frontend dist目录不存在" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  完成" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# 显示访问地址
Write-Host "请尝试访问以下地址：" -ForegroundColor Yellow
Write-Host "  - 前端: http://localhost:5173" -ForegroundColor Cyan
Write-Host "  - API:  http://localhost:8080" -ForegroundColor Cyan
Write-Host "  - Edge: http://localhost:8084" -ForegroundColor Cyan
Write-Host ""

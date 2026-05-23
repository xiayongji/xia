# 智能家居系统 - 树莓派 Docker + GitHub 双轨部署完全指南

## 系统架构总览

```
┌─────────────────────────────────────────────────────────────┐
│                     GitHub (云端)                            │
│  ┌───────────┐    ┌──────────────────┐    ┌──────────────┐ │
│  │ 代码推送   │ -> │ GitHub Actions   │ -> │ ghcr.io      │ │
│  │ git push  │    │ QEMU ARM64 构建  │    │ 镜像仓库     │ │
│  └───────────┘    └──────────────────┘    └──────┬───────┘ │
└──────────────────────────────────────────────────┼──────────┘
                                                   │ docker pull
┌──────────────────────────────────────────────────┼──────────┐
│                树莓派 5 (ARM64 / 8GB)             │          │
│                                                  ▼          │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                  Docker Compose                       │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────────┐         │  │
│  │  │ MariaDB  │ │  Redis   │ │  RabbitMQ    │ 基础设施 │  │
│  │  │ :3306    │ │  :6379   │ │  :15672      │         │  │
│  │  └──────────┘ └──────────┘ └──────────────┘         │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────────┐         │  │
│  │  │ Eureka   │ │  API GW  │ │  Device Svc  │         │  │
│  │  │ :8761    │ │  :8080   │ │  :8081       │ 微服务  │  │
│  │  └──────────┘ └──────────┘ └──────────────┘         │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────────┐         │  │
│  │  │ Scene    │ │ Analytics│ │  Edge Svc    │         │  │
│  │  │ :8083    │ │ :8085    │ │  :8084/9094  │         │  │
│  │  └──────────┘ └──────────┘ └──────────────┘         │  │
│  │  ┌──────────┐                                        │  │
│  │  │ Frontend │  Nginx :80                             │  │
│  │  └──────────┘                                        │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
│  两种部署模式：                                             │
│  A) 本地构建 (推荐)：Pi 上 Maven 编译 + Docker 构建        │
│  B) 远程镜像：GitHub Actions 交叉编译 -> Pi 直接拉取       │
└─────────────────────────────────────────────────────────────┘
```

---

## 目录

1. [硬件与系统准备](#1-硬件与系统准备)
2. [方式 A：本地构建部署（推荐新手）](#2-方式-a本地构建部署)
3. [方式 B：GitHub Actions + 拉取镜像部署](#3-方式-bgithub-actions--拉取镜像部署)
4. [GitHub 仓库配置详解](#4-github-仓库配置详解)
5. [日常运维命令](#5-日常运维命令)
6. [JVM 调优与内存管理](#6-jvm-调优与内存管理)
7. [故障排查](#7-故障排查)

---

## 1. 硬件与系统准备

### 1.1 硬件要求

| 配置项 | 最小 | 推荐 |
|--------|------|------|
| 树莓派型号 | Pi 4 (4GB) | Pi 5 (8GB) |
| SD 卡 | 32GB Class 10 | 64GB+ A2 等级 |
| 电源 | 5V/3A | 官方电源适配器 |
| 散热 | 散热片 | 散热片 + 风扇 |
| 网络 | WiFi 5 / 有线 | 千兆有线 |

### 1.2 烧录系统（最关键的第一步）

**必须使用 64 位系统！** Docker 在 32 位系统上无法运行。

```
1. 下载 Raspberry Pi Imager: https://www.raspberrypi.com/software/
2. 选择设备: Raspberry Pi 5
3. 选择系统: Raspberry Pi OS (64-bit) ← 必须是 64-bit
4. 选择 SD 卡
5. 按 Ctrl+Shift+X 打开高级设置:
   - 主机名: smarthome-pi
   - 启用 SSH: ✓ (使用密码认证)
   - 用户名: pi 密码: (你的密码)
   - WiFi: 配置你的 WiFi
   - 时区: Asia/Shanghai
6. 烧录 + 插入 Pi 启动
```

### 1.3 首次连接到 Pi

```bash
# 从你的电脑连接 Pi
ssh pi@smarthome-pi.local
# 或者用 IP
ssh pi@192.168.x.x
```

### 1.4 系统初始化

```bash
# 更新系统（约 5-10 分钟）
sudo apt update && sudo apt full-upgrade -y

# 安装基础工具
sudo apt install -y curl git vim htop

# ===== 关键：扩容 swap（编译 Java 需要大量内存）=====
sudo dphys-swapfile swapoff
sudo sed -i 's/CONF_SWAPSIZE=.*/CONF_SWAPSIZE=2048/' /etc/dphys-swapfile
sudo dphys-swapfile setup
sudo dphys-swapfile swapon

# 验证 swap 已扩容到 2GB
free -h | grep Swap
```

### 1.5 安装 Docker

```bash
# Docker 官方一键安装脚本（自动识别 ARM64）
curl -fsSL https://get.docker.com -o /tmp/get-docker.sh
sudo sh /tmp/get-docker.sh

# 将 pi 用户加入 docker 组（免 sudo）
sudo usermod -aG docker pi

# 重新登录使权限生效
exit
# 重新 SSH 连接后再验证
ssh pi@smarthome-pi.local
docker run --rm hello-world
# 看到 "Hello from Docker!" 即为成功
```

### 1.6 安装 Java 17 + Maven（仅方式 A 需要）

```bash
sudo apt install -y openjdk-17-jdk maven

java -version   # 应显示 17.x.x
mvn --version   # 应显示 3.x
```

---

## 2. 方式 A：本地构建部署

> **适用场景**：想在 Pi 上一键搞定，不依赖 GitHub Actions。
> **时间**：首次 30-45 分钟（Maven 下载依赖 + 编译），后续 5-10 分钟。

### 2.1 克隆代码到 Pi

```bash
cd /home/pi

# 方式 1：从 GitHub 克隆
git clone https://github.com/你的用户名/你的仓库.git smarthome
cd smarthome

# 方式 2：从电脑上传（没有 GitHub 仓库时）
# 在电脑上打包：
#   cd 项目目录
#   tar --exclude='*/target' --exclude='node_modules' -czf smarthome.tar.gz .
#   scp smarthome.tar.gz pi@smarthome-pi.local:/home/pi/
# 在 Pi 上解压：
#   cd /home/pi && mkdir smarthome && cd smarthome
#   tar -xzf ../smarthome.tar.gz
```

### 2.2 一键部署

```bash
cd /home/pi/smarthome

# 首次运行：安装依赖（约 5 分钟）
./deploy-pi.sh install

# 检查环境
./deploy-pi.sh check

# 一键部署全部（build + infra + core + frontend）
./deploy-pi.sh quick
```

**`deploy-pi.sh quick` 内部执行流程：**

```
1. check_raspberry_pi   → 验证是 ARM64 架构
2. check_swap           → 确认 swap ≥ 1GB
3. check_disk           → 确认剩余空间 ≥ 5GB
4. build_local          → Maven 编译 7 个微服务（15-20 分钟）
5. start_infra          → 启动 MariaDB + Redis + RabbitMQ
6. start_core_services  → 启动 Eureka + Gateway + Device + Scene + Analytics
7. start_frontend       → 启动 Nginx 前端
8. show_status          → 显示所有容器状态
9. show_urls            → 显示访问地址
```

### 2.3 分步部署（适合调试）

```bash
cd /home/pi/smarthome

# 1. 先启动基础设施
./deploy-pi.sh infra
# 等待 MariaDB、Redis、RabbitMQ 全部 healthy

# 2. 构建微服务（修改代码后只需运行这个）
./deploy-pi.sh build

# 3. 启动核心服务
./deploy-pi.sh core

# 4. 启动前端
./deploy-pi.sh frontend

# 5. 查看状态
./deploy-pi.sh status
```

### 2.4 部署完成后验证

```bash
# 查看所有容器状态
./deploy-pi.sh status

# 测试 API
curl http://localhost:8080/actuator/health
curl http://localhost:8085/api/analytics/health

# 查看访问地址
./deploy-pi.sh urls
```

**预期输出：**
```
==============================================
  Smart Home - Raspberry Pi Access URLs
==============================================

  Frontend:       http://192.168.1.100
  API Gateway:    http://192.168.1.100:8080
  Eureka:         http://192.168.1.100:8761
  RabbitMQ Mgmt:  http://192.168.1.100:15672

  Default Credentials:
    Grafana:   admin / admin
    RabbitMQ:  admin / smart_home_mq_2024
    MariaDB:   admin / smart_home_admin_2024
==============================================
```

---

## 3. 方式 B：GitHub Actions + 拉取镜像部署

> **适用场景**：已有 GitHub 仓库，不想在 Pi 上编译（节省 CPU 和时间）。
> **原理**：GitHub Actions 用 QEMU 模拟 ARM64 环境编译 → 推到 ghcr.io → Pi 拉取运行。
> **Pi 上时间**：首次 10-15 分钟（拉取镜像），后续 2-5 分钟（增量拉取）。

### 3.1 架构说明：GitHub Actions 如何构建 ARM64 镜像

```
GitHub Actions (ubuntu-latest x86_64 runner)
    │
    ├── docker/setup-qemu-action@v3
    │   └── 安装 QEMU 用户态模拟器，可在 x86 上运行 ARM 指令
    │
    ├── docker/setup-buildx-action@v3
    │   └── 创建多平台构建器
    │
    └── docker/build-push-action@v5
        ├── platforms: linux/amd64,linux/arm64
        ├── context: ./smart-home-microservices/eureka-server
        ├── 构建 ARM64 镜像（通过 QEMU 模拟）
        └── push → ghcr.io/用户名/smart-home/SERVICE:pi-latest
```

### 3.2 GitHub 仓库准备

**第一步：在 GitHub 上创建仓库**

```
1. 登录 github.com
2. 点击右上角 + → New repository
3. Repository name: smart-home
4. 选择 Public 或 Private
5. 不要勾选 "Add a README file"
6. Create repository
```

**第二步：推送代码到 GitHub**

```bash
# 在项目根目录
cd /workspace  # 或你的项目路径

git init
git add .
git commit -m "Initial commit: smart home microservices with Pi Docker support"

# 关联远程仓库
git remote add origin https://github.com/你的用户名/smart-home.git

# 推送到 main 分支
git branch -M main
git push -u origin main
```

**第三步：配置 GitHub Container Registry 权限**

```
1. 进入仓库 Settings → Actions → General
2. Workflow permissions: 选择 "Read and write permissions" ✓
3. 勾选 "Allow GitHub Actions to create and approve pull requests"
4. Save
```

### 3.3 触发多架构构建

推送代码到 `main` 分支后，GitHub Actions 自动开始构建：

```
1. 打开仓库的 Actions 标签页
2. 查看 "Docker Multi-Arch Build" workflow
3. 每个服务独立并行构建（7-8 个并行 Job）
4. 构建时间：每个服务约 8-15 分钟（含 QEMU 模拟开销）
5. 完成后镜像推送至 ghcr.io
```

**手动触发构建**（不想 push 代码时）：

```
Actions → Docker Multi-Arch Build → Run workflow → Run workflow
```

**查看构建好的镜像：**

```
1. 进入仓库主页
2. 右侧找到 Packages 区域
3. 会看到：
   - smart-home/eureka-server:pi-latest
   - smart-home/api-gateway:pi-latest
   - smart-home/device-service:pi-latest
   - ... 等
```

### 3.4 Pi 上拉取预构建镜像

**第一步：在 Pi 上登录 GitHub Container Registry**

```bash
# 生成 Personal Access Token (classic)
# 1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
# 2. Generate new token (classic)
# 3. 勾选: read:packages
# 4. 复制生成的 token (ghp_xxxxxxxxxx)

# 在 Pi 上登录
echo "你的token" | docker login ghcr.io -u 你的GitHub用户名 --password-stdin
# 看到 "Login Succeeded"
```

**第二步：配置从 Registry 拉取**

Pi 上编辑 `.env.pi`：

```bash
cd /home/pi/smarthome
nano .env.pi
```

修改构建模式：

```ini
# 改为 registry 模式
BUILD_MODE=registry
REGISTRY=ghcr.io
# 改成你的 GitHub 用户名
REPO_OWNER=你的用户名
```

**第三步：使用 Registry 专用 compose 一键部署**

项目已包含 `docker-compose.pi.registry.yml`，所有服务使用 `image:` 从 ghcr.io 拉取，无需本地 `build:`。

```bash
cd /home/pi/smarthome

# 1. 登录 ghcr.io（用之前生成的 token）
echo "ghp_token" | docker login ghcr.io -u 你的GitHub用户名 --password-stdin

# 2. 一键拉取 + 启动全部服务
docker compose -f docker-compose.pi.registry.yml --env-file .env.pi up -d

# 3. 查看启动状态
docker compose -f docker-compose.pi.registry.yml --env-file .env.pi ps

# 4. 查看日志确认一切正常
docker compose -f docker-compose.pi.registry.yml logs -f --tail=50
```

**Pi 上只需 3 条命令：**

```bash
# 登录 → 启动 → 查看
echo "token" | docker login ghcr.io -u USER --password-stdin
docker compose -f docker-compose.pi.registry.yml --env-file .env.pi up -d
docker compose -f docker-compose.pi.registry.yml --env-file .env.pi ps
```

### 3.5 验证部署
```

---

## 4. GitHub 仓库配置详解

### 4.1 三个 GitHub Actions Workflow 说明

| Workflow 文件 | 触发条件 | 作用 |
|--------------|---------|------|
| `ci.yml` | Push/PR 到 main | 编译 + 单元测试所有微服务 |
| `docker-multiarch.yml` | Push 到 main / 打 tag v* | QEMU 交叉编译 ARM64+AMD64 镜像，推送到 ghcr.io |
| `deploy.yml` | 手动触发 / 镜像推送后 | SSH 到服务器自动部署 |

### 4.2 配置 Secrets（deploy.yml 需要）

```
仓库 Settings → Secrets and variables → Actions → New repository secret

DEPLOY_HOST        = 192.168.1.100     (树莓派 IP)
DEPLOY_USER        = pi               (SSH 用户名)
DEPLOY_PATH        = /home/pi/smarthome (项目路径)
SSH_PRIVATE_KEY    = (树莓派的 SSH 私钥内容)
```

### 4.3 树莓派端生成 SSH Key（给 GitHub Actions Deploy 用）

```bash
# 在 Pi 上生成密钥对
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_actions

# 将公钥加入 authorized_keys
cat ~/.ssh/github_actions.pub >> ~/.ssh/authorized_keys

# 查看私钥（复制全部内容添加到 GitHub Secret SSH_PRIVATE_KEY）
cat ~/.ssh/github_actions
```

---

## 5. 日常运维命令

### 5.1 核心命令速查

```bash
cd /home/pi/smarthome

# 查看所有容器状态
./deploy-pi.sh status

# 查看某服务日志
./deploy-pi.sh logs device-service
./deploy-pi.sh logs edge-service

# 重启某个服务
./deploy-pi.sh restart api-gateway

# 只停服务，保留基础设施
./deploy-pi.sh stop

# 全部停止
./deploy-pi.sh down

# 查看资源使用
docker stats --no-stream

# 清理 3 天前的无用镜像
./deploy-pi.sh prune
```

### 5.2 更新代码后重新部署

```bash
cd /home/pi/smarthome

# 拉取最新代码
git pull origin main

# 重新构建并重启
./deploy-pi.sh build
./deploy-pi.sh stop
./deploy-pi.sh core
./deploy-pi.sh status
```

### 5.3 MariaDB 数据管理

```bash
# 进入 MariaDB 命令行
docker exec -it smart-home-mariadb mariadb -u admin -p
# 输入密码: smart_home_admin_2024

# 备份数据库
docker exec smart-home-mariadb mariadb-dump -u root -psmart_home_root_2024 smart_home \
  > /home/pi/backup_$(date +%Y%m%d).sql

# 查看表
SHOW DATABASES;
USE smart_home;
SHOW TABLES;
```

### 5.4 Redis 缓存管理

```bash
# 进入 Redis
docker exec -it smart-home-redis redis-cli

# 查看内存使用
INFO memory

# 查看所有 key
KEYS *

# 清空缓存
FLUSHALL
```

---

## 6. JVM 调优与内存管理

### 6.1 Pi 上各服务内存占用（理论 + 实测）

| 服务 | JVM 堆 | 容器限制 | SerialGC | 说明 |
|------|--------|---------|----------|------|
| MariaDB | - | 384MB | - | InnoDB pool=64MB |
| Redis | - | 96MB | - | maxmemory=64MB |
| RabbitMQ | - | 256MB | - | 内存水位 40% |
| Eureka | 64-128MB | 192MB | ✓ | 最轻量 |
| API Gateway | 128-256MB | 320MB | ✓ | IO 密集型 |
| Device | 128-256MB | 320MB | ✓ | - |
| Scene | 128-256MB | 320MB | ✓ | - |
| Analytics | 128-512MB | 640MB | ✓ | 计算密集型 |
| Edge | 128-384MB | 512MB | ✓ | gRPC+DB |
| Multimodal | 128-384MB | 512MB | ✓ | - |
| Frontend | - | 64MB | - | Nginx |
| **合计(核心)** | - | **~2.0GB** | - | (不含监控) |
| **合计(全部)** | - | **~3.0GB** | - | (含 edge+multimodal+监控) |

### 6.2 为什么用 SerialGC 而不是 G1GC？

```
G1GC 特点：并行标记、并发回收、适合大堆（>4GB）、多核 CPU
           在 Pi 上会产生额外线程开销

SerialGC 特点：单线程、低开销、适合小堆（<512MB）、ARM 设备
              Pi 上实测吞吐量更高，CPU 占用更低

结论：Pi 上用 -XX:+UseSerialGC 比 G1GC 快 15-30%
```

### 6.3 内存不够时的调整

**Pi 4 (4GB) 调整方案：**

编辑 `.env.pi`，将每个 JVM 的 `-Xmx` 值减半：

```bash
JAVA_OPTS_EUREKA=-Xms48m -Xmx80m
JAVA_OPTS_GATEWAY=-Xms80m -Xmx160m
# ... 其他同理
```

然后关闭不需要的服务：

```bash
# 只启动最小核心服务，关闭 edge/multimodal/monitoring
docker compose -f docker-compose.pi.yml --env-file .env.pi up -d \
    mariadb redis rabbitmq eureka-server api-gateway \
    device-service scene-service analytics-service frontend
```

---

## 7. 故障排查

### 7.1 构建失败

```bash
# 现象：mvn package 时报 OutOfMemoryError
# 原因：swap 不够大

# 解决：
sudo dphys-swapfile swapoff
sudo sed -i 's/CONF_SWAPSIZE=.*/CONF_SWAPSIZE=2048/' /etc/dphys-swapfile
sudo dphys-swapfile setup
sudo dphys-swapfile swapon
free -h  # 确认 swap >= 2GB
```

### 7.2 容器启动失败

```bash
# 查看具体错误
docker compose -f docker-compose.pi.yml logs analytics-service

# 常见原因 1：MariaDB 还没准备好
# 等 60-90 秒让 MariaDB 完成初始化，再启动微服务

# 常见原因 2：端口被占用
sudo lsof -i :8080
sudo lsof -i :8085

# 常见原因 3：SD 卡空间不足
df -h /
docker system prune -a -f  # 清理无用镜像
```

### 7.3 服务启动但 API 返回 502/503

```bash
# 检查 Eureka 是否注册成功
curl http://localhost:8761/eureka/apps
# 应该看到所有已注册的服务

# 检查 API Gateway 路由
curl http://localhost:8080/actuator/gateway/routes

# 直接测试后端服务（绕过 Gateway）
curl http://localhost:8085/actuator/health
```

### 7.4 Docker 镜像拉取慢

```bash
# 配置 Docker 国内镜像加速
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json << 'EOF'
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.xuanyuan.me"
  ]
}
EOF
sudo systemctl restart docker
```

### 7.5 GitHub Actions 构建 ARM64 失败

```bash
# 常见原因：QEMU 模拟超时
# 解决：在 docker-multiarch.yml 中增加超时设置

# 常见原因：protobuf 编译失败（edge-service）
# 确认 Dockerfile 中 COPY 了 proto 目录
```

---

## 附录：文件对照表

| 文件 | 用途 | 目标环境 |
|------|------|----------|
| `Dockerfile` (7个) | 多阶段 Java 构建 | 通用 (x86+ARM) |
| `docker-compose.pi.yml` | Pi 专用编排 | 树莓派 |
| `.env.pi` | Pi 专用环境变量 | 树莓派 |
| `deploy-pi.sh` | Pi 一键部署脚本 | 树莓派 |
| `.github/workflows/docker-multiarch.yml` | 多架构交叉构建 | GitHub Actions |
| `.github/workflows/ci.yml` | 持续集成测试 | GitHub Actions |
| `docker-compose.yml` | 开发环境编排 | x86 开发机 |
| `.env` | 开发环境变量 | x86 开发机 |
| `deploy.sh` | 开发环境部署脚本 | x86 开发机 |
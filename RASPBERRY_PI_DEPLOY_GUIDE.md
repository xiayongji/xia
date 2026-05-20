# 树莓派边缘网关完整部署指南

## 目录
1. [硬件准备](#1-硬件准备)
2. [系统安装](#2-系统安装)
3. [基础环境配置](#3-基础环境配置)
4. [Redis安装配置](#4-redis安装配置)
5. [MySQL安装配置](#5-mysql安装配置)
6. [项目部署](#6-项目部署)
7. [服务启动](#7-服务启动)
8. [验证测试](#8-验证测试)
9. [常见问题排查](#9-常见问题排查)

---

## 1. 硬件准备

### 推荐配置
- **树莓派5** (4GB/8GB版本) - 您的配置完美！
- **microSD卡** (32GB+ Class10) - 您的32GB足够！
- **树莓派官方电源适配器** (5V/3A)
- **网络连接** (有线/WiFi)
- **可选配件**：
  - ZigBee协调器 (如SONOFF ZigBee 3.0 USB Dongle)
  - 蓝牙适配器
  - USB摄像头等

### Redis资源需求
- 内存：约50-200MB（您有8GB，绝对足够！）
- 磁盘：约50-100MB
- 您的配置完全能轻松运行！

---

## 2. 系统安装

### 2.1 下载Raspberry Pi Imager
访问：https://www.raspberrypi.com/software/

### 2.2 烧录系统
1. 打开Raspberry Pi Imager
2. **选择设备**: Raspberry Pi 5
3. **选择系统**: Raspberry Pi OS (64-bit)
4. **选择存储**: 选择您的SD卡
5. **高级配置** (按Ctrl+Shift+X):
   ```
   主机名: smarthome-edge
   启用SSH: ✓
   用户: pi
   密码: 您的密码
   WiFi配置:
     SSID: 您的WiFi名称
     密码: 您的WiFi密码
     国家: CN
   时区: Asia/Shanghai
   键盘: China
   ```
6. 点击**烧录**

### 2.3 首次启动
1. 将SD卡插入树莓派
2. 连接电源启动
3. 等待约2-3分钟

---

## 3. 基础环境配置

### 3.1 连接到树莓派
```bash
# Windows PowerShell
ssh pi@smarthome-edge.local

# 或使用IP地址 (替换为实际IP)
ssh pi@192.168.1.100
```

### 3.2 更新系统
```bash
# 更新软件包列表
sudo apt update

# 升级所有已安装的软件 (约10-20分钟)
sudo apt full-upgrade -y

# 清理不需要的包
sudo apt autoremove -y
```

### 3.3 配置静态IP (推荐)

**注意：新版本树莓派OS (Bookworm及以上) 使用NetworkManager，不再使用dhcpcd！**

#### 方法一：使用nmtui（图形化界面，推荐）
```bash
# 启动NetworkManager文本界面
sudo nmtui
```

步骤：
1. 选择 `Edit a connection`
2. 选择要配置的网络（Wired/Wireless）
3. 选择 `IPv4 CONFIGURATION` → 改为 `Manual`
4. 添加地址（例如：`192.168.1.100/24`）
5. 添加网关（例如：`192.168.1.1`）
6. 添加DNS（例如：`192.168.1.1, 8.8.8.8`）
7. 保存并退出
8. 重启网络连接

#### 方法二：使用nmcli（命令行）
```bash
# 查看网络连接
nmcli connection show

# 配置有线网络（将"eth0"替换为您的接口名）
sudo nmcli connection modify eth0 \
  ipv4.addresses 192.168.1.100/24 \
  ipv4.gateway 192.168.1.1 \
  ipv4.dns "192.168.1.1 8.8.8.8" \
  ipv4.method manual

# 配置无线网络（将"wlan0"替换为您的接口名）
sudo nmcli connection modify wlan0 \
  ipv4.addresses 192.168.1.100/24 \
  ipv4.gateway 192.168.1.1 \
  ipv4.dns "192.168.1.1 8.8.8.8" \
  ipv4.method manual

# 重启网络连接
sudo nmcli connection up eth0
# 或
sudo nmcli connection up wlan0

# 验证IP配置
ip addr show
```

#### 方法三：如果您仍使用旧系统（dhcpcd）
```bash
# 检查dhcpcd是否存在
systemctl status dhcpcd

# 如果存在，继续使用老方法
sudo nano /etc/dhcpcd.conf
```

在文件末尾添加：
```
interface eth0
static ip_address=192.168.1.100/24
static routers=192.168.1.1
static domain_name_servers=192.168.1.1 8.8.8.8

interface wlan0
static ip_address=192.168.1.100/24
static routers=192.168.1.1
static domain_name_servers=192.168.1.1 8.8.8.8
```

保存并重启网络：
```bash
sudo systemctl restart dhcpcd
```

### 3.4 安装Java 17
```bash
# 安装OpenJDK 17
sudo apt install openjdk-17-jdk -y

# 验证安装
java -version

# 预期输出: openjdk version "17.x.x"
```

### 3.5 安装Maven
```bash
# 安装Maven
sudo apt install maven -y

# 验证安装
mvn -version

# 预期输出: Apache Maven 3.9.x
```

### 3.6 安装Git
```bash
# 安装Git
sudo apt install git -y

# 配置Git (可选)
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# 验证安装
git --version
```

---

## 4. Redis安装配置

### 4.1 安装Redis
```bash
# 安装Redis服务器
sudo apt install redis-server -y

# 启动Redis
sudo systemctl start redis

# 设置开机自启
sudo systemctl enable redis

# 验证Redis
redis-cli ping
# 预期输出: PONG
```

### 4.2 配置Redis (可选)
```bash
# 编辑配置文件
sudo nano /etc/redis/redis.conf
```

建议修改：
```
# 设置密码 (取消注释并设置密码)
requirepass your_redis_password

# 内存限制 (根据树莓派内存调整，256MB足够)
maxmemory 256mb
maxmemory-policy allkeys-lru

# 启用持久化
appendonly yes
appendfsync everysec
```

重启Redis：
```bash
sudo systemctl restart redis
```

### 4.3 验证Redis连接
```bash
# 不带密码
redis-cli ping

# 带密码
redis-cli -a your_redis_password ping
```

---

## 5. MySQL安装配置

### 5.1 安装MariaDB（推荐）或MySQL
**注意：树莓派新系统使用MariaDB代替MySQL，两者完全兼容！**

```bash
# 安装MariaDB服务器（推荐，树莓派官方默认）
sudo apt install mariadb-server -y

# 如果您确实想安装MySQL（不推荐，可能找不到软件包）
# sudo apt install mysql-server -y

# 启动服务
sudo systemctl start mariadb

# 设置开机自启
sudo systemctl enable mariadb

# 安全初始化
sudo mariadb-secure-installation
```

安全初始化回答：
```
VALIDATE PASSWORD COMPONENT: N
设置root密码: infini_rag_flow
移除匿名用户: Y
禁止远程root登录: N
移除测试数据库: Y
重新加载权限表: Y
```

### 5.2 创建数据库
```bash
# 登录MariaDB（MySQL命令也可用）
sudo mariadb -u root -p
# 或
sudo mysql -u root -p
# 输入密码: infini_rag_flow
```

在MariaDB命令行执行：
```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS smarthome_edge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 查看数据库
SHOW DATABASES;

-- 退出
EXIT;
```

### 5.3 验证
```bash
# 检查服务状态
sudo systemctl status mariadb

# 测试连接（用mariadb或mysql命令都可以）
mariadb -u root -pinfini_rag_flow -e "SELECT VERSION();"
# 或
mysql -u root -pinfini_rag_flow -e "SELECT VERSION();"
```

---

## 6. 项目部署

### 6.1 准备项目目录
```bash
# 创建项目目录
mkdir -p ~/smarthome
cd ~/smarthome
```

### 6.2 上传项目代码

**方式一: 使用Git克隆**
```bash
# 如果有Git仓库
git clone https://your-git-repo/smart-home-microservices.git
cd smart-home-microservices/edge-service
```

**方式二: 从本地电脑上传**

在您的Windows电脑PowerShell执行：
```powershell
# 进入项目目录
cd C:\Users\user\Desktop\毕业设计

# 打包项目 (排除target目录)
tar -cvf edge-service.tar --exclude='edge-service/target' smart-home-microservices/edge-service

# 上传到树莓派 (替换为实际IP)
scp edge-service.tar pi@192.168.1.100:/home/pi/smarthome/
```

在树莓派解压：
```bash
cd ~/smarthome
tar -xvf edge-service.tar
cd smart-home-microservices/edge-service
```

### 6.3 配置application.yml
```bash
# 编辑配置文件
nano src/main/resources/application.yml
```

关键配置项：
```yaml
spring:
  # Redis缓存配置
  data:
    redis:
      host: localhost
      port: 6379
      # password: your_redis_password  # 如果设置了密码取消注释
      database: 0
      timeout: 5000
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
  # MySQL数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/smarthome_edge?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: infini_rag_flow

server:
  port: 8084

grpc:
  server:
    port: 9094
```

### 6.4 编译项目
```bash
# 进入edge-service目录
cd ~/smarthome/smart-home-microservices/edge-service

# 编译打包 (跳过测试)
mvn clean package -DskipTests

# 等待编译完成 (约5-10分钟)

# 检查生成的jar文件
ls -lh target/edge-service-1.0.0.jar
```

---

## 7. 服务启动

### 7.1 创建Systemd服务
```bash
# 创建服务文件
sudo nano /etc/systemd/system/edge-service.service
```

内容：
```ini
[Unit]
Description=Smart Home Edge Gateway Service
After=network.target mysql.service redis.service
Wants=mysql.service redis.service

[Service]
Type=simple
User=pi
WorkingDirectory=/home/pi/smarthome/smart-home-microservices/edge-service
ExecStart=/usr/bin/java -jar /home/pi/smarthome/smart-home-microservices/edge-service/target/edge-service-1.0.0.jar
Restart=always
RestartSec=10
StandardOutput=append:/home/pi/smarthome/smart-home-microservices/edge-service/logs/edge-service.log
StandardError=append:/home/pi/smarthome/smart-home-microservices/edge-service/logs/edge-service.log

# 内存限制 (8GB内存可以设置更大)
Environment=JAVA_OPTS="-Xmx512m -Xms256m"

[Install]
WantedBy=multi-user.target
```

### 7.2 创建日志目录
```bash
mkdir -p /home/pi/smarthome/smart-home-microservices/edge-service/logs
```

### 7.3 启动服务
```bash
# 重新加载systemd
sudo systemctl daemon-reload

# 启用开机自启
sudo systemctl enable edge-service

# 启动服务
sudo systemctl start edge-service

# 查看服务状态
sudo systemctl status edge-service
```

### 7.4 常用服务命令
```bash
# 启动
sudo systemctl start edge-service

# 停止
sudo systemctl stop edge-service

# 重启
sudo systemctl restart edge-service

# 查看状态
sudo systemctl status edge-service

# 查看实时日志
sudo journalctl -u edge-service -f

# 或查看日志文件
tail -f /home/pi/smarthome/smart-home-microservices/edge-service/logs/edge-service.log
```

---

## 8. 验证测试

### 8.1 测试健康检查
```bash
# 本地测试
curl http://localhost:8084/actuator/health

# 或从其他电脑
curl http://192.168.1.100:8084/actuator/health
```

预期输出：
```json
{"status":"UP"}
```

### 8.2 测试设备API
```bash
# 获取本地设备列表
curl http://localhost:8084/api/edge/devices

# 查看性能指标
curl http://localhost:8084/api/edge/metrics/latency
```

### 8.3 测试Redis缓存
```bash
# 进入Redis命令行
redis-cli

# 查看所有键
KEYS *

# 查看缓存数据
GET "deviceStatus::device-001"

# 退出
EXIT
```

### 8.4 配置防火墙 (可选)
```bash
# 安装防火墙
sudo apt install ufw -y

# 允许SSH
sudo ufw allow 22/tcp

# 允许HTTP API
sudo ufw allow 8084/tcp

# 允许gRPC
sudo ufw allow 9094/tcp

# 启用防火墙
sudo ufw enable

# 查看状态
sudo ufw status
```

---

## 9. 常见问题排查

### 9.1 服务无法启动
```bash
# 查看详细日志
sudo journalctl -u edge-service -n 100

# 检查端口占用
sudo netstat -tulpn | grep -E '8084|9094'

# 检查Java进程
ps aux | grep java
```

### 9.2 Redis连接失败
```bash
# 检查Redis状态
sudo systemctl status redis

# 测试Redis连接
redis-cli ping

# 查看Redis日志
sudo tail -50 /var/log/redis/redis-server.log
```

### 9.3 MySQL连接失败
```bash
# 检查MySQL状态
sudo systemctl status mysql

# 测试MySQL连接
mysql -u root -pinfini_rag_flow -e "SELECT 1;"

# 查看MySQL日志
sudo tail -50 /var/log/mysql/error.log
```

### 9.4 内存不足
```bash
# 查看内存使用
free -h

# 查看进程内存占用
top -o %MEM

# 调整JVM参数 (您有8GB，可以适当调大)
# 编辑服务文件，修改 Xmx 和 Xms
sudo nano /etc/systemd/system/edge-service.service
```

### 9.5 重新部署
```bash
# 1. 停止服务
sudo systemctl stop edge-service

# 2. 更新代码 (拉取或重新上传)
cd ~/smarthome/smart-home-microservices/edge-service

# 3. 重新编译
mvn clean package -DskipTests

# 4. 启动服务
sudo systemctl start edge-service

# 5. 查看日志
sudo journalctl -u edge-service -f
```

---

## 10. 自动化脚本 (可选)

### 一键部署脚本
```bash
# 创建部署脚本
nano ~/smarthome/deploy-all.sh
```

内容：
```bash
#!/bin/bash
set -e

echo "========================================"
echo "智能边缘网关一键部署"
echo "========================================"

# 更新系统
echo "[1/8] 更新系统..."
sudo apt update && sudo apt full-upgrade -y

# 安装Java
echo "[2/8] 安装Java 17..."
sudo apt install openjdk-17-jdk -y

# 安装Maven
echo "[3/8] 安装Maven..."
sudo apt install maven -y

# 安装Redis
echo "[4/8] 安装Redis..."
sudo apt install redis-server -y
sudo systemctl start redis
sudo systemctl enable redis

# 安装MySQL
echo "[5/8] 安装MySQL..."
sudo apt install mysql-server -y
sudo systemctl start mysql
sudo systemctl enable mysql

# 创建数据库
echo "[6/8] 创建数据库..."
mysql -u root -pinfini_rag_flow -e "CREATE DATABASE IF NOT EXISTS smarthome_edge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null || true

# 编译项目
echo "[7/8] 编译项目..."
cd ~/smarthome/smart-home-microservices/edge-service
mvn clean package -DskipTests

# 启动服务
echo "[8/8] 启动服务..."
sudo systemctl daemon-reload
sudo systemctl enable edge-service
sudo systemctl restart edge-service

echo "========================================"
echo "部署完成!"
echo "访问地址: http://$(hostname -I | awk '{print $1}'):8084"
echo "========================================"
```

设置执行权限：
```bash
chmod +x ~/smarthome/deploy-all.sh

# 执行脚本
./deploy-all.sh
```

---

## 联系支持
如遇问题，查看日志文件：
- 应用日志: `/home/pi/smarthome/smart-home-microservices/edge-service/logs/edge-service.log`
- 系统日志: `sudo journalctl -u edge-service`

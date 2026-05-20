# 边缘服务数据库集成部署指南

## 概述

本文档说明如何部署配置好数据库连接的边缘服务，使其从MySQL数据库获取所有设备状态。

## 主要修改内容

### 1. 新增数据库表结构

创建了以下数据库表：
- `edge_device`: 存储设备基本信息
- `edge_device_status`: 存储设备实时状态
- `edge_command_history`: 存储命令执行历史（可选）

### 2. 新增Repository层

- `DeviceRepository`: 设备数据访问层
- `DeviceStatusRepository`: 设备状态数据访问层

### 3. 核心服务修改

- `LocalDeviceManager`: 从内存Map改为使用数据库持久化
- `EdgeGatewayService`: 从数据库获取设备状态
- `EdgeGatewayController`: 所有API都从数据库查询

### 4. 数据库优化

- HikariCP连接池配置优化
- JPA性能优化参数
- 数据库索引创建

## 部署步骤

### 步骤1：创建数据库

在MySQL服务器上执行初始化脚本：

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source /home/pi/smarthome/edge-service/sql/init-edge-database.sql

# 或者在命令行中直接执行
mysql -u root -p < sql/init-edge-database.sql
```

### 步骤2：编译边缘服务

```bash
cd ~/smarthome/edge-service

# 清理并打包（跳过测试）
mvn clean package -DskipTests

# 等待编译完成（约5-10分钟）
```

### 步骤3：配置数据库连接

编辑配置文件：

```bash
nano src/main/resources/application.yml
```

确保数据库配置正确：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smarthome_edge?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: infini_rag_flow
```

### 步骤4：启动服务

```bash
# 启动边缘服务
sudo systemctl start edge-service

# 检查服务状态
sudo systemctl status edge-service
```

## 验证部署

### 方法1：使用测试脚本

```bash
# Linux/Mac
chmod +x scripts/test-database.sh
./scripts/test-database.sh

# Windows
scripts\test-database.bat
```

### 方法2：手动测试API

```bash
# 测试健康检查
curl http://localhost:8084/api/edge/health

# 测试获取所有设备状态（从数据库）
curl http://localhost:8084/api/edge/devices/status/all

# 测试获取设备统计
curl http://localhost:8084/api/edge/devices/statistics
```

## API接口说明

### 设备状态查询（从数据库获取）

```bash
# 获取单个设备最新状态
curl http://localhost:8084/api/edge/devices/{deviceId}/status/latest

# 获取所有设备最新状态
curl http://localhost:8084/api/edge/devices/status/all

# 获取设备统计信息
curl http://localhost:8084/api/edge/devices/statistics

# 获取特定时间后更新的设备状态
curl "http://localhost:8084/api/edge/devices/status/updated?since=2026-05-18T10:00:00"
```

### 设备控制

```bash
# 控制设备
curl -X POST http://localhost:8084/api/edge/devices/{deviceId}/command \
  -H "Content-Type: application/json" \
  -d '{"command":"turn_on"}'
```

### 云端同步

```bash
# 手动触发云端同步
curl -X POST http://localhost:8084/api/edge/sync/cloud
```

## 数据库表结构说明

### edge_device 表

| 字段 | 类型 | 说明 |
|------|------|------|
| device_id | VARCHAR(64) | 设备唯一ID（主键） |
| device_name | VARCHAR(128) | 设备名称 |
| device_type | VARCHAR(64) | 设备类型 |
| protocol | VARCHAR(32) | 通信协议（WiFi/Bluetooth/ZigBee） |
| status | VARCHAR(32) | 设备状态 |
| location | VARCHAR(128) | 设备位置 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### edge_device_status 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| device_id | VARCHAR(64) | 设备ID（外键） |
| status | VARCHAR(32) | 设备状态 |
| device_type | VARCHAR(64) | 设备类型 |
| power | DOUBLE | 功率（W） |
| temperature | DOUBLE | 温度（℃） |
| humidity | DOUBLE | 湿度（%） |
| properties | TEXT | 其他属性（JSON格式） |
| last_update_time | DATETIME | 最后更新时间 |
| created_at | DATETIME | 创建时间 |

## 性能优化

### 1. 数据库连接池

HikariCP配置优化：
- 最大连接数：10
- 最小空闲连接：5
- 连接超时：30秒
- 空闲超时：10分钟

### 2. JPA优化

- 批量插入大小：20
- 启用批量插入和更新
- 禁用二级缓存

### 3. 索引优化

创建了以下索引：
- `idx_device_id`: 设备ID索引
- `idx_last_update_time`: 更新时间索引
- `idx_status`: 状态索引
- `idx_device_type_status`: 设备类型和状态复合索引

## 监控和维护

### 查看数据库连接

```sql
-- 查看当前连接
SHOW PROCESSLIST;

-- 查看连接池状态
SELECT * FROM information_schema.HIKARICPStatistics;
```

### 查看设备状态

```sql
-- 查看所有设备状态
SELECT * FROM edge_device_status ORDER BY last_update_time DESC;

-- 查看在线设备
SELECT * FROM edge_device_status WHERE status = 'online';

-- 查看异常设备
SELECT * FROM edge_device_status WHERE status = 'error';
```

### 清理旧数据

```sql
-- 删除30天前的历史记录
DELETE FROM edge_command_history
WHERE executed_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

## 常见问题排查

### 1. 数据库连接失败

检查项：
- MySQL服务是否运行：`sudo systemctl status mysql`
- 数据库是否创建：`mysql -u root -p -e "SHOW DATABASES;"`
- 端口是否正确：默认3306
- 防火墙是否开放：`sudo ufw allow 3306`

### 2. 查询性能慢

优化方法：
- 检查索引是否生效
- 定期清理旧数据
- 调整连接池大小
- 使用Redis缓存热点数据

### 3. 数据不同步

检查项：
- 云端同步服务是否正常
- 网络连接是否稳定
- 同步频率配置是否合理
- 查看日志：`tail -f logs/edge-service.log`

## 数据库备份

### 备份脚本

```bash
#!/bin/bash
BACKUP_DIR="/home/pi/smarthome/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# 备份数据库
mysqldump -u root -p'infini_rag_flow' smarthome_edge > "$BACKUP_DIR/db_backup_$DATE.sql"

# 压缩备份
tar -czf "$BACKUP_DIR/edge_backup_$DATE.tar.gz" "$BACKUP_DIR/db_backup_$DATE.sql"

# 删除7天前的备份
find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete

echo "备份完成: $BACKUP_DIR/edge_backup_$DATE.tar.gz"
```

### 恢复数据

```bash
# 停止服务
sudo systemctl stop edge-service

# 恢复数据库
mysql -u root -p'infini_rag_flow' smarthome_edge < backup.sql

# 启动服务
sudo systemctl start edge-service
```

## 总结

修改后的边缘服务实现了：
1. ✅ 所有设备状态从数据库获取
2. ✅ 实时状态持久化存储
3. ✅ 支持复杂的数据库查询
4. ✅ 云边数据同步
5. ✅ 高性能的数据库访问
6. ✅ 完整的监控和维护工具

部署后，边缘服务将能够可靠地从MySQL数据库获取所有设备状态，支持离线查询和历史数据分析。

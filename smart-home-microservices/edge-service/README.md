# 边缘服务数据库集成说明

## 项目简介

智能家居边缘网关服务，负责管理本地设备并与云端同步数据。

## 核心功能

本版本实现了与MySQL数据库的完整集成，所有设备状态都从数据库获取和存储。

## 技术架构

### 数据层
- **MySQL**: 主数据库，存储设备和状态数据
- **Redis**: 缓存层（可选配置）
- **HikariCP**: 数据库连接池

### 应用层
- **Spring Boot 3.2.0**: 核心框架
- **Spring Data JPA**: 数据访问
- **Spring Cloud**: 微服务架构
- **gRPC**: 高性能RPC通信

## 核心组件

### 1. Entity实体类

- [Device.java](src/main/java/com/smarthome/edge/entity/Device.java) - 设备实体
- [DeviceStatus.java](src/main/java/com/smarthome/edge/entity/DeviceStatus.java) - 设备状态实体
- [CommandResult.java](src/main/java/com/smarthome/edge/entity/CommandResult.java) - 命令结果实体

### 2. Repository数据访问层

- [DeviceRepository.java](src/main/java/com/smarthome/edge/repository/DeviceRepository.java) - 设备Repository
- [DeviceStatusRepository.java](src/main/java/com/smarthome/edge/repository/DeviceStatusRepository.java) - 设备状态Repository

### 3. Service服务层

- [LocalDeviceManager.java](src/main/java/com/smarthome/edge/LocalDeviceManager.java) - 本地设备管理（使用数据库）
- [EdgeGatewayService.java](src/main/java/com/smarthome/edge/EdgeGatewayService.java) - 边缘网关服务
- [CloudSyncService.java](src/main/java/com/smarthome/edge/CloudSyncService.java) - 云端同步服务

### 4. Controller控制器

- [EdgeGatewayController.java](src/main/java/com/smarthome/edge/controller/EdgeGatewayController.java) - REST API控制器

### 5. gRPC服务

- [DeviceControlServiceImpl.java](src/main/java/com/smarthome/edge/grpc/DeviceControlServiceImpl.java) - gRPC服务实现

## 数据库表结构

### edge_device

```sql
CREATE TABLE edge_device (
    device_id VARCHAR(64) PRIMARY KEY,
    device_name VARCHAR(128),
    device_type VARCHAR(64),
    protocol VARCHAR(32),
    status VARCHAR(32),
    location VARCHAR(128),
    created_at DATETIME,
    updated_at DATETIME
);
```

### edge_device_status

```sql
CREATE TABLE edge_device_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL,
    status VARCHAR(32),
    device_type VARCHAR(64),
    power DOUBLE,
    temperature DOUBLE,
    humidity DOUBLE,
    properties TEXT,
    last_update_time DATETIME,
    created_at DATETIME,
    UNIQUE KEY uk_device_id (device_id),
    FOREIGN KEY (device_id) REFERENCES edge_device(device_id) ON DELETE CASCADE
);
```

## API接口

### 设备状态查询

```bash
# 获取所有设备状态
GET /api/edge/devices/status/all

# 获取单个设备最新状态
GET /api/edge/devices/{deviceId}/status/latest

# 获取设备统计
GET /api/edge/devices/statistics
```

### 设备控制

```bash
# 控制设备
POST /api/edge/devices/{deviceId}/command
Content-Type: application/json

{"command": "turn_on"}
```

### 云端同步

```bash
# 触发云端同步
POST /api/edge/sync/cloud
```

### 健康检查

```bash
# 健康检查
GET /api/edge/health
```

## 配置说明

### 数据库配置

编辑 [application.yml](src/main/resources/application.yml):

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smarthome_edge
    username: root
    password: infini_rag_flow
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
```

### 服务端口

- HTTP API: 8084
- gRPC: 9090

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < sql/init-edge-database.sql
```

### 2. 编译项目

```bash
mvn clean package -DskipTests
```

### 3. 启动服务

```bash
java -jar target/edge-service-1.0.0.jar
```

### 4. 测试API

```bash
# 健康检查
curl http://localhost:8084/api/edge/health

# 获取所有设备状态
curl http://localhost:8084/api/edge/devices/status/all
```

## 测试脚本

### 数据库测试

```bash
# Linux/Mac
chmod +x scripts/test-database.sh
./scripts/test-database.sh

# Windows
scripts\test-database.bat
```

### API测试

```bash
chmod +x scripts/test-api.sh
./scripts/test-api.sh
```

## 性能指标

- 设备状态查询响应时间: <50ms
- 数据库连接池大小: 10
- 定期同步频率: 10秒
- 支持协议: WiFi, Bluetooth, ZigBee

## 监控

### 查看日志

```bash
# 实时日志
tail -f logs/edge-service.log

# systemd日志
sudo journalctl -u edge-service -f
```

### 性能监控

```bash
# 获取性能指标
curl http://localhost:8084/api/edge/metrics/latency
```

## 常见问题

### 1. 数据库连接失败

检查MySQL服务是否运行：
```bash
sudo systemctl status mysql
```

### 2. 设备状态查询慢

可能原因：
- 缺少索引
- 数据量过大
- 连接池配置不当

解决方案：
- 运行数据库优化脚本
- 增加数据库索引
- 调整连接池参数

### 3. 云端同步失败

检查项：
- 云端服务是否可用
- 网络连接是否正常
- 防火墙是否开放端口

## 维护

### 数据备份

```bash
mysqldump -u root -p infini_rag_flow smarthome_edge > backup.sql
```

### 数据恢复

```bash
mysql -u root -p infini_rag_flow smarthome_edge < backup.sql
```

### 清理旧数据

```sql
DELETE FROM edge_command_history
WHERE executed_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

## 文档

- [部署指南](DEPLOYMENT-GUIDE.md) - 详细部署说明
- [API文档](API.md) - API接口文档
- [数据库设计](DATABASE.md) - 数据库设计说明

## 技术支持

如有问题，请检查：
1. 日志文件
2. 数据库连接配置
3. 网络连接状态
4. 防火墙规则

## 版本历史

### v1.0.0 (2026-05-18)
- 实现数据库持久化存储
- 优化设备状态查询性能
- 添加云边同步功能
- 完善监控和维护工具

## 许可证

本项目仅用于毕业设计学习交流。

# 树莓派边缘服务测试指南

## 目录

1. [基础健康检查](#1-基础健康检查)
2. [REST API 测试](#2-rest-api-测试)
3. [gRPC 服务测试](#3-grpc-服务测试)
4. [设备管理功能测试](#4-设备管理功能测试)
5. [可视化接口测试](#5-可视化接口测试)
6. [数据库连接测试](#6-数据库连接测试)
7. [测试脚本](#7-测试脚本)

---

## 1. 基础健康检查

### 1.1 检查容器运行状态

```bash
# 在树莓派上执行
docker compose -f docker-compose.pi.edge-only.yml --env-file .env.pi.edge-only ps
```

**预期输出**：所有容器状态应为 `Up (healthy)`

```
NAME           COMMAND                  SERVICE         STATUS              PORTS
edge-mariadb   "docker-entrypoint.s…"   mariadb         running (healthy)   0.0.0.0:3306->3306/tcp
edge-redis     "docker-entrypoint.s…"   redis           running (healthy)   0.0.0.0:6379->6379/tcp
edge-service   "sh -c 'java ${JAVA_…"   edge-service    running (healthy)   0.0.0.0:8084->8084/tcp, 0.0.0.0:9094->9094/tcp
```

### 1.2 测试 Spring Actuator 健康端点

```bash
# 方式一：在 Pi 本地测试
curl http://localhost:8084/actuator/health

# 方式二：从局域网其他设备测试（替换为 Pi 的 IP）
curl http://192.168.1.100:8084/actuator/health
```

**预期输出**：
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

### 1.3 检查服务日志

```bash
# 查看边缘服务日志
docker compose -f docker-compose.pi.edge-only.yml logs edge-service

# 持续跟踪日志
docker compose -f docker-compose.pi.edge-only.yml logs -f edge-service
```

**关键信息**：
- 应看到 `Started EdgeServiceApplication`
- 不应有 `ERROR` 级别日志
- gRPC 服务应显示 `Started gRPC server on port 9094`

---

## 2. REST API 测试

### 2.1 API 端点列表

| 端点 | 方法 | 功能 |
|------|------|------|
| `/api/edge/health` | GET | 服务健康检查 |
| `/api/edge/devices` | GET | 获取所有设备列表 |
| `/api/edge/devices/{deviceId}/status` | GET | 获取设备状态 |
| `/api/edge/devices/register` | POST | 注册新设备 |
| `/api/edge/devices/{deviceId}/command` | POST | 发送设备控制命令 |
| `/api/edge/devices/statistics` | GET | 获取设备统计 |
| `/api/edge/sync/cloud` | POST | 同步到云端 |

### 2.2 测试健康检查接口

```bash
curl http://localhost:8084/api/edge/health
```

**预期输出**：
```json
{
  "status": "healthy",
  "timestamp": "2024-01-15T10:30:00",
  "services": {
    "database": "connected",
    "redis": "connected",
    "grpc": "running"
  }
}
```

### 2.3 测试设备列表接口

```bash
curl http://localhost:8084/api/edge/devices
```

**预期输出**（首次运行应为空数组）：
```json
[]
```

### 2.4 测试设备注册接口

```bash
curl -X POST http://localhost:8084/api/edge/devices/register \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "test-light-001",
    "deviceName": "测试智能灯",
    "deviceType": "LIGHT",
    "protocol": "MQTT",
    "ipAddress": "192.168.1.101",
    "status": "ONLINE"
  }'
```

**预期输出**：
```json
{
  "success": true,
  "message": "Device registered successfully",
  "device": {
    "deviceId": "test-light-001",
    "deviceName": "测试智能灯",
    "deviceType": "LIGHT",
    "protocol": "MQTT",
    "ipAddress": "192.168.1.101",
    "status": "ONLINE"
  }
}
```

### 2.5 测试设备控制接口

```bash
curl -X POST http://localhost:8084/api/edge/devices/test-light-001/command \
  -H "Content-Type: application/json" \
  -d '{
    "commandType": "ON",
    "value": "100",
    "timestamp": "2024-01-15T10:35:00"
  }'
```

**预期输出**：
```json
{
  "success": true,
  "deviceId": "test-light-001",
  "commandType": "ON",
  "message": "Command executed successfully",
  "executionTime": 15
}
```

### 2.6 测试设备状态接口

```bash
curl http://localhost:8084/api/edge/devices/test-light-001/status
```

**预期输出**：
```json
{
  "deviceId": "test-light-001",
  "status": "ONLINE",
  "lastUpdated": "2024-01-15T10:35:00",
  "properties": {
    "power": "ON",
    "brightness": 100
  }
}
```

---

## 3. gRPC 服务测试

### 3.1 查看 gRPC 端口是否监听

```bash
# 在树莓派上检查端口
netstat -tlnp | grep 9094

# 或使用 ss
ss -tlnp | grep 9094
```

**预期输出**：
```
LISTEN 0      50         *:9094      *:*    users:(("java",pid=xxxx,fd=xx))
```

### 3.2 使用 gRPCurl 测试（推荐）

```bash
# 安装 grpcurl（需要先安装 Go）
sudo apt install golang-go
go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest

# 查看可用的 gRPC 服务
grpcurl -plaintext localhost:9094 list

# 查看具体服务的方法
grpcurl -plaintext localhost:9094 list com.smarthome.edge.EdgeGatewayService

# 调用 gRPC 方法
grpcurl -plaintext -d '{"deviceId": "test-light-001"}' \
  localhost:9094 com.smarthome.edge.EdgeGatewayService.GetDeviceStatus
```

---

## 4. 设备管理功能测试

### 4.1 测试设备统计接口

```bash
curl http://localhost:8084/api/edge/devices/statistics
```

**预期输出**：
```json
{
  "totalDevices": 1,
  "onlineDevices": 1,
  "offlineDevices": 0,
  "deviceTypes": {
    "LIGHT": 1
  },
  "protocols": {
    "MQTT": 1
  }
}
```

### 4.2 测试设备查询接口

```bash
# 按类型查询
curl http://localhost:8084/api/edge/devices/type/LIGHT

# 按协议查询
curl http://localhost:8084/api/edge/devices/protocol/MQTT

# 获取所有设备状态
curl http://localhost:8084/api/edge/devices/status/all
```

### 4.3 测试云端同步接口

```bash
curl -X POST http://localhost:8084/api/edge/sync/cloud
```

**预期输出**：
```json
{
  "success": true,
  "message": "Sync completed",
  "syncedDevices": 1,
  "timestamp": "2024-01-15T10:40:00"
}
```

---

## 5. 可视化接口测试

### 5.1 Swagger UI

在浏览器中访问：
```
http://树莓派IP:8084/swagger-ui.html
```

**功能**：
- 查看所有 API 文档
- 在线测试 API 接口
- 查看请求/响应格式

### 5.2 Actuator 端点

| 端点 | 功能 |
|------|------|
| `/actuator/health` | 健康检查 |
| `/actuator/info` | 应用信息 |
| `/actuator/metrics` | 性能指标 |
| `/api-docs` | OpenAPI 文档 |

```bash
# 查看应用信息
curl http://localhost:8084/actuator/info

# 查看指标
curl http://localhost:8084/actuator/metrics
```

---

## 6. 数据库连接测试

### 6.1 测试 MariaDB 连接

```bash
# 进入数据库容器
docker exec -it edge-mariadb mariadb -u admin -p

# 输入密码：smart_home_admin_2024

# 查询设备表
USE smarthome_edge;
SELECT * FROM devices;
```

**预期输出**：应看到刚才注册的设备记录。

### 6.2 测试 Redis 连接

```bash
# 进入 Redis 容器
docker exec -it edge-redis redis-cli

# 测试 Redis
PING
# 预期：PONG

# 查看设备缓存
KEYS *
```

---

## 7. 测试脚本

创建一个一键测试脚本：

```bash
#!/bin/bash

PI_IP="localhost"
EDGE_PORT="8084"

echo "=== 边缘服务测试套件 ==="

echo ""
echo "1. 健康检查..."
curl -s http://${PI_IP}:${EDGE_PORT}/actuator/health | head -c 100

echo ""
echo ""
echo "2. 设备列表..."
curl -s http://${PI_IP}:${EDGE_PORT}/api/edge/devices

echo ""
echo ""
echo "3. 注册测试设备..."
curl -s -X POST http://${PI_IP}:${EDGE_PORT}/api/edge/devices/register \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"test-sensor-001","deviceName":"测试传感器","deviceType":"SENSOR","protocol":"MQTT","ipAddress":"192.168.1.102"}'

echo ""
echo ""
echo "4. 设备状态..."
curl -s http://${PI_IP}:${EDGE_PORT}/api/edge/devices/test-sensor-001/status

echo ""
echo ""
echo "5. 设备统计..."
curl -s http://${PI_IP}:${EDGE_PORT}/api/edge/devices/statistics

echo ""
echo ""
echo "=== 测试完成 ==="
```

保存为 `test-edge.sh`，运行：
```bash
chmod +x test-edge.sh
./test-edge.sh
```

---

## 故障排查

### 常见问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 容器启动失败 | 端口被占用 | `netstat -tlnp | grep 8084` 查看占用进程 |
| 健康检查失败 | 数据库连接失败 | 检查 MariaDB 容器状态和日志 |
| API 返回 500 | 代码异常 | 查看 `docker logs edge-service` |
| gRPC 无法连接 | 端口未开放 | 检查防火墙配置 |

### 日志排查命令

```bash
# 查看边缘服务完整日志
docker compose -f docker-compose.pi.edge-only.yml logs edge-service

# 查看最近 50 行日志
docker compose -f docker-compose.pi.edge-only.yml logs --tail=50 edge-service

# 持续跟踪日志
docker compose -f docker-compose.pi.edge-only.yml logs -f edge-service

# 查看数据库日志（排查连接问题）
docker compose -f docker-compose.pi.edge-only.yml logs mariadb
```
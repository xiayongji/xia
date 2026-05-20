# ========================================
# 智能家居云边协同系统 - 启动指南
# ========================================

## 📋 系统架构

这是一个基于微服务架构的智能家居云边协同系统，包含以下组件：

### 基础设施层
- **MySQL 8.0** (端口5455) - 主数据库
- **Redis 7.0** (端口6379) - 缓存服务

### 微服务层
1. **Eureka Server** (端口8761) - 服务注册中心
2. **API Gateway** (端口8080) - API网关
3. **User Service** (端口8082) - 用户服务
4. **Device Service** (端口8081) - 设备服务
5. **Scene Service** (端口8083) - 场景服务
6. **Analytics Service** (端口8085) - 分析服务
7. **Edge Service** (端口8084) - 边缘网关服务

### 前端应用
- **Vue.js Frontend** (端口5173) - 用户界面

## 🚀 快速启动

### 方法一：使用一键启动脚本（推荐）

```powershell
cd c:\Users\user\Desktop\毕业设计\smart-home-microservices
.\start-all-services.bat
```

选择选项 `1` 启动所有服务。

### 方法二：手动启动

#### 步骤1：启动基础设施

```powershell
# 使用Docker启动MySQL和Redis
docker run -d --name smart-home-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=smarthome \
  -p 5455:3306 \
  mysql:8.0

docker run -d --name smart-home-redis \
  -p 6379:6379 \
  redis:7.0
```

#### 步骤2：初始化数据库

```powershell
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS smarthome_edge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 初始化表结构
mysql -u root -p infini_rag_flow -D smarthome_edge < smart-home-microservices\edge-service\sql\init-edge-database.sql

# 插入测试数据
mysql -u root -p infini_rag_flow -D smarthome_edge < smart-home-microservices\edge-service\sql\insert-all-test-devices.sql
```

#### 步骤3：启动Eureka Server

```powershell
cd smart-home-microservices\eureka-server
mvn spring-boot:run
```

等待看到以下日志：
```
Started EurekaServerApplication in X.XX seconds
```

#### 步骤4：启动微服务（并行或依次）

```powershell
# 在不同的终端窗口中启动

# 终端1：设备服务
cd smart-home-microservices\device-service
mvn spring-boot:run

# 终端2：用户服务
cd smart-home-microservices\user-service
mvn spring-boot:run

# 终端3：场景服务
cd smart-home-microservices\scene-service
mvn spring-boot:run

# 终端4：边缘服务
cd smart-home-microservices\edge-service
mvn spring-boot:run

# 终端5：分析服务
cd smart-home-microservices\analytics-service
mvn spring-boot:run
```

#### 步骤5：启动API网关

```powershell
cd smart-home-microservices\api-gateway
mvn spring-boot:run
```

#### 步骤6：启动前端应用

```powershell
# 如果使用Vue CLI
cd smart-home-microservices
npm install
npm run dev

# 或者使用预编译的dist
cd dist
python -m http.server 5173
```

## 🌐 访问地址

启动完成后，可以通过以下地址访问：

| 服务 | 地址 | 说明 |
|------|------|------|
| Eureka Dashboard | http://localhost:8761 | 查看所有注册的服务 |
| API Gateway | http://localhost:8080 | API统一入口 |
| 前端应用 | http://localhost:5173 | 用户界面 |
| 边缘服务 | http://localhost:8084 | 边缘网关API |
| Swagger UI | http://localhost:8080/swagger-ui.html | API文档 |

## ✅ 验证系统运行

### 1. 检查Eureka服务注册

打开浏览器访问：http://localhost:8761

应该能看到以下服务已注册：
- DEVICE-SERVICE (8081)
- USER-SERVICE (8082)
- SCENE-SERVICE (8083)
- EDGE-SERVICE (8084)
- ANALYTICS-SERVICE (8085)

### 2. 测试API网关

```bash
# 测试设备服务
curl http://localhost:8080/api/devices

# 测试用户服务
curl http://localhost:8080/api/user/health

# 测试边缘服务
curl http://localhost:8084/api/edge/health
```

### 3. 检查边缘服务数据库

```bash
mysql -u root -p infini_rag_flow -D smarthome_edge -e "SELECT * FROM edge_device;"
```

应该能看到测试设备数据。

### 4. 前端登录测试

1. 打开浏览器访问：http://localhost:5173
2. 使用默认账号登录（如果已配置）
3. 查看Dashboard中的设备状态

## 🛠️ 常用操作

### 查看服务状态

```powershell
# 使用启动脚本检查状态
.\start-all-services.bat
# 选择选项 7
```

### 查看日志

```powershell
# 查看Eureka日志
type smart-home-microservices\eureka-server\logs\*.log

# 查看边缘服务日志
type smart-home-microservices\edge-service\logs\*.log
```

### 重启服务

```powershell
# 停止所有Java进程
taskkill /F /IM java.exe

# 重新启动
.\start-all-services.bat
```

### 清理环境

```powershell
# 停止所有Docker容器
docker stop $(docker ps -aq)

# 清理Docker资源
docker system prune -f

# 清理端口占用
netstat -ano | findstr ":8084"
taskkill /PID <PID> /F
```

## 🐛 常见问题排查

### 问题1：端口被占用

**错误信息**：`Port 8084 is already in use`

**解决方案**：
```powershell
# 查找占用端口的进程
netstat -ano | findstr ":8084"

# 结束进程
taskkill /PID <进程ID> /F
```

### 问题2：MySQL连接失败

**错误信息**：`Connection refused to MySQL on port 5455`

**解决方案**：
```powershell
# 检查Docker是否运行
docker ps

# 启动MySQL容器
docker start smart-home-mysql

# 或使用本地MySQL
net start MySQL
```

### 问题3：Eureka注册不上

**错误信息**：`Cannot execute request on any known server`

**解决方案**：
1. 确认Eureka Server已启动（端口8761）
2. 检查微服务的Eureka配置
3. 查看微服务日志中的错误信息

### 问题4：前端无法访问后端API

**错误信息**：`Failed to load resource: net::ERR_CONNECTION_REFUSED`

**解决方案**：
1. 确认API Gateway已启动（端口8080）
2. 检查CORS配置
3. 查看浏览器控制台错误信息

### 问题5：边缘服务设备状态offline

**解决方案**：
```powershell
# 1. 初始化数据库
.\start-all-services.bat
# 选择选项 9

# 2. 重启边缘服务
cd smart-home-microservices\edge-service
mvn spring-boot:run
```

## 📊 性能监控

### 查看服务指标

```bash
# Eureka健康检查
curl http://localhost:8761/health

# 边缘服务指标
curl http://localhost:8084/api/edge/metrics/latency

# 设备统计
curl http://localhost:8084/api/edge/devices/statistics
```

### 查看Docker容器状态

```bash
# 查看运行中的容器
docker ps

# 查看容器日志
docker logs smart-home-mysql -f

# 查看资源使用
docker stats
```

## 🔧 配置说明

### 数据库配置

编辑 `application.yml` 修改数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:5455/smarthome_edge
    username: root
    password: infini_rag_flow
```

### Redis配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### Eureka配置

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## 📝 维护指南

### 定期备份数据库

```bash
# 备份边缘服务数据库
mysqldump -u root -p infini_rag_flow smarthome_edge > backup_$(date +%Y%m%d).sql
```

### 清理日志文件

```powershell
# 清理7天前的日志
forfiles /p "smart-home-microservices" /s /m "*.log" /d -7 /c "cmd /c del @path"
```

### 更新边缘服务

```powershell
cd smart-home-microservices\edge-service
git pull
mvn clean package -DskipTests
mvn spring-boot:run
```

## 🎯 下一步

1. ✅ 启动所有服务
2. ✅ 验证系统运行
3. 🔧 根据需要修改配置
4. 📊 配置监控和日志
5. 🚀 开始使用系统

## 📞 技术支持

如有问题，请检查：
1. 日志文件
2. 服务端口占用情况
3. Docker容器状态
4. 数据库连接配置

---

祝您使用愉快！🎉

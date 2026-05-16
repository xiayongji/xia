# 智能家居管理系统

基于Spring Boot微服务架构的智能家居管理系统，提供设备管理、场景控制、用户服务和数据分析四大核心模块。

## 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.2 + Spring Cloud 2023
- **注册中心**: Netflix Eureka
- **API网关**: Spring Cloud Gateway
- **数据库**: MySQL 8.0+
- **缓存**: Redis
- **消息队列**: RabbitMQ
- **认证**: JWT
- **规则引擎**: Drools

### 前端技术栈
- **框架**: Vue 3 + Element Plus
- **图表**: ECharts
- **路由**: Vue Router
- **状态管理**: Pinia

## 微服务模块

| 服务名称 | 端口 | 说明 |
| :--- | :--- | :--- |
| eureka-server | 8761 | 服务注册中心 |
| api-gateway | 8080 | API网关 |
| device-service | 8081 | 设备管理服务 |
| scene-service | 8082 | 场景控制服务 |
| user-service | 8083 | 用户服务 |
| analytics-service | 8084 | 数据分析服务 |

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 7.0+
- RabbitMQ 3.10+
- Maven 3.8+

### 数据库配置

1. 创建数据库用户和权限
```sql
CREATE USER 'smarthome'@'localhost' IDENTIFIED BY 'smarthome@123';
GRANT ALL PRIVILEGES ON device_db.* TO 'smarthome'@'localhost';
GRANT ALL PRIVILEGES ON scene_db.* TO 'smarthome'@'localhost';
GRANT ALL PRIVILEGES ON user_db.* TO 'smarthome'@'localhost';
GRANT ALL PRIVILEGES ON analytics_db.* TO 'smarthome'@'localhost';
FLUSH PRIVILEGES;
```

2. 执行数据库初始化脚本
```bash
mysql -u root -p < smart_home.sql
```

### 启动服务

```bash
# 方式一：使用启动脚本
./start-all.sh

# 方式二：逐个启动
cd eureka-server && mvn spring-boot:run
cd device-service && mvn spring-boot:run
cd scene-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd analytics-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

### 服务访问

| 服务 | URL |
| :--- | :--- |
| Eureka控制台 | http://localhost:8761 |
| API网关 | http://localhost:8080 |
| 设备管理API | http://localhost:8080/api/devices |
| 场景控制API | http://localhost:8080/api/scenes |
| 用户服务API | http://localhost:8080/api/user |
| 数据分析API | http://localhost:8080/api/analytics |

## API接口

### 用户认证
- `POST /api/user/auth/login` - 用户登录
- `POST /api/user/auth/register` - 用户注册
- `POST /api/user/auth/logout` - 用户登出

### 设备管理
- `GET /api/devices` - 获取设备列表
- `POST /api/devices` - 注册新设备
- `GET /api/devices/{id}` - 获取设备详情
- `PUT /api/devices/{id}` - 更新设备信息
- `DELETE /api/devices/{id}` - 删除设备
- `POST /api/devices/{id}/control` - 控制设备

### 场景控制
- `GET /api/scenes` - 获取场景列表
- `POST /api/scenes` - 创建场景
- `GET /api/scenes/{id}` - 获取场景详情
- `PUT /api/scenes/{id}` - 更新场景
- `DELETE /api/scenes/{id}` - 删除场景
- `POST /api/scenes/{id}/execute` - 执行场景

### 数据分析
- `GET /api/analytics/energy` - 获取能耗统计
- `GET /api/analytics/anomaly` - 获取异常检测结果
- `GET /api/analytics/behavior` - 获取用户行为分析

## 核心功能

### 1. 设备管理
- 多协议设备统一接入（WiFi、蓝牙、ZigBee）
- 设备心跳机制，实时监控设备状态
- 设备影子技术，实现期望状态与实际状态同步

### 2. 场景控制
- 规则引擎，支持时间、设备、环境触发条件
- 动作编排器，支持顺序、并行、条件分支执行
- 场景执行日志和状态追踪

### 3. 用户服务
- JWT无状态认证
- RBAC权限模型，支持角色-权限分层授权
- 操作日志持久化

### 4. 数据分析
- 能耗统计与趋势分析
- 设备异常检测（Z-score统计方法）
- 用户行为模式分析

## 项目结构

```
smart-home-microservices/
├── eureka-server/          # 注册中心
├── api-gateway/            # API网关
├── device-service/         # 设备管理服务
│   ├── controller/         # REST API控制层
│   ├── service/            # 业务逻辑层
│   ├── repository/         # 数据访问层
│   ├── entity/             # 数据库实体
│   ├── dto/                # 数据传输对象
│   ├── adapter/            # 协议适配器
│   └── config/             # 配置类
├── scene-service/          # 场景控制服务
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── engine/             # 规则引擎
│   └── orchestrator/       # 动作编排器
├── user-service/           # 用户服务
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── security/           # 安全认证
│   └── rbac/               # RBAC权限管理
└── analytics-service/      # 数据分析服务
    ├── controller/
    ├── service/
    ├── repository/
    ├── entity/
    ├── dto/
    └── algorithm/          # 异常检测算法
```

## 配置说明

各服务配置文件位于 `src/main/resources/application.yml`

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/{database}?useSSL=false&serverTimezone=UTC
    username: root
    password: root
```

### Eureka配置
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### JWT配置
```yaml
jwt:
  secret: smart-home-jwt-secret-key-2024
  expiration: 86400000  # 24小时
```

## 开发说明

### 添加新设备协议
1. 在 `device-service/adapter/` 下创建新的协议适配器
2. 实现 `DeviceAdapter` 接口
3. 在配置类中注册适配器

### 添加新场景规则
1. 在 `scene-service/engine/rules/` 下创建Drools规则文件
2. 定义规则条件和动作
3. 在规则引擎中注册规则

## 许可证

MIT License
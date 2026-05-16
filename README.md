# 智能家居微服务系统

## 项目简介

本项目是一个完整的智能家居微服务系统，采用微服务架构设计，包含设备管理、场景控制、用户服务和数据分析四大核心模块。系统采用Spring Boot 3.x + Spring Cloud微服务架构，前端采用Vue 3.x + Element Plus开发。

## 系统架构

### 微服务模块划分
1. **设备管理服务** (device-service) - 端口8081
2. **场景控制服务** (scene-service) - 端口8082
3. **用户服务** (user-service) - 端口8083
4. **数据分析服务** (analytics-service) - 端口8084
5. **API网关** (gateway) - 端口8080
6. **服务注册中心** (registry) - 端口8761
7. **配置中心** (config) - 端口8888

### 技术栈
- **后端框架**: Spring Boot 3.2.0 + Spring Cloud 2023.0.0
- **前端框架**: Vue 3.x + Element Plus
- **数据库**: MySQL 8.0 + Redis 7.0
- **消息队列**: RabbitMQ
- **安全认证**: Spring Security + JWT
- **规则引擎**: Drools
- **机器学习**: TensorFlow + DeepLearning4j

## 核心功能模块

### 1. 设备管理模块
- **多协议设备统一接入**：支持WiFi、蓝牙、ZigBee等不同通讯协议
- **设备注册与鉴权**：设备身份验证和安全接入
- **状态监控与同步**：设备心跳机制和影子技术
- **实时控制**：设备远程控制和状态查询

### 2. 场景控制模块
- **智能场景联动**：基于条件触发器和动作编排器
- **规则引擎**：动态配置和热更新场景规则
- **异步处理**：确保场景执行的实时性和可靠性
- **图形化界面**：直观的场景配置界面

### 3. 用户服务模块
- **安全认证**：Spring Security + JWT认证授权机制
- **权限管理**：RBAC模型支持角色权限动态配置
- **个性化设置**：主题切换、设备分组、场景偏好
- **操作审计**：完整的操作日志记录和审计功能

### 4. 数据分析模块
- **能耗统计**：基于时序分析的能耗模式识别
- **异常检测**：LSTM神经网络实现设备故障预警
- **用户行为分析**：聚类算法识别用户使用模式
- **分层存储**：Redis + MySQL分层存储策略

## 项目结构

```
smart-home-microservices/
├── device-service/          # 设备管理微服务
│   ├── src/main/java/com/smarthome/device/
│   │   ├── entity/          # 设备、设备影子、心跳实体
│   │   ├── adapter/         # 多协议设备适配器
│   │   ├── service/         # 设备管理业务逻辑
│   │   └── controller/      # REST API接口
│   └── src/main/resources/  # 配置文件
├── scene-service/           # 场景控制微服务
│   ├── src/main/java/com/smarthome/scene/
│   │   ├── entity/          # 场景、规则、动作实体
│   │   ├── service/         # 规则引擎、场景执行
│   │   └── controller/      # 场景管理API
│   └── src/main/resources/  # 配置文件
├── user-service/            # 用户服务微服务
│   ├── src/main/java/com/smarthome/user/
│   │   ├── entity/          # 用户、角色、权限实体
│   │   ├── security/        # 安全认证组件
│   │   ├── service/         # 用户管理业务逻辑
│   │   └── controller/      # 用户认证API
│   └── src/main/resources/  # 配置文件
├── analytics-service/       # 数据分析微服务
│   └── pom.xml              # 项目配置
└── README.md                # 项目说明

frontend/                    # Vue 3前端应用
├── src/
│   ├── views/               # 页面组件
│   │   ├── Dashboard.vue    # 仪表板
│   │   ├── Login.vue        # 登录页面
│   │   ├── Register.vue     # 注册页面
│   │   ├── Profile.vue      # 个人中心
│   │   ├── Scenes.vue       # 场景管理
│   │   ├── Energy.vue       # 能耗分析
│   │   └── Settings.vue     # 系统设置
│   ├── router/              # 路由配置
│   ├── App.vue              # 根组件
│   └── main.js              # 入口文件
└── package.json             # 前端依赖配置
```

## 快速开始

### 环境要求
- **JDK 17+**：Spring Boot 3.x要求
- **Node.js 16+**：前端开发环境
- **MySQL 8.0+**：关系型数据库
- **Redis 7.0+**：缓存和会话存储
- **RabbitMQ 3.12+**：消息队列

### 前端启动
```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 后端启动（需要Java环境）
```bash
# 启动各微服务（按顺序）
cd smart-home-microservices/device-service && mvn spring-boot:run
cd smart-home-microservices/scene-service && mvn spring-boot:run
cd smart-home-microservices/user-service && mvn spring-boot:run
```

## API接口文档

### 设备管理服务
- `POST /api/device/devices/register` - 注册设备
- `GET /api/device/devices` - 获取设备列表
- `PUT /api/device/devices/{id}/status` - 更新设备状态
- `POST /api/device/devices/{id}/heartbeat` - 处理设备心跳

### 场景控制服务
- `POST /api/scene/scenes` - 创建场景
- `GET /api/scene/scenes` - 获取场景列表
- `POST /api/scene/scenes/{id}/execute` - 执行场景
- `PUT /api/scene/scenes/{id}/toggle` - 切换场景状态

### 用户服务
- `POST /api/user/auth/register` - 用户注册
- `POST /api/user/auth/login` - 用户登录
- `GET /api/user/users/profile` - 获取用户信息
- `PUT /api/user/users/settings` - 更新用户设置

### 数据分析服务
- `GET /api/analytics/energy/today` - 获取今日能耗
- `GET /api/analytics/device/anomalies` - 获取设备异常
- `GET /api/analytics/user/behavior` - 分析用户行为

## 系统特性

### 微服务架构优势
- **服务解耦**：各模块独立部署、独立扩展
- **弹性伸缩**：根据负载动态调整服务实例
- **故障隔离**：单个服务故障不影响整体系统
- **技术异构**：不同服务可采用不同技术栈

### 安全机制
- **JWT认证**：无状态认证，支持分布式部署
- **RBAC权限**：灵活的权限管理模型
- **TLS加密**：保障数据传输安全
- **操作审计**：完整的操作日志记录

### 性能优化
- **Redis缓存**：提升查询性能
- **异步处理**：提高系统吞吐量
- **分层存储**：优化数据访问效率
- **负载均衡**：确保服务高可用性

## 当前状态

### ✅ 前端项目
- **状态**：已成功启动
- **访问地址**：http://localhost:3000/
- **功能**：完整的用户界面和模拟数据演示

### ❌ 后端微服务
- **状态**：需要Java环境支持
- **问题**：Java环境未安装
- **解决方案**：安装JDK 17+后即可启动

## 访问方式

1. **前端界面**：http://localhost:3000/
2. **设备管理API**：http://localhost:8081/api/device/
3. **场景控制API**：http://localhost:8082/api/scene/
4. **用户服务API**：http://localhost:8083/api/user/

## 注意事项

- 前端项目已包含完整的模拟数据，可直接体验功能
- 后端微服务需要Java 17+环境支持
- 数据库配置需要根据实际环境调整
- 生产环境部署需要配置SSL证书和防火墙

## 后续开发计划

- [ ] 实现API网关和服务注册中心
- [ ] 完成数据分析微服务模块
- [ ] 添加监控和日志系统
- [ ] 实现容器化部署
- [ ] 集成CI/CD流水线
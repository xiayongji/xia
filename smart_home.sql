-- 智能家居管理系统数据库结构
-- 生成时间：2026-04-13

-- 创建设备管理数据库
CREATE DATABASE IF NOT EXISTS device_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建场景管理数据库
CREATE DATABASE IF NOT EXISTS scene_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户管理数据库
CREATE DATABASE IF NOT EXISTS user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建数据分析数据库
CREATE DATABASE IF NOT EXISTS analytics_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ========== 设备管理数据库 ==========
USE device_db;

-- 设备表
CREATE TABLE IF NOT EXISTS devices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id VARCHAR(64) UNIQUE NOT NULL COMMENT '设备唯一标识',
    name VARCHAR(100) NOT NULL COMMENT '设备名称',
    type VARCHAR(50) NOT NULL COMMENT '设备类型',
    protocol VARCHAR(30) NOT NULL COMMENT '通信协议(wifi/bluetooth/zigbee)',
    status VARCHAR(20) DEFAULT 'offline' COMMENT '设备状态(online/offline/warning)',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    mac_address VARCHAR(50) UNIQUE COMMENT 'MAC地址',
    firmware_version VARCHAR(50) COMMENT '固件版本',
    manufacturer VARCHAR(100) COMMENT '制造商',
    model VARCHAR(100) COMMENT '设备型号',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    last_heartbeat DATETIME COMMENT '最后心跳时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 设备心跳表
CREATE TABLE IF NOT EXISTS device_heartbeats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    timestamp DATETIME NOT NULL COMMENT '心跳时间',
    status VARCHAR(20) COMMENT '设备状态',
    cpu_usage DECIMAL(5,2) COMMENT 'CPU使用率',
    memory_usage DECIMAL(5,2) COMMENT '内存使用率',
    temperature DECIMAL(5,2) COMMENT '设备温度',
    network_status VARCHAR(20) COMMENT '网络状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备心跳表';

-- 设备影子表
CREATE TABLE IF NOT EXISTS device_shadows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id VARCHAR(64) UNIQUE NOT NULL COMMENT '设备ID',
    desired_state TEXT COMMENT '期望状态(JSON)',
    reported_state TEXT COMMENT '上报状态(JSON)',
    last_updated DATETIME NOT NULL COMMENT '最后更新时间',
    version INT DEFAULT 1 COMMENT '版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备影子表';

-- ========== 场景管理数据库 ==========
USE scene_db;

-- 场景表
CREATE TABLE IF NOT EXISTS scenes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene_id VARCHAR(64) UNIQUE NOT NULL COMMENT '场景唯一标识',
    name VARCHAR(100) NOT NULL COMMENT '场景名称',
    description VARCHAR(500) COMMENT '场景描述',
    enabled BOOLEAN DEFAULT true COMMENT '是否启用',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景表';

-- 场景规则表
CREATE TABLE IF NOT EXISTS scene_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id VARCHAR(64) UNIQUE NOT NULL COMMENT '规则唯一标识',
    type VARCHAR(30) NOT NULL COMMENT '规则类型(time/device/environment)',
    condition TEXT COMMENT '触发条件',
    scene_id BIGINT NOT NULL COMMENT '所属场景ID',
    priority INT DEFAULT 1 COMMENT '优先级',
    enabled BOOLEAN DEFAULT true COMMENT '是否启用',
    FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景规则表';

-- 场景动作表
CREATE TABLE IF NOT EXISTS scene_actions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    action_id VARCHAR(64) UNIQUE NOT NULL COMMENT '动作唯一标识',
    device_id VARCHAR(64) NOT NULL COMMENT '目标设备ID',
    command VARCHAR(100) NOT NULL COMMENT '执行命令',
    parameters TEXT COMMENT '命令参数(JSON)',
    order_index INT DEFAULT 0 COMMENT '执行顺序',
    rule_id BIGINT NOT NULL COMMENT '所属规则ID',
    type VARCHAR(30) DEFAULT 'device_control' COMMENT '动作类型',
    delay_seconds INT DEFAULT 0 COMMENT '延迟秒数',
    enabled BOOLEAN DEFAULT true COMMENT '是否启用',
    FOREIGN KEY (rule_id) REFERENCES scene_rules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景动作表';

-- 场景执行记录表
CREATE TABLE IF NOT EXISTS scene_executions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene_id VARCHAR(64) NOT NULL COMMENT '场景ID',
    trigger_type VARCHAR(50) COMMENT '触发类型',
    trigger_condition TEXT COMMENT '触发条件',
    status VARCHAR(20) DEFAULT 'running' COMMENT '执行状态',
    total_actions INT DEFAULT 0 COMMENT '总动作数',
    success_actions INT DEFAULT 0 COMMENT '成功动作数',
    failed_actions INT DEFAULT 0 COMMENT '失败动作数',
    execution_log TEXT COMMENT '执行日志',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景执行记录表';

-- ========== 用户管理数据库 ==========
USE user_db;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密密码',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    full_name VARCHAR(100) COMMENT '全名',
    role_id BIGINT COMMENT '角色ID',
    enabled BOOLEAN DEFAULT true COMMENT '是否启用',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    last_login DATETIME COMMENT '最后登录时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) UNIQUE NOT NULL COMMENT '角色名称',
    description VARCHAR(200) COMMENT '角色描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS user_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(100) UNIQUE NOT NULL COMMENT '权限名称',
    description VARCHAR(200) COMMENT '权限描述',
    resource VARCHAR(100) COMMENT '资源',
    action VARCHAR(50) COMMENT '操作'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES user_roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES user_permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 用户设置表
CREATE TABLE IF NOT EXISTS user_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL COMMENT '用户ID',
    theme VARCHAR(20) DEFAULT 'light' COMMENT '主题',
    language VARCHAR(10) DEFAULT 'zh-CN' COMMENT '语言',
    timezone VARCHAR(50) DEFAULT 'Asia/Shanghai' COMMENT '时区',
    notifications_enabled BOOLEAN DEFAULT true COMMENT '通知开关',
    email_notifications BOOLEAN DEFAULT true COMMENT '邮件通知',
    push_notifications BOOLEAN DEFAULT true COMMENT '推送通知',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设置表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '操作用户',
    operation VARCHAR(100) NOT NULL COMMENT '操作类型',
    resource VARCHAR(100) COMMENT '操作资源',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(255) COMMENT '用户代理',
    success BOOLEAN DEFAULT true COMMENT '是否成功',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME NOT NULL COMMENT '操作时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ========== 数据分析数据库 ==========
USE analytics_db;

-- 能耗数据表
CREATE TABLE IF NOT EXISTS energy_consumption (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    device_name VARCHAR(100) COMMENT '设备名称',
    power DECIMAL(10,3) NOT NULL COMMENT '功率(W)',
    energy DECIMAL(10,3) NOT NULL COMMENT '能耗(kWh)',
    timestamp DATETIME NOT NULL COMMENT '记录时间',
    unit VARCHAR(10) DEFAULT 'kWh' COMMENT '单位',
    device_type VARCHAR(50) COMMENT '设备类型'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='能耗数据表';

-- 异常检测表
CREATE TABLE IF NOT EXISTS anomaly_detection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    device_name VARCHAR(100) COMMENT '设备名称',
    anomaly_type VARCHAR(50) NOT NULL COMMENT '异常类型',
    confidence DECIMAL(5,2) NOT NULL COMMENT '置信度',
    details TEXT COMMENT '异常详情',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '处理状态',
    detected_at DATETIME NOT NULL COMMENT '检测时间',
    resolved_at DATETIME COMMENT '处理时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常检测表';

-- 用户行为表
CREATE TABLE IF NOT EXISTS user_behavior (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    behavior_type VARCHAR(50) NOT NULL COMMENT '行为类型',
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    device_name VARCHAR(100) COMMENT '设备名称',
    timestamp DATETIME NOT NULL COMMENT '行为时间',
    action VARCHAR(100) COMMENT '执行动作',
    category VARCHAR(50) COMMENT '行为分类'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为表';

-- ========== 创建索引 ==========
USE device_db;
CREATE INDEX idx_devices_device_id ON devices(device_id);
CREATE INDEX idx_devices_status ON devices(status);
CREATE INDEX idx_devices_protocol ON devices(protocol);
CREATE INDEX idx_device_shadows_device_id ON device_shadows(device_id);

USE scene_db;
CREATE INDEX idx_scenes_scene_id ON scenes(scene_id);
CREATE INDEX idx_scene_rules_scene_id ON scene_rules(scene_id);
CREATE INDEX idx_scene_actions_rule_id ON scene_actions(rule_id);

USE user_db;
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_operation_logs_username ON operation_logs(username);

USE analytics_db;
CREATE INDEX idx_energy_consumption_device_id ON energy_consumption(device_id);
CREATE INDEX idx_energy_consumption_timestamp ON energy_consumption(timestamp);
CREATE INDEX idx_anomaly_detection_device_id ON anomaly_detection(device_id);
CREATE INDEX idx_anomaly_detection_status ON anomaly_detection(status);
CREATE INDEX idx_user_behavior_user_id ON user_behavior(user_id);

-- ========== 插入初始数据 ==========
USE user_db;
INSERT IGNORE INTO user_roles (id, role_name, description) VALUES
(1, 'ROLE_ADMIN', '系统管理员'),
(2, 'ROLE_USER', '普通用户');

INSERT IGNORE INTO user_permissions (id, permission_name, description, resource, action) VALUES
(1, 'device:read', '读取设备信息', 'device', 'read'),
(2, 'device:write', '修改设备信息', 'device', 'write'),
(3, 'device:control', '控制设备', 'device', 'control'),
(4, 'scene:read', '读取场景信息', 'scene', 'read'),
(5, 'scene:write', '修改场景信息', 'scene', 'write'),
(6, 'scene:execute', '执行场景', 'scene', 'execute'),
(7, 'user:read', '读取用户信息', 'user', 'read'),
(8, 'user:write', '修改用户信息', 'user', 'write'),
(9, 'analytics:read', '读取分析数据', 'analytics', 'read'),
(10, 'rbac:manage', '管理权限', 'rbac', 'manage');

INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(2, 1), (2, 3), (2, 4), (2, 6), (2, 7), (2, 9);

-- ========== 查看数据库结构 ==========
SELECT '=== device_db ===' AS database_name;
USE device_db;
SHOW TABLES;

SELECT '=== scene_db ===' AS database_name;
USE scene_db;
SHOW TABLES;

SELECT '=== user_db ===' AS database_name;
USE user_db;
SHOW TABLES;

SELECT '=== analytics_db ===' AS database_name;
USE analytics_db;
SHOW TABLES;
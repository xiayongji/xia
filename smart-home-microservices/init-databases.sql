-- 智能家居系统数据库初始化脚本
-- 请确保MySQL服务已启动，并使用root用户登录

-- 创建数据库
CREATE DATABASE IF NOT EXISTS smarthome DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smarthome_device DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smarthome_scene DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smarthome_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smarthome_analytics DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smarthome_edge DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smarthome_multimodal DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ========== 设备管理服务数据库 ==========
USE smarthome_device;

CREATE TABLE IF NOT EXISTS devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(100) UNIQUE NOT NULL,
    device_name VARCHAR(100) NOT NULL,
    device_type VARCHAR(50),
    protocol VARCHAR(20),
    status VARCHAR(20) DEFAULT 'OFFLINE',
    online BOOLEAN DEFAULT FALSE,
    user_id BIGINT,
    location VARCHAR(100),
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    firmware_version VARCHAR(50),
    last_heartbeat TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    properties JSON,
    INDEX idx_device_id (device_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_type (device_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS device_heartbeats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20),
    response_time_ms INT,
    INDEX idx_device_id (device_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS device_shadows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(100) UNIQUE NOT NULL,
    desired_state JSON,
    reported_state JSON,
    delta JSON,
    version BIGINT DEFAULT 1,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_device_id (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 场景控制服务数据库 ==========
USE smarthome_scene;

CREATE TABLE IF NOT EXISTS scenes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    user_id BIGINT,
    trigger_type VARCHAR(50),
    trigger_conditions JSON,
    actions JSON,
    enabled BOOLEAN DEFAULT TRUE,
    priority INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_executed_at TIMESTAMP NULL,
    execution_count INT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_enabled (enabled),
    INDEX idx_trigger_type (trigger_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS scene_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scene_id BIGINT,
    rule_name VARCHAR(100) NOT NULL,
    rule_type VARCHAR(50),
    rule_condition TEXT,
    rule_action TEXT,
    priority INT DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_scene_id (scene_id),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS scene_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scene_id BIGINT,
    trigger_type VARCHAR(50),
    trigger_source VARCHAR(50),
    status VARCHAR(20),
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    actions_executed INT,
    actions_total INT,
    result TEXT,
    error_message TEXT,
    INDEX idx_scene_id (scene_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 用户服务数据库 ==========
USE smarthome_user;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'USER',
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    theme VARCHAR(20) DEFAULT 'light',
    language VARCHAR(10) DEFAULT 'zh_CN',
    notification_enabled BOOLEAN DEFAULT TRUE,
    temperature_unit VARCHAR(1) DEFAULT 'C',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 数据分析服务数据库 ==========
USE smarthome_analytics;

CREATE TABLE IF NOT EXISTS sensor_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL,
    sensor_type VARCHAR(50),
    value DOUBLE,
    unit VARCHAR(20),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_id (device_id),
    INDEX idx_sensor_type (sensor_type),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS energy_consumption (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL,
    power_watts DOUBLE,
    voltage DOUBLE,
    current DOUBLE,
    energy_kwh DOUBLE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_id (device_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS anomaly_detections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL,
    anomaly_type VARCHAR(50),
    severity VARCHAR(20),
    score DOUBLE,
    description TEXT,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMP NULL,
    INDEX idx_device_id (device_id),
    INDEX idx_severity (severity),
    INDEX idx_detected_at (detected_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_behaviors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_id VARCHAR(100),
    action VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    context JSON,
    INDEX idx_user_id (user_id),
    INDEX idx_device_id (device_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 边缘网关服务数据库 ==========
USE smarthome_edge;

CREATE TABLE IF NOT EXISTS edge_devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(100) UNIQUE NOT NULL,
    device_name VARCHAR(100) NOT NULL,
    device_type VARCHAR(50),
    protocol VARCHAR(20),
    status VARCHAR(20) DEFAULT 'OFFLINE',
    local_ip VARCHAR(50),
    mac_address VARCHAR(50),
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_id (device_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS edge_commands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL,
    command VARCHAR(255) NOT NULL,
    status VARCHAR(20),
    response_time_ms INT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_id (device_id),
    INDEX idx_executed_at (executed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS edge_sync_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sync_type VARCHAR(50),
    status VARCHAR(20),
    items_synced INT,
    sync_time_ms INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sync_type (sync_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 多模态交互服务数据库 ==========
USE smarthome_multimodal;

CREATE TABLE IF NOT EXISTS behavior_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    input_type VARCHAR(50),
    intent VARCHAR(100),
    confidence DOUBLE,
    device_id VARCHAR(100),
    action VARCHAR(100),
    context JSON,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_input_type (input_type),
    INDEX idx_intent (intent),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_behavior_patterns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pattern_name VARCHAR(100),
    pattern_type VARCHAR(50),
    pattern_data JSON,
    confidence DOUBLE,
    frequency INT,
    last_observed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_pattern_type (pattern_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 插入示例数据 ==========

USE smarthome_device;
INSERT INTO devices (device_id, device_name, device_type, protocol, status, online, user_id, location, manufacturer) VALUES
('light_001', '客厅主灯', 'LIGHT', 'WIFI', 'ONLINE', TRUE, 1, '客厅', 'Philips'),
('light_002', '卧室灯', 'LIGHT', 'WIFI', 'ONLINE', TRUE, 1, '卧室', 'Philips'),
('light_003', '餐厅灯', 'LIGHT', 'WIFI', 'ONLINE', TRUE, 1, '餐厅', 'Philips'),
('ac_001', '客厅空调', 'AC', 'WIFI', 'ONLINE', TRUE, 1, '客厅', '格力'),
('ac_002', '卧室空调', 'AC', 'WIFI', 'ONLINE', TRUE, 1, '卧室', '格力'),
('curtain_001', '电动窗帘', 'CURTAIN', 'WIFI', 'ONLINE', TRUE, 1, '客厅', '杜亚'),
('camera_001', '安防摄像头', 'CAMERA', 'WIFI', 'ONLINE', TRUE, 1, '玄关', '海康威视'),
('sensor_temp_001', '温湿度传感器', 'SENSOR', 'ZIGBEE', 'ONLINE', TRUE, 1, '客厅', '小米'),
('sensor_motion_001', '人体红外传感器', 'SENSOR', 'ZIGBEE', 'ONLINE', TRUE, 1, '玄关', '小米'),
('lock_001', '智能门锁', 'LOCK', 'ZIGBEE', 'ONLINE', TRUE, 1, '门口', '德施曼');

USE smarthome_scene;
INSERT INTO scenes (name, description, trigger_type, enabled, priority, execution_count) VALUES
('晨起模式', '自动开启晨起流程：拉开窗帘、开启咖啡机、播放轻音乐', 'TIME', TRUE, 10, 156),
('离家模式', '一键离家：关闭所有灯光、空调切换节能模式、启动安防', 'MANUAL', TRUE, 9, 289),
('回家模式', '欢迎回家：玄关灯亮起、空调开启、播放欢迎语音', 'LOCATION', TRUE, 8, 203),
('晚间休息', '温馨晚间：客厅灯光调暗、空调调至舒适温度、播放舒缓音乐', 'TIME', TRUE, 7, 134),
('睡眠模式', '安心入眠：关闭所有灯光、空调低噪运行、夜灯开启', 'MANUAL', TRUE, 6, 98),
('影院模式', '影院级享受：灯光渐暗、窗帘关闭、音响环绕', 'MANUAL', TRUE, 5, 45),
('节能模式', '智能节能：根据作息自动调节设备功率、优化用电', 'AUTOMATIC', TRUE, 4, 67),
('安防模式', '全面安防：门窗传感器激活、视频监控开启', 'AUTOMATIC', TRUE, 3, 112);

USE smarthome_user;
INSERT INTO users (username, password, email, phone, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@smarthome.com', '13800138000', 'ADMIN'),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'user1@smarthome.com', '13800138001', 'USER'),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'user2@smarthome.com', '13800138002', 'USER');

INSERT INTO user_settings (user_id, theme, temperature_unit) VALUES
(1, 'dark', 'C'),
(2, 'light', 'C'),
(3, 'light', 'F');

USE smarthome_analytics;
INSERT INTO energy_consumption (device_id, power_watts, voltage, current, energy_kwh) VALUES
('light_001', 15.5, 220.5, 0.07, 0.015),
('light_002', 12.0, 220.3, 0.05, 0.012),
('ac_001', 850.0, 220.0, 3.86, 0.85),
('ac_002', 720.0, 220.2, 3.27, 0.72);

SELECT '所有数据库初始化完成！' AS message;

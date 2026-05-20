-- 智能家居边缘网关数据库初始化脚本
-- 数据库: smarthome_edge

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS smarthome_edge
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE smarthome_edge;

-- 创建设备表
CREATE TABLE IF NOT EXISTS edge_device (
    device_id VARCHAR(64) PRIMARY KEY COMMENT '设备ID',
    device_name VARCHAR(128) COMMENT '设备名称',
    device_type VARCHAR(64) COMMENT '设备类型',
    protocol VARCHAR(32) COMMENT '通信协议: WiFi/Bluetooth/ZigBee',
    status VARCHAR(32) COMMENT '设备状态: online/offline/error',
    location VARCHAR(128) COMMENT '设备位置',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_device_type (device_type),
    INDEX idx_protocol (protocol),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='边缘设备表';

-- 创建设备状态表
CREATE TABLE IF NOT EXISTS edge_device_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    status VARCHAR(32) COMMENT '设备状态: online/offline/error',
    device_type VARCHAR(64) COMMENT '设备类型',
    power DOUBLE DEFAULT 0.0 COMMENT '功率(W)',
    temperature DOUBLE COMMENT '温度(℃)',
    humidity DOUBLE COMMENT '湿度(%)',
    properties TEXT COMMENT '其他属性(JSON格式)',
    last_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_device_id (device_id),
    INDEX idx_last_update_time (last_update_time),
    INDEX idx_status (status),
    UNIQUE KEY uk_device_id (device_id),
    FOREIGN KEY (device_id) REFERENCES edge_device(device_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备状态表';

-- 创建设备命令历史表（可选，用于记录命令历史）
CREATE TABLE IF NOT EXISTS edge_command_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    command VARCHAR(255) NOT NULL COMMENT '执行的命令',
    result VARCHAR(64) COMMENT '执行结果: success/failed',
    message TEXT COMMENT '详细消息',
    response_time_ms INT COMMENT '响应时间(毫秒)',
    executed_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    INDEX idx_device_id (device_id),
    INDEX idx_executed_at (executed_at),
    INDEX idx_result (result),
    FOREIGN KEY (device_id) REFERENCES edge_device(device_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备命令历史表';

-- 插入示例设备数据（用于测试）
INSERT INTO edge_device (device_id, device_name, device_type, protocol, status, location) VALUES
    ('device-001', '智能灯泡-客厅', 'light', 'WiFi', 'online', 'living_room'),
    ('device-002', '智能插座-卧室', 'socket', 'WiFi', 'online', 'bedroom'),
    ('device-003', '温湿度传感器', 'sensor', 'ZigBee', 'online', 'living_room'),
    ('device-004', '蓝牙门锁', 'lock', 'Bluetooth', 'online', 'front_door'),
    ('device-005', '空调控制器', 'ac', 'WiFi', 'online', 'bedroom')
ON DUPLICATE KEY UPDATE device_name=VALUES(device_name);

-- 插入示例设备状态数据（用于测试）
INSERT INTO edge_device_status (device_id, status, device_type, power, temperature, humidity, properties) VALUES
    ('device-001', 'online', 'light', 15.5, NULL, NULL, '{"brightness": 80, "color": "warm"}'),
    ('device-002', 'online', 'socket', 120.0, NULL, NULL, '{"voltage": 220, "current": 0.5}'),
    ('device-003', 'online', 'sensor', 0.1, 25.3, 65.2, '{"accuracy": 0.5}'),
    ('device-004', 'online', 'lock', 0.2, NULL, NULL, '{"battery": 85}'),
    ('device-005', 'online', 'ac', 800.0, 24.0, NULL, '{"mode": "cooling", "fan_speed": "auto"}')
ON DUPLICATE KEY UPDATE
    status=VALUES(status),
    power=VALUES(power),
    temperature=VALUES(temperature),
    humidity=VALUES(humidity),
    properties=VALUES(properties);

-- 创建统计视图
CREATE OR REPLACE VIEW device_statistics AS
SELECT
    d.device_id,
    d.device_name,
    d.device_type,
    d.protocol,
    d.location,
    ds.status,
    ds.power,
    ds.temperature,
    ds.humidity,
    ds.last_update_time,
    CASE
        WHEN ds.last_update_time < DATE_SUB(NOW(), INTERVAL 5 MINUTE) THEN 'stale'
        ELSE 'fresh'
    END AS data_freshness
FROM edge_device d
LEFT JOIN edge_device_status ds ON d.device_id = ds.device_id;

-- 创建在线设备统计视图
CREATE OR REPLACE VIEW online_device_summary AS
SELECT
    device_type,
    COUNT(*) as total_count,
    SUM(CASE WHEN status = 'online' THEN 1 ELSE 0 END) as online_count,
    SUM(CASE WHEN status = 'offline' THEN 1 ELSE 0 END) as offline_count,
    AVG(power) as avg_power
FROM edge_device_status
GROUP BY device_type;

-- 创建索引以优化查询性能
CREATE INDEX IF NOT EXISTS idx_device_type_status ON edge_device_status(device_type, status);
CREATE INDEX IF NOT EXISTS idx_power ON edge_device_status(power);

-- 输出完成信息
SELECT '数据库初始化完成！' AS message;
SELECT '设备数量: ' AS info, COUNT(*) AS count FROM edge_device;
SELECT '在线设备数量: ' AS info, COUNT(*) AS count FROM edge_device_status WHERE status = 'online';

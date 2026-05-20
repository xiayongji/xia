-- 快速插入多个测试设备
-- 用于演示和测试

USE smarthome_edge;

-- 插入测试设备（包含您日志中的设备ID）
INSERT INTO edge_device (device_id, device_name, device_type, protocol, status, location) VALUES
    ('device-1779073453825-9716', '温湿度传感器', 'sensor', 'WiFi', 'online', 'living_room'),
    ('device-1779079690283-0983', '智能调光灯', 'light', 'WiFi', 'online', 'bedroom'),
    ('device-test-001', '智能插座-客厅', 'socket', 'WiFi', 'online', 'living_room'),
    ('device-test-002', '智能窗帘电机', 'curtain', 'ZigBee', 'online', 'bedroom'),
    ('device-test-003', '空调控制器', 'ac', 'WiFi', 'online', 'bedroom'),
    ('device-test-004', '智能门锁', 'lock', 'Bluetooth', 'online', 'front_door'),
    ('device-test-005', '烟雾报警器', 'sensor', 'ZigBee', 'online', 'kitchen')
ON DUPLICATE KEY UPDATE
    device_name = VALUES(device_name),
    status = 'online',
    updated_at = NOW();

-- 插入设备状态
INSERT INTO edge_device_status (device_id, status, device_type, power, temperature, humidity, properties) VALUES
    ('device-1779073453825-9716', 'online', 'sensor', 3.5, 25.2, 58.6, '{"battery": 92, "last_calibration": "2026-05-01"}'),
    ('device-1779079690283-0983', 'online', 'light', 15.0, NULL, NULL, '{"brightness": 75, "color_temp": 4000, "mode": "reading"}'),
    ('device-test-001', 'online', 'socket', 45.5, NULL, NULL, '{"voltage": 220, "current": 0.21, "energy_kwh": 12.5}'),
    ('device-test-002', 'online', 'curtain', 25.0, NULL, NULL, '{"position": 100, "mode": "auto"}'),
    ('device-test-003', 'online', 'ac', 850.0, 24.0, NULL, '{"mode": "cooling", "target_temp": 24, "fan_speed": "auto"}'),
    ('device-test-004', 'online', 'lock', 0.1, NULL, NULL, '{"battery": 88, "last_access": "2026-05-18 10:30:00"}'),
    ('device-test-005', 'online', 'sensor', 0.2, NULL, NULL, '{"battery": 95, "sensitivity": "high"}')
ON DUPLICATE KEY UPDATE
    status = 'online',
    last_update_time = NOW();

-- 显示插入结果
SELECT '=== 设备列表 ===' AS title;
SELECT device_id, device_name, device_type, status, location FROM edge_device;

SELECT '' AS '';
SELECT '=== 设备状态 ===' AS title;
SELECT device_id, status, device_type, power, temperature, humidity, last_update_time FROM edge_device_status;

SELECT '' AS '';
SELECT '=== 在线统计 ===' AS title;
SELECT status, COUNT(*) as count FROM edge_device_status GROUP BY status;

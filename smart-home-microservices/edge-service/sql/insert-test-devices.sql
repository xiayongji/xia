-- 插入测试设备数据
-- 用于解决前端设备状态显示为offline的问题

USE smarthome_edge;

-- 插入设备基本信息
INSERT INTO edge_device (device_id, device_name, device_type, protocol, status, location) VALUES
    ('device-1779073453825-9716', '智能传感器-客厅', 'sensor', 'WiFi', 'online', 'living_room'),
    ('device-1779079690283-0983', '智能灯泡-卧室', 'light', 'WiFi', 'online', 'bedroom')
ON DUPLICATE KEY UPDATE
    device_name = VALUES(device_name),
    status = VALUES(status);

-- 插入设备状态
INSERT INTO edge_device_status (device_id, status, device_type, power, temperature, humidity, properties) VALUES
    ('device-1779073453825-9716', 'online', 'sensor', 5.2, 24.5, 62.3, '{"battery": 95, "signal": "excellent"}'),
    ('device-1779079690283-0983', 'online', 'light', 12.0, NULL, NULL, '{"brightness": 80, "color": "warm_white", "mode": "normal"}')
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    power = VALUES(power),
    temperature = VALUES(temperature),
    humidity = VALUES(humidity),
    properties = VALUES(properties);

-- 验证插入结果
SELECT '设备注册成功！' AS message;
SELECT * FROM edge_device WHERE device_id IN ('device-1779073453825-9716', 'device-1779079690283-0983');
SELECT * FROM edge_device_status WHERE device_id IN ('device-1779073453825-9716', 'device-1779079690283-0983');

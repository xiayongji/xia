-- 通用设备注册脚本
-- 用于自动注册前端传递过来的设备

USE smarthome_edge;

-- 动态创建设备记录
-- 这个脚本会自动根据设备ID创建记录

DELIMITER $$

DROP PROCEDURE IF EXISTS RegisterDeviceIfNotExists $$

CREATE PROCEDURE RegisterDeviceIfNotExists(
    IN p_device_id VARCHAR(64),
    IN p_device_name VARCHAR(128),
    IN p_device_type VARCHAR(64),
    IN p_protocol VARCHAR(32),
    IN p_location VARCHAR(128),
    IN p_status VARCHAR(32)
)
BEGIN
    DECLARE device_exists INT DEFAULT 0;
    
    -- 检查设备是否已存在
    SELECT COUNT(*) INTO device_exists
    FROM edge_device
    WHERE device_id = p_device_id;
    
    -- 如果不存在，则插入
    IF device_exists = 0 THEN
        INSERT INTO edge_device (device_id, device_name, device_type, protocol, status, location)
        VALUES (p_device_id, p_device_name, p_device_type, p_protocol, p_status, p_location);
        
        INSERT INTO edge_device_status (device_id, status, device_type, power, temperature, humidity, properties)
        VALUES (p_device_id, p_status, p_device_type, 0.0, NULL, NULL, '{}');
        
        SELECT '设备注册成功' AS result;
    ELSE
        -- 如果存在，更新状态
        UPDATE edge_device
        SET status = p_status, updated_at = NOW()
        WHERE device_id = p_device_id;
        
        UPDATE edge_device_status
        SET status = p_status, last_update_time = NOW()
        WHERE device_id = p_device_id;
        
        SELECT '设备状态已更新' AS result;
    END IF;
END $$

DELIMITER ;

-- 测试：注册一个设备
CALL RegisterDeviceIfNotExists(
    'device-1779073453825-9716',
    '智能传感器-客厅',
    'sensor',
    'WiFi',
    'living_room',
    'online'
);

CALL RegisterDeviceIfNotExists(
    'device-1779079690283-0983',
    '智能灯泡-卧室',
    'light',
    'WiFi',
    'bedroom',
    'online'
);

-- 查询所有设备状态
SELECT '所有设备状态：' AS info;
SELECT d.device_id, d.device_name, d.device_type, ds.status, ds.power, ds.temperature, ds.humidity
FROM edge_device d
LEFT JOIN edge_device_status ds ON d.device_id = ds.device_id;

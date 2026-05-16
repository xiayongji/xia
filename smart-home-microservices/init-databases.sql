-- 创建智能家居系统数据库

-- 用户服务数据库
CREATE DATABASE IF NOT EXISTS smarthome_user 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- 设备服务数据库
CREATE DATABASE IF NOT EXISTS smarthome_device 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- 场景服务数据库
CREATE DATABASE IF NOT EXISTS smarthome_scene 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- 数据分析服务数据库
CREATE DATABASE IF NOT EXISTS smarthome_analytics 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权
CREATE USER IF NOT EXISTS 'smarthome'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON smarthome_user.* TO 'smarthome'@'localhost';
GRANT ALL PRIVILEGES ON smarthome_device.* TO 'smarthome'@'localhost';
GRANT ALL PRIVILEGES ON smarthome_scene.* TO 'smarthome'@'localhost';
GRANT ALL PRIVILEGES ON smarthome_analytics.* TO 'smarthome'@'localhost';
FLUSH PRIVILEGES;

SELECT '数据库创建完成' AS message;
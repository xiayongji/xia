#ifndef CONFIG_H
#define CONFIG_H

// ============================================================
// 每个 ESP32 需要单独修改以下配置
// ============================================================

// 设备 1: 客厅温湿度传感器
#define DEVICE_ID       "esp32-dht11-001"
#define DEVICE_NAME     "客厅温湿度传感器"
#define DEVICE_LOCATION "living_room"

// 设备 2: 卧室温湿度传感器 (烧录时取消下行注释，注释上行)
// #define DEVICE_ID       "esp32-dht11-002"
// #define DEVICE_NAME     "卧室温湿度传感器"
// #define DEVICE_LOCATION "bedroom"

// ============================================================
// WiFi 配置
// ============================================================
#define WIFI_SSID           "YOUR_WIFI_SSID"
#define WIFI_PASSWORD       "YOUR_WIFI_PASSWORD"
#define WIFI_RETRY_INTERVAL 10000
#define WIFI_MAX_RETRIES    30

// ============================================================
// 服务器地址配置
// ============================================================
#define DEVICE_SERVICE_HOST "192.168.1.100"
#define DEVICE_SERVICE_PORT 8081
#define EDGE_SERVICE_HOST   "192.168.1.100"
#define EDGE_SERVICE_PORT   8084

// ============================================================
// DHT11 传感器引脚配置
// ============================================================
#define DHT11_PIN         4
#define DHT11_READ_INTERVAL 2000

// ============================================================
// 数据上报间隔 (毫秒)
// ============================================================
#define STATUS_REPORT_INTERVAL 30000
#define HEARTBEAT_INTERVAL     30000

// ============================================================
// 调试配置
// ============================================================
#define SERIAL_BAUD_RATE 115200
#define DEBUG_ENABLED    1

#if DEBUG_ENABLED
  #define DEBUG_PRINT(x)    Serial.print(x)
  #define DEBUG_PRINTLN(x)  Serial.println(x)
  #define DEBUG_PRINTF(...) Serial.printf(__VA_ARGS__)
#else
  #define DEBUG_PRINT(x)
  #define DEBUG_PRINTLN(x)
  #define DEBUG_PRINTF(...)
#endif

#endif
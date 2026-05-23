#include <Arduino.h>
#include "config.h"
#include "wifi_manager.h"
#include "sensor.h"
#include "api_client.h"

enum SystemState {
    STATE_INIT,
    STATE_WIFI_CONNECTING,
    STATE_DEVICE_REGISTERING,
    STATE_RUNNING
};

static SystemState state = STATE_INIT;
static WiFiManager& wifi = WiFiManager::getInstance();
static DHT11Sensor& sensor = DHT11Sensor::getInstance();
static ApiClient& api = ApiClient::getInstance();

void setup() {
    Serial.begin(SERIAL_BAUD_RATE);
    delay(1000);

    DEBUG_PRINTLN("");
    DEBUG_PRINTLN("========================================");
    DEBUG_PRINTF("  ESP32-S3 DHT11 传感器节点\n");
    DEBUG_PRINTF("  设备ID: %s\n", DEVICE_ID);
    DEBUG_PRINTF("  设备名: %s\n", DEVICE_NAME);
    DEBUG_PRINTF("  位置:   %s\n", DEVICE_LOCATION);
    DEBUG_PRINTLN("========================================");

    if (!sensor.begin()) {
        DEBUG_PRINTLN("[启动] DHT11 初始化失败, 继续运行...");
    }

    state = STATE_WIFI_CONNECTING;
}

void loop() {
    wifi.checkConnection();

    switch (state) {
        case STATE_WIFI_CONNECTING: {
            if (wifi.isConnected()) {
                state = STATE_DEVICE_REGISTERING;
            } else if (!wifi.begin()) {
                DEBUG_PRINTF("[启动] WiFi 连接失败, %d秒后重试\n",
                             WIFI_RETRY_INTERVAL / 1000);
                delay(WIFI_RETRY_INTERVAL);
            }
            break;
        }

        case STATE_DEVICE_REGISTERING: {
            if (!wifi.isConnected()) {
                state = STATE_WIFI_CONNECTING;
                break;
            }

            DEBUG_PRINTLN("[注册] 正在注册设备到云端...");
            if (api.registerDevice()) {
                state = STATE_RUNNING;
                DEBUG_PRINTLN("[注册] 进入运行模式");
            } else {
                DEBUG_PRINTLN("[注册] 注册失败, 10秒后重试");
                delay(10000);
            }
            break;
        }

        case STATE_RUNNING: {
            if (!wifi.isConnected()) {
                state = STATE_WIFI_CONNECTING;
                break;
            }

            SensorData data = sensor.read();

            if (data.valid) {
                api.sendHeartbeat(data);
                api.reportStatus(data);
                api.updateShadow(data);
            }

            delay(2000);
            break;
        }
    }
}
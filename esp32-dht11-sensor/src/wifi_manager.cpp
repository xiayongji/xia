#include "wifi_manager.h"

WiFiManager& WiFiManager::getInstance() {
    static WiFiManager instance;
    return instance;
}

bool WiFiManager::begin() {
    DEBUG_PRINTF("[WiFi] 连接中: %s", WIFI_SSID);

    WiFi.mode(WIFI_STA);
    WiFi.setAutoReconnect(true);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

    unsigned long start = millis();
    while (WiFi.status() != WL_CONNECTED && (millis() - start) < 20000) {
        delay(500);
        DEBUG_PRINT(".");
    }

    if (WiFi.status() == WL_CONNECTED) {
        DEBUG_PRINTLN("");
        DEBUG_PRINTF("[WiFi] 已连接, IP: %s, MAC: %s\n",
                     WiFi.localIP().toString().c_str(),
                     WiFi.macAddress().c_str());
        return true;
    }

    DEBUG_PRINTLN("");
    DEBUG_PRINTLN("[WiFi] 连接失败");
    return false;
}

bool WiFiManager::isConnected() {
    return WiFi.status() == WL_CONNECTED;
}

void WiFiManager::checkConnection() {
    if (WiFi.status() == WL_CONNECTED) {
        _retryCount = 0;
        return;
    }

    unsigned long now = millis();
    if (now - _lastRetryTime < WIFI_RETRY_INTERVAL) return;

    _lastRetryTime = now;
    _retryCount++;

    if (_retryCount > WIFI_MAX_RETRIES) {
        DEBUG_PRINTF("[WiFi] 重连超过%d次, 重启设备\n", WIFI_MAX_RETRIES);
        delay(1000);
        ESP.restart();
    }

    DEBUG_PRINTF("[WiFi] 断连, 第%d次重试...\n", _retryCount);
    WiFi.reconnect();
}

String WiFiManager::getLocalIP() {
    return WiFi.localIP().toString();
}

String WiFiManager::getMacAddress() {
    return WiFi.macAddress();
}
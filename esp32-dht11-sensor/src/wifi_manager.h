#ifndef WIFI_MANAGER_H
#define WIFI_MANAGER_H

#include <WiFi.h>
#include "config.h"

class WiFiManager {
public:
    static WiFiManager& getInstance();

    bool begin();
    bool isConnected();
    void checkConnection();
    String getLocalIP();
    String getMacAddress();

private:
    WiFiManager() = default;
    WiFiManager(const WiFiManager&) = delete;
    WiFiManager& operator=(const WiFiManager&) = delete;

    unsigned long _lastRetryTime = 0;
    int _retryCount = 0;

    void onWiFiEvent(WiFiEvent_t event);
};

#endif
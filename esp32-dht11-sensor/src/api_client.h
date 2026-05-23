#ifndef API_CLIENT_H
#define API_CLIENT_H

#include <HTTPClient.h>
#include <ArduinoJson.h>
#include "config.h"
#include "sensor.h"

struct ApiResult {
    bool success;
    int httpCode;
    String response;
};

class ApiClient {
public:
    static ApiClient& getInstance();

    bool registerDevice();
    bool sendHeartbeat(const SensorData& data);
    bool reportStatus(const SensorData& data);
    bool updateShadow(const SensorData& data);

    bool isRegistered() { return _registered; }

private:
    ApiClient() = default;
    ApiClient(const ApiClient&) = delete;
    ApiClient& operator=(const ApiClient&) = delete;

    bool _registered = false;
    unsigned long _lastHeartbeatTime = 0;
    unsigned long _lastStatusReportTime = 0;

    ApiResult httpPost(const String& url, const String& jsonBody);
    ApiResult httpPut(const String& url, const String& jsonBody);

    String buildDeviceServiceUrl(const String& path);
    String buildEdgeServiceUrl(const String& path);

    String getDeviceRegisterJson();
    String getHeartbeatJson(const SensorData& data);
    String getStatusReportJson(const SensorData& data);
    String getShadowReportJson(const SensorData& data);
};

#endif
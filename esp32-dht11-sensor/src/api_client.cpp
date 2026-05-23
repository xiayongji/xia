#include "api_client.h"
#include "wifi_manager.h"

ApiClient& ApiClient::getInstance() {
    static ApiClient instance;
    return instance;
}

String ApiClient::buildDeviceServiceUrl(const String& path) {
    String url = "http://";
    url += DEVICE_SERVICE_HOST;
    url += ":";
    url += String(DEVICE_SERVICE_PORT);
    url += path;
    return url;
}

String ApiClient::buildEdgeServiceUrl(const String& path) {
    String url = "http://";
    url += EDGE_SERVICE_HOST;
    url += ":";
    url += String(EDGE_SERVICE_PORT);
    url += path;
    return url;
}

ApiResult ApiClient::httpPost(const String& url, const String& jsonBody) {
    ApiResult result = {false, 0, ""};
    HTTPClient http;

    http.setTimeout(10000);
    http.begin(url);
    http.addHeader("Content-Type", "application/json");

    int code = http.POST(jsonBody);
    result.httpCode = code;
    result.response = http.getString();
    result.success = (code >= 200 && code < 300);

    http.end();
    return result;
}

ApiResult ApiClient::httpPut(const String& url, const String& jsonBody) {
    ApiResult result = {false, 0, ""};
    HTTPClient http;

    http.setTimeout(10000);
    http.begin(url);
    http.addHeader("Content-Type", "application/json");

    int code = http.PUT(jsonBody);
    result.httpCode = code;
    result.response = http.getString();
    result.success = (code >= 200 && code < 300);

    http.end();
    return result;
}

String ApiClient::getDeviceRegisterJson() {
    WiFiManager& wifi = WiFiManager::getInstance();
    JsonDocument doc;

    doc["deviceId"]       = DEVICE_ID;
    doc["name"]           = DEVICE_NAME;
    doc["type"]           = "temperature_humidity_sensor";
    doc["protocol"]       = "wifi";
    doc["status"]         = "online";
    doc["ipAddress"]      = wifi.getLocalIP();
    doc["macAddress"]     = wifi.getMacAddress();
    doc["firmwareVersion"] = "1.0.0";
    doc["manufacturer"]   = "ESP32";
    doc["model"]          = "ESP32-S3-DHT11";

    String json;
    serializeJson(doc, json);
    return json;
}

String ApiClient::getHeartbeatJson(const SensorData& data) {
    JsonDocument doc;

    doc["deviceId"]      = DEVICE_ID;
    doc["status"]        = "online";
    doc["temperature"]   = data.valid ? data.temperature : 0.0f;
    doc["networkStatus"] = "wifi_connected";

    String json;
    serializeJson(doc, json);
    return json;
}

String ApiClient::getStatusReportJson(const SensorData& data) {
    JsonDocument doc;

    doc["status"]      = "online";
    doc["temperature"] = data.valid ? round(data.temperature * 10.0f) / 10.0f : 0.0;
    doc["humidity"]    = data.valid ? round(data.humidity * 10.0f) / 10.0f : 0.0;
    doc["properties"]  = DEVICE_LOCATION;

    String json;
    serializeJson(doc, json);
    return json;
}

String ApiClient::getShadowReportJson(const SensorData& data) {
    JsonDocument doc;

    doc["temperature"] = data.valid ? round(data.temperature * 10.0f) / 10.0f : 0.0;
    doc["humidity"]    = data.valid ? round(data.humidity * 10.0f) / 10.0f : 0.0;
    doc["location"]    = DEVICE_LOCATION;
    doc["status"]      = "online";

    String json;
    serializeJson(doc, json);
    return json;
}

bool ApiClient::registerDevice() {
    String url = buildDeviceServiceUrl("/api/devices/register");
    String json = getDeviceRegisterJson();

    DEBUG_PRINTF("[API] 注册设备 -> %s\n", DEVICE_ID);
    DEBUG_PRINTF("[API] URL: %s\n", url.c_str());
    DEBUG_PRINTF("[API] Body: %s\n", json.c_str());

    ApiResult result = httpPost(url, json);

    if (result.success) {
        _registered = true;
        DEBUG_PRINTF("[API] 设备注册成功 (HTTP %d)\n", result.httpCode);

        String edgeUrl = buildEdgeServiceUrl("/api/edge/devices/register");
        JsonDocument edgeDoc;
        edgeDoc["deviceId"]   = DEVICE_ID;
        edgeDoc["deviceName"] = DEVICE_NAME;
        edgeDoc["deviceType"] = "temperature_humidity_sensor";
        edgeDoc["protocol"]   = "wifi";
        edgeDoc["location"]   = DEVICE_LOCATION;

        String edgeJson;
        serializeJson(edgeDoc, edgeJson);

        ApiResult edgeResult = httpPost(edgeUrl, edgeJson);
        DEBUG_PRINTF("[API] 边缘网关注册 %s (HTTP %d)\n",
                     edgeResult.success ? "成功" : "失败",
                     edgeResult.httpCode);
        return true;
    }

    DEBUG_PRINTF("[API] 设备注册失败 (HTTP %d): %s\n",
                 result.httpCode, result.response.c_str());
    return false;
}

bool ApiClient::sendHeartbeat(const SensorData& data) {
    unsigned long now = millis();
    if (now - _lastHeartbeatTime < HEARTBEAT_INTERVAL) return true;
    _lastHeartbeatTime = now;

    String url = buildDeviceServiceUrl("/api/devices/") + DEVICE_ID + "/heartbeat";
    String json = getHeartbeatJson(data);

    ApiResult result = httpPost(url, json);

    if (result.success) {
        DEBUG_PRINTF("[心跳] 发送成功, T=%.1f°C\n",
                     data.valid ? data.temperature : 0.0f);
        return true;
    }

    DEBUG_PRINTF("[心跳] 发送失败 (HTTP %d): %s\n",
                 result.httpCode, result.response.c_str());
    return false;
}

bool ApiClient::reportStatus(const SensorData& data) {
    unsigned long now = millis();
    if (now - _lastStatusReportTime < STATUS_REPORT_INTERVAL) return true;
    _lastStatusReportTime = now;

    String url = buildEdgeServiceUrl("/api/edge/devices/") + DEVICE_ID + "/status";
    String json = getStatusReportJson(data);

    ApiResult result = httpPut(url, json);

    if (result.success) {
        DEBUG_PRINTF("[状态] 上报成功, T=%.1f°C, H=%.1f%%\n",
                     data.temperature, data.humidity);
        return true;
    }

    DEBUG_PRINTF("[状态] 上报失败 (HTTP %d): %s\n",
                 result.httpCode, result.response.c_str());
    return false;
}

bool ApiClient::updateShadow(const SensorData& data) {
    String url = buildDeviceServiceUrl("/api/devices/") + DEVICE_ID + "/shadow/reported";
    String json = getShadowReportJson(data);

    ApiResult result = httpPut(url, json);

    if (result.success) {
        DEBUG_PRINTLN("[影子] 同步成功");
        return true;
    }

    DEBUG_PRINTF("[影子] 同步失败 (HTTP %d)\n", result.httpCode);
    return false;
}
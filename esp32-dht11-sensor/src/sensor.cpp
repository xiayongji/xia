#include "sensor.h"

DHT11Sensor& DHT11Sensor::getInstance() {
    static DHT11Sensor instance;
    return instance;
}

bool DHT11Sensor::begin() {
    _dht.begin();
    _initialized = true;
    DEBUG_PRINTF("[DHT11] 初始化完成, 引脚: GPIO%d\n", DHT11_PIN);

    delay(1000);
    SensorData data = read();
    if (data.valid) {
        DEBUG_PRINTF("[DHT11] 首次读数 - 温度: %.1f°C, 湿度: %.1f%%\n",
                     data.temperature, data.humidity);
    }

    return data.valid;
}

SensorData DHT11Sensor::read() {
    SensorData data = {0.0f, 0.0f, false};

    unsigned long now = millis();
    if (now - _lastReadTime < DHT11_READ_INTERVAL) {
        return _lastValidData;
    }
    _lastReadTime = now;

    float h = _dht.readHumidity();
    float t = _dht.readTemperature();

    if (isnan(h) || isnan(t)) {
        DEBUG_PRINTLN("[DHT11] 读数失败, 返回上次有效数据");
        return _lastValidData;
    }

    if (t > 80.0f || t < -20.0f || h > 100.0f || h < 0.0f) {
        DEBUG_PRINTF("[DHT11] 异常数据: T=%.1f H=%.1f, 已丢弃\n", t, h);
        return _lastValidData;
    }

    data.temperature = t;
    data.humidity = h;
    data.valid = true;
    _lastValidData = data;

    return data;
}

bool DHT11Sensor::isWorking() {
    return _initialized && _lastValidData.valid;
}
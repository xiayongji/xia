#ifndef SENSOR_H
#define SENSOR_H

#include <DHT.h>
#include "config.h"

struct SensorData {
    float temperature;
    float humidity;
    bool valid;
};

class DHT11Sensor {
public:
    static DHT11Sensor& getInstance();

    bool begin();
    SensorData read();
    bool isWorking();

private:
    DHT11Sensor() : _dht(DHT11_PIN, DHT11) {}
    DHT11Sensor(const DHT11Sensor&) = delete;
    DHT11Sensor& operator=(const DHT11Sensor&) = delete;

    DHT _dht;
    bool _initialized = false;
    unsigned long _lastReadTime = 0;
    SensorData _lastValidData = {0.0f, 0.0f, false};
};

#endif
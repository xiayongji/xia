package com.smarthome.multimodal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataPoint {
    
    private String sensorId;
    private SensorType sensorType;
    @Builder.Default
    private List<SensorReading> readings = new ArrayList<>();
    
    public void addReading(SensorReading reading) {
        readings.add(reading);
    }
    
    public void clearReadings() {
        readings.clear();
    }
}
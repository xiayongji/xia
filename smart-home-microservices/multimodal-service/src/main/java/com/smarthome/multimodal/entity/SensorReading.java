package com.smarthome.multimodal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorReading {
    
    private String sensorId;
    private SensorType sensorType;
    private double value;
    private double confidence;
    private LocalDateTime timestamp;
}
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
public class SensorEvent {
    
    private String sensorId;
    private SensorType sensorType;
    private SensorEventType eventType;
    private double confidence;
    private LocalDateTime timestamp;
}
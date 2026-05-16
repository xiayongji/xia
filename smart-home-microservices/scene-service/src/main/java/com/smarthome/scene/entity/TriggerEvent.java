package com.smarthome.scene.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerEvent {
    
    private String eventId;
    private String triggerType;
    private String deviceId;
    private String sensorType;
    private Double value;
    private String location;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
    
    public boolean hasValue() {
        return value != null;
    }
    
    public boolean hasLocation() {
        return location != null && !location.isEmpty();
    }
}
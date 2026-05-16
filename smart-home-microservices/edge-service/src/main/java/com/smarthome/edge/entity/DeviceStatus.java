package com.smarthome.edge.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatus {
    
    private String deviceId;
    private String status;
    private String deviceType;
    private double power;
    private LocalDateTime lastUpdateTime;
    
    public LocalDateTime getLastUpdate() {
        return lastUpdateTime;
    }
}
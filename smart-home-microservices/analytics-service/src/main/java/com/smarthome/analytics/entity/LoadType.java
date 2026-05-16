package com.smarthome.analytics.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadType {
    
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private double consumption;
    private double avgPower;
    private double peakPower;
    private boolean isHighEnergy;
}
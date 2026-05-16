package com.smarthome.analytics.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PowerConsumptionPattern {
    
    private String deviceId;
    private Range normalRange;
    private Range peakRange;
    private double avgPower;
    private double stdDev;
    private List<Double> recentReadings;
    
    public boolean isInNormalRange(double power) {
        return normalRange != null && normalRange.contains(power);
    }
    
    public boolean isPeak(double power) {
        return peakRange != null && power > peakRange.getMax();
    }
}
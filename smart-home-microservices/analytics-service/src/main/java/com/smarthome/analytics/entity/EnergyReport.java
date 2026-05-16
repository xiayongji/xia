package com.smarthome.analytics.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyReport {
    
    private String deviceId;
    private String deviceName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double totalConsumption;
    private double avgPower;
    private double peakPower;
    private double offPeakConsumption;
    private double peakConsumption;
    private List<LoadType> highEnergyDevices;
    private Map<String, Double> consumptionByHour;
    private String recommendation;
}
package com.smarthome.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PowerConsumptionPattern {
    private String deviceId;
    private Double minPower;
    private Double maxPower;
    private Double avgPower;
    private Double stdDeviation;
    private Double peakThreshold;
    private Double normalRangeMin;
    private Double normalRangeMax;
    private LocalDateTime lastUpdated;
}
package com.smarthome.analytics.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyResult {
    
    private String deviceId;
    private boolean isAnomaly;
    private double anomalyProbability;
    private String message;
    private LocalDateTime timestamp;
}
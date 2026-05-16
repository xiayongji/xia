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
public class PowerReading {
    
    private String deviceId;
    private double power;
    private LocalDateTime timestamp;
    private double rateOfChange;
}
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
public class PowerReading {
    private String deviceId;
    private Double power;
    private Double voltage;
    private Double current;
    private LocalDateTime timestamp;
    private Double temperature;
    private Double humidity;
}
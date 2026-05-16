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
public class EnergyRecord {
    private Long id;
    private String deviceId;
    private Double energy;
    private LocalDateTime timestamp;
    private String properties;
}
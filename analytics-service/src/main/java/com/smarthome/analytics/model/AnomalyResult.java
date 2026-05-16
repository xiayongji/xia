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
public class AnomalyResult {
    private String deviceId;
    private Double score;
    private Boolean isAnomaly;
    private String level;
    private String anomalyType;
    private String description;
    private LocalDateTime timestamp;
    private Double threshold;
}
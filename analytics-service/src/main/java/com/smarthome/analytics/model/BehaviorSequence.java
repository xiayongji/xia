package com.smarthome.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorSequence {
    private String sequenceId;
    private Long userId;
    private List<BehaviorStep> steps;
    private Double confidence;
    private String patternType;
    private LocalDateTime detectedTime;
    private Integer occurrenceCount;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class BehaviorStep {
    private String deviceId;
    private String action;
    private Integer order;
    private LocalDateTime timestamp;
    private Integer timeOffsetSeconds;
}